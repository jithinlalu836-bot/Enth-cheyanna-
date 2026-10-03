const express = require('express');
const http = require('http');
const { WebSocketServer, WebSocket } = require('ws');
const path = require('path');
require('dotenv').config();

const app = express();
const server = http.createServer(app);
const wss = new WebSocketServer({ server });

const PORT = (process.env.PORT && process.env.PORT !== '8080') ? process.env.PORT : 3000;

app.use(express.json());
app.use(express.static(path.join(__dirname, 'public')));

// YouTube Live Chat State
let youtubeApiKey = process.env.YOUTUBE_API_KEY || '';
let streamUrlOrId = process.env.YOUTUBE_STREAM_ID || '';
let isYouTubePolling = false;
let pollingTimeoutId = null;
let activeLiveChatId = null;
let nextPageToken = null;
let processedMessageIds = new Set();
let connectionStatus = {
  connected: false,
  connecting: false,
  error: null,
  streamTitle: '',
  channelName: '',
  liveChatId: null,
  messagesProcessed: 0
};

// Spam Protection Tracker: Map<userIdOrName, lastCommandTimestamp>
const lastCommandTime = new Map();

// Test Mode State
let isTestMode = false;
let testModeIntervalId = null;

const sampleTestUsers = [
  'DragonSlayer', 'PixelKnight', 'ShadowNinja', 'Valkyrie_Live',
  'GamerAlex', 'MysticMage', 'IronFist', 'CyberSamurai',
  'FrostArcher', 'NeonBlade', 'StarRanger', 'BlazeRider'
];

const availableCommands = ['!attack', '!left', '!right', '!jump'];

// Broadcast message to all connected WebSocket clients
function broadcast(data) {
  const json = JSON.stringify(data);
  wss.clients.forEach((client) => {
    if (client.readyState === WebSocket.OPEN) {
      client.send(json);
    }
  });
}

// Extract YouTube Video ID from standard YouTube URLs or direct 11-char ID
function extractVideoId(input) {
  if (!input) return null;
  const trimmed = input.trim();
  if (/^[a-zA-Z0-9_-]{11}$/.test(trimmed)) {
    return trimmed;
  }
  const patterns = [
    /(?:https?:\/\/)?(?:www\.)?youtube\.com\/watch\?v=([a-zA-Z0-9_-]{11})/,
    /(?:https?:\/\/)?(?:www\.)?youtube\.com\/live\/([a-zA-Z0-9_-]{11})/,
    /(?:https?:\/\/)?(?:www\.)?youtu\.be\/([a-zA-Z0-9_-]{11})/,
    /(?:https?:\/\/)?(?:www\.)?youtube\.com\/embed\/([a-zA-Z0-9_-]{11})/
  ];
  for (const p of patterns) {
    const match = trimmed.match(p);
    if (match && match[1]) return match[1];
  }
  return null;
}

// Parse Command: !left, !right, !jump, !attack
function parseCommand(messageText) {
  if (!messageText) return null;
  const trimmed = messageText.trim().toLowerCase();
  
  if (trimmed === '!left' || trimmed === '!l') return 'left';
  if (trimmed === '!right' || trimmed === '!r') return 'right';
  if (trimmed === '!jump' || trimmed === '!j' || trimmed === '!up') return 'jump';
  if (trimmed === '!attack' || trimmed === '!a' || trimmed === '!hit' || trimmed === '!slash') return 'attack';
  
  // If message starts with command (e.g. "!attack boss!")
  const parts = trimmed.split(/\s+/);
  const firstWord = parts[0];
  if (firstWord === '!left' || firstWord === '!l') return 'left';
  if (firstWord === '!right' || firstWord === '!r') return 'right';
  if (firstWord === '!jump' || firstWord === '!j') return 'jump';
  if (firstWord === '!attack' || firstWord === '!a') return 'attack';

  return null; // Ignore messages that are not commands
}

// Process a viewer command with spam limit (1 command per second per viewer)
function handleViewerMessage(username, userId, rawMessage, isSuperChat = false, dollarAmount = 0) {
  const command = parseCommand(rawMessage);
  if (!command) return false; // Ignore non-command messages

  const viewerKey = userId || username;
  const now = Date.now();
  const lastTime = lastCommandTime.get(viewerKey) || 0;

  // Basic spam limit: 1 command per viewer every 1 second (1000ms)
  if (now - lastTime < 1000) {
    return false;
  }
  lastCommandTime.set(viewerKey, now);

  const payload = {
    type: 'CHAT_COMMAND',
    username,
    userId: viewerKey,
    command,
    rawMessage,
    isSuperChat,
    dollarAmount,
    timestamp: now
  };

  connectionStatus.messagesProcessed++;
  broadcast(payload);
  return true;
}

// Connect to YouTube Live Stream
async function connectToYouTube(videoIdOrUrl, apiKey) {
  const videoId = extractVideoId(videoIdOrUrl);
  if (!videoId) {
    connectionStatus.error = 'Invalid YouTube URL or Video ID.';
    broadcast({ type: 'STATUS_UPDATE', status: connectionStatus });
    return false;
  }

  if (!apiKey) {
    connectionStatus.error = 'YouTube Data API v3 Key is missing.';
    broadcast({ type: 'STATUS_UPDATE', status: connectionStatus });
    return false;
  }

  stopYouTubePolling();
  connectionStatus.connecting = true;
  connectionStatus.error = null;
  broadcast({ type: 'STATUS_UPDATE', status: connectionStatus });

  try {
    const videoDetailsUrl = `https://www.googleapis.com/youtube/v3/videos?id=${videoId}&part=snippet,liveStreamingDetails&key=${apiKey}`;
    const res = await fetch(videoDetailsUrl);
    if (!res.ok) {
      const errText = await res.text();
      connectionStatus.connecting = false;
      connectionStatus.error = `YouTube API Error (${res.status}): ${errText}`;
      broadcast({ type: 'STATUS_UPDATE', status: connectionStatus });
      return false;
    }

    const data = await res.json();
    if (!data.items || data.items.length === 0) {
      connectionStatus.connecting = false;
      connectionStatus.error = 'Video not found. Please verify the link.';
      broadcast({ type: 'STATUS_UPDATE', status: connectionStatus });
      return false;
    }

    const item = data.items[0];
    const title = item.snippet?.title || 'YouTube Live Stream';
    const channelTitle = item.snippet?.channelTitle || 'Streamer';
    const liveChatId = item.liveStreamingDetails?.activeLiveChatId;

    if (!liveChatId) {
      connectionStatus.connecting = false;
      connectionStatus.error = 'Live chat is not active or video is not currently live.';
      broadcast({ type: 'STATUS_UPDATE', status: connectionStatus });
      return false;
    }

    activeLiveChatId = liveChatId;
    youtubeApiKey = apiKey;
    streamUrlOrId = videoIdOrUrl;
    isYouTubePolling = true;

    connectionStatus.connected = true;
    connectionStatus.connecting = false;
    connectionStatus.streamTitle = title;
    connectionStatus.channelName = channelTitle;
    connectionStatus.liveChatId = liveChatId;
    connectionStatus.error = null;

    // Automatically stop test mode if real stream is connected
    if (isTestMode) {
      toggleTestMode(false);
    }

    broadcast({ type: 'STATUS_UPDATE', status: connectionStatus });

    // Start polling loop respecting pollingIntervalMillis
    processedMessageIds.clear();
    nextPageToken = null;
    pollLiveChatMessages();
    return true;
  } catch (err) {
    connectionStatus.connecting = false;
    connectionStatus.error = `Connection failed: ${err.message}`;
    broadcast({ type: 'STATUS_UPDATE', status: connectionStatus });
    return false;
  }
}

// Poll liveChat/messages endpoint
async function pollLiveChatMessages() {
  if (!isYouTubePolling || !activeLiveChatId || !youtubeApiKey) return;

  let pollInterval = 2500; // default 2.5s

  try {
    const pageParam = nextPageToken ? `&pageToken=${encodeURIComponent(nextPageToken)}` : '';
    const chatUrl = `https://www.googleapis.com/youtube/v3/liveChat/messages?liveChatId=${encodeURIComponent(activeLiveChatId)}&part=snippet,authorDetails&key=${encodeURIComponent(youtubeApiKey)}${pageParam}`;

    const res = await fetch(chatUrl);
    if (res.ok) {
      const data = await res.json();
      nextPageToken = data.nextPageToken || null;
      
      // Respect pollingIntervalMillis from the API response so quota is not wasted
      if (data.pollingIntervalMillis && data.pollingIntervalMillis >= 1000) {
        pollInterval = data.pollingIntervalMillis;
      }

      if (data.items && Array.isArray(data.items)) {
        for (const item of data.items) {
          const msgId = item.id;
          if (msgId && !processedMessageIds.has(msgId)) {
            processedMessageIds.add(msgId);

            const authorName = item.authorDetails?.displayName || 'Viewer';
            const channelId = item.authorDetails?.channelId || authorName;
            const messageType = item.snippet?.type || 'textMessageEvent';
            const displayMessage = item.snippet?.displayMessage || '';

            if (messageType === 'superChatEvent') {
              const amountMicros = item.snippet?.superChatDetails?.amountMicros || 1000000;
              const dollars = Math.max(1, amountMicros / 1000000);
              const userComment = item.snippet?.superChatDetails?.userComment || displayMessage || '!attack';
              handleViewerMessage(authorName, channelId, userComment, true, dollars);
            } else {
              handleViewerMessage(authorName, channelId, displayMessage);
            }
          }
        }
      }
    } else {
      // If error (quota or stream end), back off
      pollInterval = 5000;
    }
  } catch (err) {
    pollInterval = 5000;
  }

  if (isYouTubePolling) {
    pollingTimeoutId = setTimeout(pollLiveChatMessages, pollInterval);
  }
}

// Stop YouTube Polling
function stopYouTubePolling() {
  isYouTubePolling = false;
  if (pollingTimeoutId) {
    clearTimeout(pollingTimeoutId);
    pollingTimeoutId = null;
  }
  connectionStatus.connected = false;
  connectionStatus.connecting = false;
  connectionStatus.liveChatId = null;
  broadcast({ type: 'STATUS_UPDATE', status: connectionStatus });
}

// Toggle Test Mode Simulation
function toggleTestMode(enable) {
  isTestMode = enable !== undefined ? enable : !isTestMode;

  if (testModeIntervalId) {
    clearInterval(testModeIntervalId);
    testModeIntervalId = null;
  }

  if (isTestMode) {
    // Generate simulated chat commands every 900ms - 1500ms
    testModeIntervalId = setInterval(() => {
      const user = sampleTestUsers[Math.floor(Math.random() * sampleTestUsers.length)];
      // Bias towards !attack for active fun gameplay
      const roll = Math.random();
      let cmd = '!attack';
      if (roll < 0.25) cmd = '!left';
      else if (roll < 0.50) cmd = '!right';
      else if (roll < 0.65) cmd = '!jump';

      handleViewerMessage(user, `user_${user}`, cmd);
    }, 1100);
  }

  broadcast({ type: 'TEST_MODE_UPDATE', isTestMode });
  return isTestMode;
}

// API Routes
app.get('/api/status', (req, res) => {
  res.json({
    status: connectionStatus,
    isTestMode,
    savedUrl: streamUrlOrId,
    hasApiKey: !!youtubeApiKey
  });
});

app.post('/api/connect', async (req, res) => {
  const { url, apiKey } = req.body;
  const key = apiKey || youtubeApiKey;
  if (!url || !key) {
    return res.status(400).json({ error: 'URL and API Key are required' });
  }
  const success = await connectToYouTube(url, key);
  res.json({ success, status: connectionStatus });
});

app.post('/api/disconnect', (req, res) => {
  stopYouTubePolling();
  res.json({ success: true, status: connectionStatus });
});

app.post('/api/test-mode', (req, res) => {
  const { enabled } = req.body;
  const active = toggleTestMode(enabled);
  res.json({ success: true, isTestMode: active });
});

app.post('/api/test-command', (req, res) => {
  const { username, command } = req.body;
  const user = username || sampleTestUsers[Math.floor(Math.random() * sampleTestUsers.length)];
  const cmd = command || '!attack';
  const handled = handleViewerMessage(user, `user_${user}`, cmd);
  res.json({ success: handled, user, command: cmd });
});

// WebSocket Connection Handler
wss.on('connection', (ws) => {
  // Send initial state to newly connected client
  ws.send(JSON.stringify({
    type: 'INIT_STATE',
    status: connectionStatus,
    isTestMode
  }));

  ws.on('message', (message) => {
    try {
      const data = JSON.parse(message);
      if (data.type === 'TEST_COMMAND') {
        handleViewerMessage(data.username || 'TestViewer', data.userId || 'test_user', data.command || '!attack');
      } else if (data.type === 'TOGGLE_TEST_MODE') {
        toggleTestMode(data.enabled);
      }
    } catch (e) {
      // Ignore invalid JSON
    }
  });
});

// Start Server
server.listen(PORT, () => {
  console.log(`🎮 YouTube Live Chat Boss Fight Server running at http://localhost:${PORT}`);

  // Automatically start test mode on startup so user sees the game in action immediately!
  toggleTestMode(true);

  // If environment variables were pre-configured, auto-connect to live stream
  if (streamUrlOrId && youtubeApiKey) {
    console.log(`🔗 Auto-connecting to YouTube stream: ${streamUrlOrId}`);
    connectToYouTube(streamUrlOrId, youtubeApiKey);
  }
});
