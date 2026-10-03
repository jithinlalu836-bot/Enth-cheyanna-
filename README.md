# 🎮 YouTube Live Chat Boss Fight - Stream Overlay Game

An interactive, live-streaming game controlled by your **YouTube Live chat comments**. Viewers battle an evolving boss on screen in real time during your stream!

Designed specifically for **OBS Studio Browser Source** with a 100% transparent overlay option.

---

## ⚡ Features

- **Viewer Interaction**: Viewers join automatically on their very first command!
- **Supported Commands**:
  - `!attack` / `!a`: Dash forward, slash weapon, damage boss with floating numbers & critical hits!
  - `!jump` / `!j`: Leap high into the air with dust trails.
  - `!left` / `!l`: Move left.
  - `!right` / `!r`: Move right toward the boss.
- **Boss Fight**:
  - Top Boss HP bar with damage-lag animation.
  - Defeating the boss triggers a celebratory **"LEVEL UP!"** sequence.
  - The boss respawns stronger with new visual forms (Inferno Golem, Shadow Wyvern, Cyber Colossus, Chaos Overlord).
- **Damage Leaderboard**: Top 5 viewers dynamically ranked by total damage dealt with 🥇 🥈 🥉 medals.
- **Live Command Feed**: Shows the latest 5 viewer commands as an animated stream ticker.
- **Anti-Spam Protection**: 1 command per viewer every 1 second (respects YouTube API quotas).
- **Test Mode**: Built-in test simulator that generates fake viewers and commands so you can test and preview without going live!

---

## 🚀 Quick Setup Guide

### Step 1: Install Dependencies
Ensure you have [Node.js](https://nodejs.org/) installed (v18+). Run:
```bash
npm install
```

### Step 2: Configure Environment (.env)
Create or edit your `.env` file in the project root:
```env
# Your YouTube Data API v3 Key (from Google Cloud Console)
YOUTUBE_API_KEY=YOUR_YOUTUBE_API_KEY_HERE

# Optional: Pre-fill your Live Stream URL or Video ID
# Example: https://youtube.com/live/abc123xyz or abc123xyz
YOUTUBE_STREAM_ID=

# Server Port (default 3000)
PORT=3000
```

#### How to get a free YouTube Data API v3 Key:
1. Go to the [Google Cloud Console](https://console.cloud.google.com/).
2. Create a project (or select an existing one).
3. Navigate to **APIs & Services > Library** and search for **YouTube Data API v3**. Click **Enable**.
4. Navigate to **APIs & Services > Credentials** and click **Create Credentials > API Key**.
5. Copy your key and paste it into `.env` as `YOUTUBE_API_KEY=...`.

---

### Step 3: Run the Application
Start the Node.js server:
```bash
npm start
```
Open your browser and navigate to:
```
http://localhost:3000
```
> **Note**: Test Mode activates automatically on startup! You can immediately watch simulated viewers fight the boss.

---

### Step 4: Add to OBS Studio (Browser Source)

1. Open **OBS Studio**.
2. Under **Sources**, click the **`+`** icon and select **Browser**.
3. Name it `YouTube Chat Boss Fight`.
4. Configure the Browser Source settings:
   - **URL**: `http://localhost:3000/?obs=true&transparent=true`
   - **Width**: `1920`
   - **Height**: `1080`
   - **Custom CSS**: Leave empty or default
   - Check **Shutdown source when not visible**
   - Check **Refresh browser when scene becomes active**
5. Click **OK**. The game will now float seamlessly with a **transparent background** over your live gameplay or camera!

---

### Step 5: Connect Your Real YouTube Live Stream

1. Start your YouTube Live stream in YouTube Studio.
2. In the game app (open `http://localhost:3000` in your browser):
   - Click the red **"🔴 Link YouTube Live"** button.
   - Enter your Live Stream URL (e.g. `https://youtube.com/live/xxxxxxxxxxx`) or Video ID.
   - Click **Start Reading Live Chat**.
3. The game will automatically:
   - Locate your active `liveChatId` via the YouTube Data API.
   - Turn off Test Mode.
   - Poll incoming live chat messages at the recommended `pollingIntervalMillis`.
   - Filter commands and spawn your real viewers on screen!
