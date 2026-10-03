# 🏁 Flag Race - Interactive YouTube Livestream Game

A single-app interactive **"Flag Race"** game show built for mobile (portrait 9:16 layout) designed to be screen-shared into a YouTube live stream or added to OBS Studio.

Viewers type their country name (or flag emoji) in your YouTube live chat, boosting their country's score on a live animated scoreboard. Super Chats grant huge point multipliers ($1 = 1,000 points)!

---

## 🌟 Key Features

1. **Mobile-First Portrait (9:16) Layout**:
   - Tailored for full-screen phone display, mobile live streaming, and screen-sharing into YouTube Live.
   - **Keep Screen Awake**: Integrated with the Screen Wake Lock API so your device never dims or sleeps mid-broadcast.
   - **Clean Screen Mode (`📺`)**: One tap hides all configuration buttons and headers for a pristine, distraction-free broadcast overlay.
2. **Real-Time YouTube Chat Reader**:
   - Queries `videos.list` (with `liveStreamingDetails`) to locate `activeLiveChatId`.
   - Polls `liveChatMessages.list` respecting `pollingIntervalMillis` returned by YouTube to prevent quota drain.
   - Auto-retry with backoff on network hiccups.
3. **Smart Country Matching & Anti-Spam**:
   - Case-insensitive country name detection (e.g. `India`, `Turkey`, `USA`, `Brazil`, etc.).
   - Aliases support (e.g. `bharat`, `hindustan`, `turkiye`, `america`, `england`, etc.) and flag emojis (🇮🇳, 🇹🇷, 🇺🇸, 🇧🇷, etc.).
   - **2-second cooldown per viewer** to prevent spam flooding.
4. **Super Chat Multiplier ($1 = 1,000 PTS)**:
   - Super Chats (`superChatEvent`) grant 1,000 points per dollar.
   - Non-USD currencies (EUR, GBP, INR, BRL, TRY, JPY, etc.) automatically converted to USD.
   - Tracks **Donation Goal Progress** (e.g. `$14.50 / $50.00`) and highlights the **Top Donor**.
5. **Game-Show Scoreboard**:
   - **Top 3 Podium**: 1st Place Champion (elevated with crown 👑), 2nd Place Silver, 3rd Place Bronze with point gap indicators.
   - **Flagcdn.com Images**: Crisp flags rendered above scores (no broken system emojis).
   - **Live Event Ticker**: `@viewer -> 🇮🇳 India (+1 pt)` ticker sliding in real time.
6. **Round System**:
   - Configurable round timer (3m, 5m, 10m, 15m, 20m) with countdown readout.
   - Confetti particle celebration when a round ends, crowning the champion.
   - All-time round history saved in `localStorage`.
7. **Offline Test Mode (`🤖`)**:
   - Instant "Simulate Chat" toggle generates simulated viewer comments and Super Chats for instant testing without going live.

---

## 🚀 How to Run

### Option A: Mobile Phone (PWA / Browser)
1. Open the app URL in your mobile browser (e.g. Chrome on Android or Safari on iOS):
   ```
   https://ais-dev-kzy6ywgmyfsn5w7k6jjl4f-109939089217.asia-east1.run.app/
   ```
2. Tap **"Add to Home Screen"** to install it as a standalone full-screen PWA.
3. Tap the **Clean Screen (`📺`)** button to hide all controls.
4. Start screen sharing from your phone directly to YouTube Live via the YouTube App or Prism Live Studio!

### Option B: Local / OBS Studio
1. Start the local server:
   ```bash
   node server.js
   ```
2. In OBS Studio, add a **Browser Source**:
   - **URL**: `http://localhost:3000/`
   - **Width**: `1080`
   - **Height**: `1920` (or `540` x `960` for portrait screen-share)
3. Click the **Clean Screen Mode** button to display only the live game show.

---

## 🔑 YouTube Data API v3 Setup Guide

### 1. Get a YouTube Data API v3 Key
1. Go to the [Google Cloud Console](https://console.cloud.google.com/).
2. Create a new project (e.g., "YouTube Flag Race").
3. Navigate to **APIs & Services > Library**, search for **YouTube Data API v3**, and click **Enable**.
4. Go to **APIs & Services > Credentials**, click **Create Credentials > API key**.
5. Copy your API Key (starts with `AIzaSy...`).

### 2. Find Your Live Video ID
1. Go to [YouTube Studio](https://studio.youtube.com/) and create a live stream (or start a stream).
2. The URL of your stream will look like:
   - `https://www.youtube.com/watch?v=dQw4w9WgXcQ`
   - or `https://youtube.com/live/dQw4w9WgXcQ`
3. Your **Video ID** is the 11-character code at the end (e.g. `dQw4w9WgXcQ`).

### 3. Connect in Flag Race
1. Open Flag Race and tap the **Settings (`⚙️`)** icon.
2. Paste your **API Key** and **Video ID** (or full stream URL).
3. Tap **Connect Live Stream**.
4. Flag Race will automatically find your `activeLiveChatId`, deactivate test mode, and start processing live chat comments!
