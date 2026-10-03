// ========================================================
// FLAG RACE - INTERACTIVE YOUTUBE LIVESTREAM GAME SHOW
// Single-App Architecture for Mobile (Portrait 9:16)
// ========================================================

(function () {
  'use strict';

  // Currency exchange rates to USD for Super Chats
  const CURRENCY_TO_USD = {
    USD: 1.0,
    EUR: 1.08,
    GBP: 1.30,
    CAD: 0.74,
    AUD: 0.67,
    INR: 0.012,
    BRL: 0.18,
    TRY: 0.03,
    JPY: 0.0067,
    KRW: 0.00075,
    MXN: 0.052,
    PHP: 0.017,
    IDR: 0.000065
  };

  // Default Countries Dataset (with 2-letter ISO codes for flagcdn.com)
  const DEFAULT_COUNTRIES = [
    { code: 'in', name: 'India', aliases: ['india', 'bharat', 'hindustan', 'ind', '🇮🇳'] },
    { code: 'tr', name: 'Turkey', aliases: ['turkey', 'turkiye', 'türkiye', 'turk', 'tr', '🇹🇷'] },
    { code: 'us', name: 'USA', aliases: ['usa', 'united states', 'america', 'us', 'american', '🇺🇸'] },
    { code: 'br', name: 'Brazil', aliases: ['brazil', 'brasil', 'br', '🇧🇷'] },
    { code: 'id', name: 'Indonesia', aliases: ['indonesia', 'indo', 'id', '🇮🇩'] },
    { code: 'pk', name: 'Pakistan', aliases: ['pakistan', 'pak', 'pk', '🇵🇰'] },
    { code: 'ph', name: 'Philippines', aliases: ['philippines', 'pilipinas', 'pinoy', 'ph', '🇵🇭'] },
    { code: 'de', name: 'Germany', aliases: ['germany', 'deutschland', 'de', '🇩🇪'] },
    { code: 'gb', name: 'UK', aliases: ['uk', 'united kingdom', 'england', 'britain', 'gb', '🇬🇧'] },
    { code: 'jp', name: 'Japan', aliases: ['japan', 'nippon', 'nihon', 'jp', '🇯🇵'] },
    { code: 'mx', name: 'Mexico', aliases: ['mexico', 'méxico', 'mx', '🇲🇽'] },
    { code: 'vn', name: 'Vietnam', aliases: ['vietnam', 'viet nam', 'việt nam', 'vn', '🇻🇳'] },
    { code: 'eg', name: 'Egypt', aliases: ['egypt', 'misr', 'eg', '🇪🇬'] },
    { code: 'ar', name: 'Argentina', aliases: ['argentina', 'arg', 'ar', '🇦🇷'] },
    { code: 'fr', name: 'France', aliases: ['france', 'français', 'fr', '🇫🇷'] },
    { code: 'es', name: 'Spain', aliases: ['spain', 'españa', 'es', '🇪🇸'] },
    { code: 'it', name: 'Italy', aliases: ['italy', 'italia', 'it', '🇮🇹'] },
    { code: 'kr', name: 'South Korea', aliases: ['korea', 'south korea', 'kr', '🇰🇷'] },
    { code: 'ca', name: 'Canada', aliases: ['canada', 'ca', '🇨🇦'] },
    { code: 'au', name: 'Australia', aliases: ['australia', 'aussie', 'au', '🇦🇺'] },
    { code: 'sa', name: 'Saudi Arabia', aliases: ['saudi', 'saudi arabia', 'ksa', 'sa', '🇸🇦'] },
    { code: 'ma', name: 'Morocco', aliases: ['morocco', 'maroc', 'ma', '🇲🇦'] },
    { code: 'co', name: 'Colombia', aliases: ['colombia', 'co', '🇨🇴'] },
    { code: 'pl', name: 'Poland', aliases: ['poland', 'polska', 'pl', '🇵🇱'] },
    { code: 'ng', name: 'Nigeria', aliases: ['nigeria', 'naija', 'ng', '🇳🇬'] },
    { code: 'bd', name: 'Bangladesh', aliases: ['bangladesh', 'bangla', 'bd', '🇧🇩'] },
    { code: 'ru', name: 'Russia', aliases: ['russia', 'rossiya', 'ru', '🇷🇺'] },
    { code: 'th', name: 'Thailand', aliases: ['thailand', 'thai', 'th', '🇹🇭'] },
    { code: 'za', name: 'South Africa', aliases: ['south africa', 'mzansi', 'za', '🇿🇦'] },
    { code: 'dz', name: 'Algeria', aliases: ['algeria', 'dz', '🇩🇿'] },
    { code: 'pe', name: 'Peru', aliases: ['peru', 'pe', '🇵🇪'] },
    { code: 'cl', name: 'Chile', aliases: ['chile', 'cl', '🇨🇱'] },
    { code: 'nl', name: 'Netherlands', aliases: ['netherlands', 'holland', 'nl', '🇳🇱'] },
    { code: 'se', name: 'Sweden', aliases: ['sweden', 'sverige', 'se', '🇸🇪'] },
    { code: 'pt', name: 'Portugal', aliases: ['portugal', 'pt', '🇵🇹'] },
    { code: 'gr', name: 'Greece', aliases: ['greece', 'hellas', 'gr', '🇬🇷'] },
    { code: 'ae', name: 'UAE', aliases: ['uae', 'emirates', 'dubai', 'ae', '🇦🇪'] },
    { code: 'my', name: 'Malaysia', aliases: ['malaysia', 'my', '🇲🇾'] },
    { code: 'ua', name: 'Ukraine', aliases: ['ukraine', 'ua', '🇺🇦'] },
    { code: 'no', name: 'Norway', aliases: ['norway', 'norge', 'no', '🇳🇴'] }
  ];

  // State
  let countries = [];
  let userCooldowns = new Map(); // username -> lastTimestamp
  let totalDonationGoal = 50.0;
  let currentDonationTotal = 0.0;
  let topDonor = { name: 'None', amount: 0.0 };

  // Timer & Round State
  let roundNumber = 1;
  let roundDuration = 600; // 10 minutes in seconds
  let roundTimeRemaining = 600;
  let isTimerRunning = true;
  let timerIntervalId = null;
  let roundHistory = [];

  // YouTube Live Poller State
  let youtubeApiKey = '';
  let youtubeVideoId = '';
  let activeLiveChatId = null;
  let nextPageToken = null;
  let isYouTubePolling = false;
  let pollingTimeoutId = null;
  const processedMessageIds = new Set();

  // Test Mode Simulation State
  let isTestMode = true;
  let testModeIntervalId = null;

  // Wake Lock Sentinel
  let wakeLockSentinel = null;

  // DOM Elements
  const livePill = document.getElementById('livePill');
  const liveStatusText = document.getElementById('liveStatusText');
  const roundBadge = document.getElementById('roundBadge');
  const timerDisplay = document.getElementById('timerDisplay');
  const btnToggleSimulate = document.getElementById('btnToggleSimulate');
  const btnOpenSetup = document.getElementById('btnOpenSetup');
  const btnToggleCleanMode = document.getElementById('btnToggleCleanMode');
  const btnExitCleanMode = document.getElementById('btnExitCleanMode');

  // Donation Elements
  const currentGoalAmountEl = document.getElementById('currentGoalAmount');
  const targetGoalAmountEl = document.getElementById('targetGoalAmount');
  const goalProgressFill = document.getElementById('goalProgressFill');
  const topDonorNameEl = document.getElementById('topDonorName');
  const topDonorAmountEl = document.getElementById('topDonorAmount');

  // Podium Elements
  const name1 = document.getElementById('name1');
  const score1 = document.getElementById('score1');
  const flag1 = document.getElementById('flag1');
  const gap1 = document.getElementById('gap1');

  const name2 = document.getElementById('name2');
  const score2 = document.getElementById('score2');
  const flag2 = document.getElementById('flag2');
  const gap2 = document.getElementById('gap2');

  const name3 = document.getElementById('name3');
  const score3 = document.getElementById('score3');
  const flag3 = document.getElementById('flag3');
  const gap3 = document.getElementById('gap3');

  // Ticker & Grid Elements
  const tickerContent = document.getElementById('tickerContent');
  const flagsGrid = document.getElementById('flagsGrid');
  const totalCountriesCount = document.getElementById('totalCountriesCount');

  // Modals
  const setupModal = document.getElementById('setupModal');
  const btnCloseSetup = document.getElementById('btnCloseSetup');
  const btnSaveAndClose = document.getElementById('btnSaveAndClose');
  const winnerModal = document.getElementById('winnerModal');
  const winnerCountryName = document.getElementById('winnerCountryName');
  const winnerFlagImg = document.getElementById('winnerFlagImg');
  const winnerScore = document.getElementById('winnerScore');
  const nextRoundCountdownEl = document.getElementById('nextRoundCountdown');
  const btnStartNextRoundNow = document.getElementById('btnStartNextRoundNow');
  const btnCloseWinnerModal = document.getElementById('btnCloseWinnerModal');

  // Setup Form Elements
  const inputApiKey = document.getElementById('inputApiKey');
  const inputVideoId = document.getElementById('inputVideoId');
  const btnConnectStream = document.getElementById('btnConnectStream');
  const btnDisconnectStream = document.getElementById('btnDisconnectStream');
  const streamStatusBox = document.getElementById('streamStatusBox');
  const selectRoundDuration = document.getElementById('selectRoundDuration');
  const inputGoalTarget = document.getElementById('inputGoalTarget');
  const btnToggleTimer = document.getElementById('btnToggleTimer');
  const btnAddExtraMinute = document.getElementById('btnAddExtraMinute');
  const btnRestartRound = document.getElementById('btnRestartRound');
  const btnResetAllScores = document.getElementById('btnResetAllScores');
  const newCountryCode = document.getElementById('newCountryCode');
  const newCountryName = document.getElementById('newCountryName');
  const btnAddCountry = document.getElementById('btnAddCountry');
  const countriesManageList = document.getElementById('countriesManageList');
  const roundHistoryList = document.getElementById('roundHistoryList');
  const tabCount = document.getElementById('tabCount');
  const btnKeepScreenAwakeToggle = document.getElementById('btnKeepScreenAwakeToggle');

  // Load Saved State from localStorage
  function loadPersistedState() {
    youtubeApiKey = localStorage.getItem('flag_race_api_key') || '';
    youtubeVideoId = localStorage.getItem('flag_race_video_id') || '';
    inputApiKey.value = youtubeApiKey;
    inputVideoId.value = youtubeVideoId;

    const savedDuration = localStorage.getItem('flag_race_duration');
    if (savedDuration) {
      roundDuration = parseInt(savedDuration, 10);
      selectRoundDuration.value = roundDuration;
    }
    roundTimeRemaining = roundDuration;

    const savedGoal = localStorage.getItem('flag_race_goal');
    if (savedGoal) {
      totalDonationGoal = parseFloat(savedGoal);
      inputGoalTarget.value = totalDonationGoal;
      targetGoalAmountEl.textContent = `$${totalDonationGoal.toFixed(2)}`;
    }

    const savedRoundNum = localStorage.getItem('flag_race_round_num');
    if (savedRoundNum) {
      roundNumber = parseInt(savedRoundNum, 10);
      roundBadge.textContent = `ROUND ${roundNumber}`;
    }

    const savedHistory = localStorage.getItem('flag_race_history');
    if (savedHistory) {
      try {
        roundHistory = JSON.parse(savedHistory);
      } catch (e) {
        roundHistory = [];
      }
    }

    // Load countries or initialize
    const savedCountries = localStorage.getItem('flag_race_countries');
    if (savedCountries) {
      try {
        countries = JSON.parse(savedCountries);
      } catch (e) {
        countries = DEFAULT_COUNTRIES.map(c => ({ ...c, score: 0 }));
      }
    } else {
      countries = DEFAULT_COUNTRIES.map(c => ({ ...c, score: 0 }));
    }

    // Load scores
    countries.forEach(c => {
      const persistedScore = localStorage.getItem(`score_${c.code}`);
      c.score = persistedScore ? parseInt(persistedScore, 10) : 0;
    });

    renderCountriesManagement();
  }

  function persistCountryScore(code, score) {
    localStorage.setItem(`score_${code}`, score);
  }

  function persistCountries() {
    localStorage.setItem('flag_race_countries', JSON.stringify(countries));
    tabCount.textContent = countries.length;
    totalCountriesCount.textContent = countries.length;
  }

  // Screen Wake Lock API
  async function requestScreenWakeLock() {
    if ('wakeLock' in navigator) {
      try {
        wakeLockSentinel = await navigator.wakeLock.request('screen');
        btnKeepScreenAwakeToggle.textContent = '💡 Screen Awake: ON';
        wakeLockSentinel.addEventListener('release', () => {
          btnKeepScreenAwakeToggle.textContent = '💡 Screen Awake: OFF';
        });
      } catch (err) {
        console.warn('Wake Lock not granted:', err);
      }
    }
  }

  document.addEventListener('visibilitychange', () => {
    if (wakeLockSentinel !== null && document.visibilityState === 'visible') {
      requestScreenWakeLock();
    }
  });

  // Extract YouTube Video ID from standard YouTube URLs or direct 11-char ID
  function extractVideoId(input) {
    if (!input) return null;
    const trimmed = input.trim();
    if (/^[a-zA-Z0-9_-]{11}$/.test(trimmed)) return trimmed;
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

  // Country Name / Alias / Emoji Matcher
  function matchCountry(messageText) {
    if (!messageText) return null;
    const trimmed = messageText.trim();
    if (!trimmed) return null;

    // 1. Direct Flag Emoji match
    for (const country of countries) {
      for (const alias of country.aliases) {
        if (alias.length <= 4 && trimmed.includes(alias)) {
          return country;
        }
      }
    }

    // 2. Normalized Word & Alias match
    const normalized = trimmed.toLowerCase();
    const words = normalized.split(/[\s,;:.!?/+\-#*~()[\]{}|'"`]+/);

    for (const country of countries) {
      for (const alias of country.aliases) {
        const lowerAlias = alias.toLowerCase();
        if (words.includes(lowerAlias) || (lowerAlias.includes(' ') && normalized.includes(lowerAlias))) {
          return country;
        }
      }
      const lowerName = country.name.toLowerCase();
      if (words.includes(lowerName) || (lowerName.includes(' ') && normalized.includes(lowerName))) {
        return country;
      }
    }

    // 3. Fallback partial match (length >= 4)
    for (const country of countries) {
      const lowerName = country.name.toLowerCase();
      if (lowerName.length >= 4 && normalized.includes(lowerName)) {
        return country;
      }
    }

    return null;
  }

  // Scoring Engine
  function processMessage(authorName, rawMessage, isSuperChat = false, dollarAmount = 0.0) {
    if (!isTimerRunning && roundTimeRemaining <= 0) return;

    const matched = matchCountry(rawMessage);
    if (!matched) return; // Ignore messages with no country

    const now = Date.now();
    const lastTime = userCooldowns.get(authorName) || 0;

    // 2-second cooldown per user to limit spam (unless it is a Super Chat)
    if (!isSuperChat && now - lastTime < 2000) {
      return;
    }
    userCooldowns.set(authorName, now);

    // Calculate Points
    let pointsToAdd = 1;
    if (isSuperChat) {
      // $1 = 1,000 points
      const effectiveDollars = Math.max(1.0, dollarAmount);
      pointsToAdd = Math.floor(effectiveDollars * 1000);

      // Add to Donation Goal
      currentDonationTotal += effectiveDollars;
      currentGoalAmountEl.textContent = `$${currentDonationTotal.toFixed(2)}`;
      const progressPercent = Math.min(100, (currentDonationTotal / totalDonationGoal) * 100);
      goalProgressFill.style.width = `${progressPercent}%`;

      // Update Top Donor
      if (effectiveDollars > topDonor.amount) {
        topDonor = { name: authorName, amount: effectiveDollars };
        topDonorNameEl.textContent = `@${authorName}`;
        topDonorAmountEl.textContent = `$${effectiveDollars.toFixed(2)}`;
      }
    }

    matched.score += pointsToAdd;
    persistCountryScore(matched.code, matched.score);

    // Live Event Ticker Update
    updateTicker(authorName, matched.name, matched.code, pointsToAdd, isSuperChat, dollarAmount);

    // Refresh Leaderboard & Podium
    reRankAndRender(matched.code);
  }

  // Live Ticker Notification
  function updateTicker(author, countryName, countryCode, points, isSuperChat, dollars) {
    const flagImg = `<img src="https://flagcdn.com/w40/${countryCode}.png" style="width:16px;height:11px;vertical-align:middle;margin:0 3px;border-radius:2px;">`;
    let text = `<strong>@${author}</strong> boosted ${flagImg} <strong>${countryName}</strong> (+${points.toLocaleString()} pts)`;
    if (isSuperChat) {
      text = `⚡ <strong>@${author}</strong> sent <strong>$${dollars.toFixed(2)} Super Chat</strong> for ${flagImg} <strong>${countryName}</strong> (+${points.toLocaleString()} pts!)`;
    }
    tickerContent.innerHTML = `<span class="ticker-text">${text}</span>`;
  }

  // Re-rank & Render Scoreboard
  function reRankAndRender(highlightCode = null) {
    // Sort descending by score
    countries.sort((a, b) => b.score - a.score || a.name.localeCompare(b.name));

    // Top 3 Podium
    const c1 = countries[0] || { name: '-', score: 0, code: 'un' };
    const c2 = countries[1] || { name: '-', score: 0, code: 'un' };
    const c3 = countries[2] || { name: '-', score: 0, code: 'un' };

    name1.textContent = c1.name;
    score1.textContent = c1.score.toLocaleString();
    flag1.src = `https://flagcdn.com/w160/${c1.code}.png`;
    gap1.textContent = 'LEADER';

    name2.textContent = c2.name;
    score2.textContent = c2.score.toLocaleString();
    flag2.src = `https://flagcdn.com/w160/${c2.code}.png`;
    const gapToFirst = c1.score - c2.score;
    gap2.textContent = gapToFirst > 0 ? `-${gapToFirst.toLocaleString()} pts` : 'TIED';

    name3.textContent = c3.name;
    score3.textContent = c3.score.toLocaleString();
    flag3.src = `https://flagcdn.com/w160/${c3.code}.png`;
    const gapToSecond = c2.score - c3.score;
    gap3.textContent = gapToSecond > 0 ? `-${gapToSecond.toLocaleString()} pts` : 'TIED';

    // Grid of all countries ranked by points
    flagsGrid.innerHTML = countries.map((c, index) => `
      <div class="country-card ${highlightCode === c.code ? 'updated' : ''}" id="card-${c.code}">
        <span class="card-rank">#${index + 1}</span>
        <div class="card-flag-wrap">
          <img class="card-flag-img" src="https://flagcdn.com/w80/${c.code}.png" alt="${c.name}">
        </div>
        <span class="card-name" title="${c.name}">${c.name}</span>
        <span class="card-score">${c.score.toLocaleString()}</span>
      </div>
    `).join('');

    if (highlightCode) {
      setTimeout(() => {
        const el = document.getElementById(`card-${highlightCode}`);
        if (el) el.classList.remove('updated');
      }, 700);
    }
  }

  // Timer & Round Engine
  function startRoundTimer() {
    clearInterval(timerIntervalId);
    timerIntervalId = setInterval(() => {
      if (isTimerRunning && roundTimeRemaining > 0) {
        roundTimeRemaining--;
        updateTimerDisplay();
        if (roundTimeRemaining <= 0) {
          onRoundFinished();
        }
      }
    }, 1000);
  }

  function updateTimerDisplay() {
    const mins = Math.floor(roundTimeRemaining / 60);
    const secs = roundTimeRemaining % 60;
    timerDisplay.textContent = `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  }

  function onRoundFinished() {
    isTimerRunning = false;
    const winner = countries[0];
    if (!winner) return;

    // Trigger Winner Celebration Confetti
    startConfettiCelebration();

    // Record winner in round history
    const historyItem = {
      round: roundNumber,
      winnerName: winner.name,
      winnerCode: winner.code,
      score: winner.score,
      date: new Date().toLocaleTimeString()
    };
    roundHistory.unshift(historyItem);
    localStorage.setItem('flag_race_history', JSON.stringify(roundHistory));
    renderRoundHistory();

    // Show Winner Dialog
    winnerCountryName.textContent = winner.name;
    winnerFlagImg.src = `https://flagcdn.com/w160/${winner.code}.png`;
    winnerScore.textContent = `Winning Score: ${winner.score.toLocaleString()} pts`;
    winnerModal.classList.add('open');

    // Auto-countdown to next round (10 seconds)
    let autoNextCount = 10;
    nextRoundCountdownEl.textContent = autoNextCount;
    const autoNextTimer = setInterval(() => {
      autoNextCount--;
      nextRoundCountdownEl.textContent = autoNextCount;
      if (autoNextCount <= 0) {
        clearInterval(autoNextTimer);
        startNextRound();
      }
    }, 1000);

    btnStartNextRoundNow.onclick = () => {
      clearInterval(autoNextTimer);
      startNextRound();
    };

    btnCloseWinnerModal.onclick = () => {
      clearInterval(autoNextTimer);
      winnerModal.classList.remove('open');
      stopConfettiCelebration();
    };
  }

  function startNextRound() {
    winnerModal.classList.remove('open');
    stopConfettiCelebration();

    roundNumber++;
    roundBadge.textContent = `ROUND ${roundNumber}`;
    localStorage.setItem('flag_race_round_num', roundNumber);

    // Reset scores for fresh round competition
    countries.forEach(c => {
      c.score = 0;
      persistCountryScore(c.code, 0);
    });

    roundTimeRemaining = roundDuration;
    isTimerRunning = true;
    updateTimerDisplay();
    reRankAndRender();
  }

  // YouTube Live Poller (Client-Side Direct Fetch)
  async function connectToYouTubeLive(apiKey, videoIdOrUrl) {
    const videoId = extractVideoId(videoIdOrUrl);
    if (!videoId) {
      setStreamStatus('Invalid YouTube URL or Video ID.', true);
      return;
    }
    if (!apiKey) {
      setStreamStatus('Please provide a YouTube Data API v3 key.', true);
      return;
    }

    disconnectYouTubeLive();
    setStreamStatus('Querying YouTube Data API...', false);
    btnConnectStream.disabled = true;

    try {
      // Step 1: videos.list to find activeLiveChatId
      const videoUrl = `https://www.googleapis.com/youtube/v3/videos?id=${encodeURIComponent(videoId)}&part=snippet,liveStreamingDetails&key=${encodeURIComponent(apiKey)}`;
      const res = await fetch(videoUrl);
      if (!res.ok) {
        const errText = await res.text();
        setStreamStatus(`YouTube API Error (${res.status}): ${errText}`, true);
        btnConnectStream.disabled = false;
        return;
      }

      const data = await res.json();
      if (!data.items || data.items.length === 0) {
        setStreamStatus('Live video not found. Verify the video link.', true);
        btnConnectStream.disabled = false;
        return;
      }

      const item = data.items[0];
      const liveChatId = item.liveStreamingDetails?.activeLiveChatId;
      if (!liveChatId) {
        setStreamStatus('Live chat is not active or video is not currently live.', true);
        btnConnectStream.disabled = false;
        return;
      }

      activeLiveChatId = liveChatId;
      youtubeApiKey = apiKey;
      youtubeVideoId = videoId;
      isYouTubePolling = true;

      // Save to localStorage
      localStorage.setItem('flag_race_api_key', apiKey);
      localStorage.setItem('flag_race_video_id', videoId);

      // Stop test mode
      toggleTestMode(false);

      setStreamStatus(`Connected! Stream: "${item.snippet?.title || 'Live'}"`, false);
      livePill.classList.add('live-active');
      liveStatusText.textContent = 'YOUTUBE LIVE';
      btnDisconnectStream.style.display = 'inline-flex';
      btnConnectStream.disabled = false;

      // Start Polling Chat
      processedMessageIds.clear();
      nextPageToken = null;
      pollLiveChat();

      setTimeout(() => setupModal.classList.remove('open'), 1200);
    } catch (err) {
      setStreamStatus(`Connection error: ${err.message}`, true);
      btnConnectStream.disabled = false;
    }
  }

  async function pollLiveChat() {
    if (!isYouTubePolling || !activeLiveChatId || !youtubeApiKey) return;

    let pollInterval = 2500;

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

              const author = item.authorDetails?.displayName || 'Viewer';
              const type = item.snippet?.type || 'textMessageEvent';
              const message = item.snippet?.displayMessage || '';

              if (type === 'superChatEvent') {
                const sc = item.snippet?.superChatDetails;
                const amountMicros = sc?.amountMicros || 1000000;
                const currency = sc?.currency || 'USD';
                const rate = CURRENCY_TO_USD[currency] || 1.0;
                const rawAmount = amountMicros / 1000000.0;
                const dollars = rawAmount * rate;
                const userComment = sc?.userComment || message;

                processMessage(author, userComment, true, dollars);
              } else {
                processMessage(author, message, false, 0);
              }
            }
          }
        }
      } else {
        pollInterval = 4000; // Backoff on error
      }
    } catch (err) {
      pollInterval = 4000;
    }

    if (isYouTubePolling) {
      pollingTimeoutId = setTimeout(pollLiveChat, pollInterval);
    }
  }

  function disconnectYouTubeLive() {
    isYouTubePolling = false;
    if (pollingTimeoutId) {
      clearTimeout(pollingTimeoutId);
      pollingTimeoutId = null;
    }
    activeLiveChatId = null;
    livePill.classList.remove('live-active');
    liveStatusText.textContent = isTestMode ? 'TEST MODE' : 'STANDBY';
    btnDisconnectStream.style.display = 'none';
    setStreamStatus('Disconnected from YouTube Live.', false);
  }

  function setStreamStatus(msg, isError) {
    streamStatusBox.textContent = msg;
    streamStatusBox.style.color = isError ? 'var(--danger-red)' : 'var(--neon-green)';
  }

  // Offline Test Mode Simulator
  const sampleUsers = [
    'AlexGamer', 'Priya_IN', 'CanTurk', 'Lucas_BR', 'Budi_ID',
    'Ali_PK', 'Maria_PH', 'Max_DE', 'James_UK', 'Kenji_JP',
    'Mateo_MX', 'Linh_VN', 'Ahmed_EG', 'Leo_AR', 'Sophie_FR'
  ];

  function toggleTestMode(enable) {
    isTestMode = enable !== undefined ? enable : !isTestMode;

    if (testModeIntervalId) {
      clearInterval(testModeIntervalId);
      testModeIntervalId = null;
    }

    if (isTestMode) {
      livePill.classList.remove('live-active');
      liveStatusText.textContent = 'TEST MODE';
      btnToggleSimulate.style.borderColor = 'var(--neon-gold)';

      // Simulate chat messages every 600ms - 1100ms
      testModeIntervalId = setInterval(() => {
        if (!isTimerRunning) return;
        const user = sampleUsers[Math.floor(Math.random() * sampleUsers.length)];
        const country = countries[Math.floor(Math.random() * Math.min(16, countries.length))];
        if (!country) return;

        // 6% chance for Super Chat
        const isSuperChat = Math.random() < 0.06;
        if (isSuperChat) {
          const dollarAmount = [2.0, 5.0, 10.0, 20.0][Math.floor(Math.random() * 4)];
          processMessage(user, `${country.name} to the top! 🔥`, true, dollarAmount);
        } else {
          // Standard comment
          const commentVariants = [
            country.name,
            country.aliases[0] || country.name,
            `${country.name} win!`,
            `+1 ${country.name}`
          ];
          const text = commentVariants[Math.floor(Math.random() * commentVariants.length)];
          processMessage(user, text, false, 0);
        }
      }, 750);
    } else {
      liveStatusText.textContent = 'STANDBY';
      btnToggleSimulate.style.borderColor = 'rgba(255,255,255,0.15)';
    }
  }

  // Countries Management inside Setup Modal
  function renderCountriesManagement() {
    tabCount.textContent = countries.length;
    totalCountriesCount.textContent = countries.length;

    countriesManageList.innerHTML = countries.map(c => `
      <div class="country-manage-item">
        <div class="c-left">
          <img class="c-flag-tiny" src="https://flagcdn.com/w40/${c.code}.png" alt="${c.name}">
          <span><strong>${c.name}</strong> (${c.code.toUpperCase()})</span>
        </div>
        <button class="btn btn-sm btn-danger" onclick="window.removeCountry('${c.code}')">Remove</button>
      </div>
    `).join('');
  }

  window.removeCountry = function (code) {
    countries = countries.filter(c => c.code !== code);
    persistCountries();
    renderCountriesManagement();
    reRankAndRender();
  };

  btnAddCountry.addEventListener('click', () => {
    const code = newCountryCode.value.trim().toLowerCase();
    const name = newCountryName.value.trim();

    if (!code || code.length !== 2) {
      alert('Please enter a valid 2-letter ISO country code (e.g. no, dk, se).');
      return;
    }
    if (!name) {
      alert('Please enter the country name.');
      return;
    }
    if (countries.some(c => c.code === code)) {
      alert('Country code already exists.');
      return;
    }

    countries.push({
      code,
      name,
      aliases: [name.toLowerCase(), code],
      score: 0
    });

    newCountryCode.value = '';
    newCountryName.value = '';
    persistCountries();
    renderCountriesManagement();
    reRankAndRender();
  });

  function renderRoundHistory() {
    if (roundHistory.length === 0) {
      roundHistoryList.innerHTML = '<div class="empty-state">No completed rounds yet.</div>';
      return;
    }

    roundHistoryList.innerHTML = roundHistory.map(h => `
      <div class="country-manage-item">
        <div class="c-left">
          <img class="c-flag-tiny" src="https://flagcdn.com/w40/${h.winnerCode}.png" alt="${h.winnerName}">
          <span><strong>Round ${h.round}:</strong> ${h.winnerName}</span>
        </div>
        <span style="color:var(--neon-cyan);font-weight:700;">${h.score.toLocaleString()} pts</span>
      </div>
    `).join('');
  }

  // Confetti Particle System
  const confettiCanvas = document.getElementById('confettiCanvas');
  const confCtx = confettiCanvas.getContext('2d');
  let confettiParticles = [];
  let confettiAnimationId = null;

  function resizeConfetti() {
    confettiCanvas.width = window.innerWidth;
    confettiCanvas.height = window.innerHeight;
  }
  window.addEventListener('resize', resizeConfetti);
  resizeConfetti();

  function startConfettiCelebration() {
    confettiParticles = [];
    const colors = ['#FFD700', '#FF007F', '#00F0FF', '#00FF88', '#FFAA00', '#FFFFFF'];
    for (let i = 0; i < 160; i++) {
      confettiParticles.push({
        x: Math.random() * confettiCanvas.width,
        y: -20 - Math.random() * 100,
        vx: (Math.random() - 0.5) * 6,
        vy: 3 + Math.random() * 6,
        size: 6 + Math.random() * 8,
        color: colors[Math.floor(Math.random() * colors.length)],
        rotation: Math.random() * 360,
        rotSpeed: (Math.random() - 0.5) * 10
      });
    }

    function renderConfetti() {
      confCtx.clearRect(0, 0, confettiCanvas.width, confettiCanvas.height);
      confettiParticles.forEach(p => {
        p.x += p.vx;
        p.y += p.vy;
        p.rotation += p.rotSpeed;

        if (p.y > confettiCanvas.height) {
          p.y = -10;
          p.x = Math.random() * confettiCanvas.width;
        }

        confCtx.save();
        confCtx.translate(p.x, p.y);
        confCtx.rotate((p.rotation * Math.PI) / 180);
        confCtx.fillStyle = p.color;
        confCtx.fillRect(-p.size / 2, -p.size / 2, p.size, p.size * 0.6);
        confCtx.restore();
      });

      confettiAnimationId = requestAnimationFrame(renderConfetti);
    }

    cancelAnimationFrame(confettiAnimationId);
    renderConfetti();
  }

  function stopConfettiCelebration() {
    cancelAnimationFrame(confettiAnimationId);
    confCtx.clearRect(0, 0, confettiCanvas.width, confettiCanvas.height);
  }

  // Event Listeners
  btnToggleSimulate.addEventListener('click', () => {
    toggleTestMode(!isTestMode);
  });

  btnOpenSetup.addEventListener('click', () => {
    setupModal.classList.add('open');
  });

  btnCloseSetup.addEventListener('click', () => {
    setupModal.classList.remove('open');
  });

  btnSaveAndClose.addEventListener('click', () => {
    setupModal.classList.remove('open');
  });

  // Modal Tabs
  document.querySelectorAll('.modal-tab').forEach(tab => {
    tab.addEventListener('click', () => {
      document.querySelectorAll('.modal-tab').forEach(t => t.classList.remove('active'));
      document.querySelectorAll('.tab-pane').forEach(p => p.classList.remove('active'));
      tab.classList.add('active');
      const targetPane = document.getElementById(tab.getAttribute('data-tab'));
      if (targetPane) targetPane.classList.add('active');
    });
  });

  // Clean Screen Mode (Broadcast Ready)
  btnToggleCleanMode.addEventListener('click', () => {
    document.body.classList.add('clean-mode');
    if (document.documentElement.requestFullscreen) {
      document.documentElement.requestFullscreen().catch(() => {});
    }
  });

  btnExitCleanMode.addEventListener('click', () => {
    document.body.classList.remove('clean-mode');
  });

  // Stream Connect Buttons
  btnConnectStream.addEventListener('click', () => {
    connectToYouTubeLive(inputApiKey.value.trim(), inputVideoId.value.trim());
  });

  btnDisconnectStream.addEventListener('click', () => {
    disconnectYouTubeLive();
  });

  // Timer & Goal Settings
  selectRoundDuration.addEventListener('change', () => {
    roundDuration = parseInt(selectRoundDuration.value, 10);
    localStorage.setItem('flag_race_duration', roundDuration);
    roundTimeRemaining = roundDuration;
    updateTimerDisplay();
  });

  inputGoalTarget.addEventListener('change', () => {
    totalDonationGoal = parseFloat(inputGoalTarget.value) || 50.0;
    localStorage.setItem('flag_race_goal', totalDonationGoal);
    targetGoalAmountEl.textContent = `$${totalDonationGoal.toFixed(2)}`;
    const progressPercent = Math.min(100, (currentDonationTotal / totalDonationGoal) * 100);
    goalProgressFill.style.width = `${progressPercent}%`;
  });

  btnToggleTimer.addEventListener('click', () => {
    isTimerRunning = !isTimerRunning;
    btnToggleTimer.textContent = isTimerRunning ? '⏸ Pause Timer' : '▶ Resume Timer';
  });

  btnAddExtraMinute.addEventListener('click', () => {
    roundTimeRemaining += 60;
    updateTimerDisplay();
  });

  btnRestartRound.addEventListener('click', () => {
    roundTimeRemaining = roundDuration;
    isTimerRunning = true;
    updateTimerDisplay();
  });

  btnResetAllScores.addEventListener('click', () => {
    if (confirm('Reset all country scores to 0?')) {
      countries.forEach(c => {
        c.score = 0;
        persistCountryScore(c.code, 0);
      });
      currentDonationTotal = 0;
      currentGoalAmountEl.textContent = '$0.00';
      goalProgressFill.style.width = '0%';
      topDonor = { name: 'None', amount: 0.0 };
      topDonorNameEl.textContent = 'Waiting for donor...';
      topDonorAmountEl.textContent = '$0.00';
      reRankAndRender();
    }
  });

  btnKeepScreenAwakeToggle.addEventListener('click', () => {
    requestScreenWakeLock();
  });

  // Initialization
  loadPersistedState();
  updateTimerDisplay();
  startRoundTimer();
  reRankAndRender();
  renderRoundHistory();
  toggleTestMode(true); // Start in test mode immediately
  requestScreenWakeLock();
})();
