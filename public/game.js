// YouTube Live Chat Boss Fight - Stream Overlay Engine
// Virtual 1080p coordinate system (1920 x 1080)

(function () {
  const canvas = document.getElementById('gameCanvas');
  const ctx = canvas.getContext('2d');

  // Virtual Game Resolution
  const V_WIDTH = 1920;
  const V_HEIGHT = 1080;
  let scale = 1;
  let offsetX = 0;
  let offsetY = 0;

  // Ground level
  const GROUND_Y = 820;

  // Game State
  const players = new Map(); // userId -> Player
  let damageNumbers = [];
  let particles = [];
  let slashEffects = [];
  let screenShake = 0;
  let isTransparent = false;

  // Boss Definition
  const bossTypes = [
    { name: 'INFERNO GOLEM', maxHp: 1000, color: '#FF3344', glow: '#FF6600', scale: 1.0, title: 'Flame Titan' },
    { name: 'SHADOW WYVERN', maxHp: 1600, color: '#8B5CF6', glow: '#EC4899', scale: 1.15, title: 'Void Beast' },
    { name: 'CYBER COLOSSUS', maxHp: 2400, color: '#00F0FF', glow: '#3B82F6', scale: 1.25, title: 'Plasma Mech' },
    { name: 'CHAOS OVERLORD', maxHp: 3500, color: '#FFD700', glow: '#FF0055', scale: 1.35, title: 'Demon King' }
  ];

  let bossLevel = 1;
  let currentBossType = bossTypes[0];
  let boss = {
    x: 1400,
    y: GROUND_Y - 180,
    width: 220,
    height: 280,
    hp: currentBossType.maxHp,
    maxHp: currentBossType.maxHp,
    targetHp: currentBossType.maxHp,
    isHurt: false,
    hurtTimer: 0,
    floatOffset: 0,
    floatSpeed: 0.04,
    breathScale: 1,
    isDead: false,
    deathTimer: 0
  };

  // Recent Command Feed (Max 5)
  const recentCommands = [];

  // WebSocket Connection
  let ws = null;
  let wsReconnectTimer = null;

  // DOM Elements
  const bossNameEl = document.getElementById('bossName');
  const bossLevelTagEl = document.getElementById('bossLevelTag');
  const bossCurrentHpEl = document.getElementById('bossCurrentHp');
  const bossMaxHpEl = document.getElementById('bossMaxHp');
  const hpBarFillEl = document.getElementById('hpBarFill');
  const hpBarLagEl = document.getElementById('hpBarLag');
  const leaderboardListEl = document.getElementById('leaderboardList');
  const commandFeedListEl = document.getElementById('commandFeedList');
  const announcementOverlay = document.getElementById('announcementOverlay');
  const announceTitleEl = document.getElementById('announceTitle');
  const announceSubEl = document.getElementById('announceSub');
  const statusPill = document.getElementById('statusPill');
  const statusIcon = document.getElementById('statusIcon');
  const statusText = document.getElementById('statusText');
  const btnToggleTestMode = document.getElementById('btnToggleTestMode');
  const testModeStateText = document.getElementById('testModeStateText');
  const btnToggleTransparent = document.getElementById('btnToggleTransparent');
  const btnOpenConnectModal = document.getElementById('btnOpenConnectModal');
  const connectModal = document.getElementById('connectModal');
  const btnCloseConnectModal = document.getElementById('btnCloseConnectModal');
  const btnConnectStream = document.getElementById('btnConnectStream');
  const btnDisconnectStream = document.getElementById('btnDisconnectStream');
  const inputStreamUrl = document.getElementById('inputStreamUrl');
  const inputApiKey = document.getElementById('inputApiKey');
  const modalStatusBox = document.getElementById('modalStatusBox');

  // Handle URL Parameters (e.g. ?obs=true&transparent=true)
  const urlParams = new URLSearchParams(window.location.search);
  if (urlParams.get('obs') === 'true' || urlParams.get('obs') === '1') {
    document.body.classList.add('obs-mode');
  }
  if (urlParams.get('transparent') === 'true' || urlParams.get('transparent') === '1') {
    setTransparent(true);
  }

  function setTransparent(transparent) {
    isTransparent = transparent;
    if (isTransparent) {
      document.body.classList.add('transparent-bg');
      btnToggleTransparent.classList.add('btn-action');
      btnToggleTransparent.innerHTML = '<span class="icon">⬛</span> Solid BG';
    } else {
      document.body.classList.remove('transparent-bg');
      btnToggleTransparent.classList.remove('btn-action');
      btnToggleTransparent.innerHTML = '<span class="icon">🏁</span> Transparent BG';
    }
  }

  btnToggleTransparent.addEventListener('click', () => {
    setTransparent(!isTransparent);
  });

  // Responsive Canvas Sizing
  function resizeCanvas() {
    const w = window.innerWidth;
    const h = window.innerHeight;
    canvas.width = w;
    canvas.height = h;

    // Maintain 1920x1080 aspect ratio mapping
    scale = Math.min(w / V_WIDTH, h / V_HEIGHT);
    offsetX = (w - V_WIDTH * scale) / 2;
    offsetY = (h - V_HEIGHT * scale) / 2;
  }
  window.addEventListener('resize', resizeCanvas);
  resizeCanvas();

  // Color generator for player avatars based on username
  function stringToColor(str) {
    let hash = 0;
    for (let i = 0; i < str.length; i++) {
      hash = str.charCodeAt(i) + ((hash << 5) - hash);
    }
    const colors = [
      '#00F0FF', '#FF007F', '#00FF88', '#FFD700',
      '#A855F7', '#FF7700', '#38BDF8', '#F43F5E',
      '#10B981', '#F59E0B', '#6366F1', '#EC4899'
    ];
    return colors[Math.abs(hash) % colors.length];
  }

  // Player Entity Class
  class Player {
    constructor(username, userId) {
      this.username = username;
      this.userId = userId;
      this.color = stringToColor(username);
      this.x = 200 + Math.random() * 450;
      this.y = GROUND_Y;
      this.vx = 0;
      this.vy = 0;
      this.isGrounded = true;
      this.facing = 1; // 1 = right, -1 = left
      this.state = 'idle'; // idle, walk, jump, attack
      this.stateTimer = 0;
      this.totalDamage = 0;
      this.lastActionTime = Date.now();
      this.characterType = Math.abs(username.charCodeAt(0) || 0) % 3; // 0=Knight, 1=Ninja, 2=Mage
      this.legCycle = 0;
    }

    executeCommand(command, isSuperChat = false, dollarAmount = 0) {
      this.lastActionTime = Date.now();

      switch (command) {
        case 'left':
          this.vx = -12;
          this.facing = -1;
          this.state = 'walk';
          this.stateTimer = 20;
          createDustParticles(this.x, this.y);
          break;

        case 'right':
          this.vx = 12;
          this.facing = 1;
          this.state = 'walk';
          this.stateTimer = 20;
          createDustParticles(this.x, this.y);
          break;

        case 'jump':
          if (this.isGrounded) {
            this.vy = -20;
            this.isGrounded = false;
            this.state = 'jump';
            createDustParticles(this.x, this.y, 8);
          }
          break;

        case 'attack':
          this.state = 'attack';
          this.stateTimer = 22;
          // Dash toward boss
          const dirToBoss = boss.x > this.x ? 1 : -1;
          this.facing = dirToBoss;
          this.vx = dirToBoss * 16;

          // Create weapon slash arc
          slashEffects.push({
            x: this.x + this.facing * 50,
            y: this.y - 40,
            facing: this.facing,
            color: isSuperChat ? '#FFD700' : this.color,
            life: 14,
            maxLife: 14
          });

          // Check hit on boss
          const distanceToBoss = Math.abs((this.x + this.facing * 80) - (boss.x + boss.width / 2));
          if (distanceToBoss < 380 && !boss.isDead) {
            // Calculate Damage
            let baseDamage = Math.floor(22 + Math.random() * 16);
            let isCrit = Math.random() < 0.22;
            if (isCrit) baseDamage = Math.floor(baseDamage * 1.8);

            if (isSuperChat) {
              baseDamage = Math.floor(100 * Math.max(1, dollarAmount));
            }

            applyDamageToBoss(baseDamage, this, isCrit, isSuperChat);
          }
          break;
      }
    }

    update() {
      // Physics gravity
      if (!this.isGrounded) {
        this.vy += 0.95;
        this.y += this.vy;
        if (this.y >= GROUND_Y) {
          this.y = GROUND_Y;
          this.vy = 0;
          this.isGrounded = true;
          if (this.state === 'jump') this.state = 'idle';
          createDustParticles(this.x, this.y, 4);
        }
      }

      // Horizontal movement & friction
      this.x += this.vx;
      this.vx *= 0.88;
      if (Math.abs(this.vx) < 0.2) this.vx = 0;

      // Keep player inside arena boundaries
      if (this.x < 80) this.x = 80;
      if (this.x > boss.x + 80) this.x = boss.x + 80;

      if (this.stateTimer > 0) {
        this.stateTimer--;
        if (this.stateTimer === 0 && this.isGrounded) {
          this.state = 'idle';
        }
      }

      if (Math.abs(this.vx) > 0.5) {
        this.legCycle += 0.25;
      } else {
        this.legCycle = 0;
      }
    }

    draw(ctx) {
      ctx.save();
      ctx.translate(this.x, this.y);

      // Player Name tag floating above
      ctx.font = 'bold 15px Chakra Petch';
      ctx.textAlign = 'center';
      const nameWidth = ctx.measureText(this.username).width;

      // Name background pill
      ctx.fillStyle = 'rgba(10, 15, 30, 0.8)';
      ctx.beginPath();
      ctx.roundRect(-nameWidth / 2 - 8, -82, nameWidth + 16, 22, 6);
      ctx.fill();
      ctx.strokeStyle = this.color;
      ctx.lineWidth = 1.5;
      ctx.stroke();

      // Name text
      ctx.fillStyle = '#FFFFFF';
      ctx.fillText(this.username, 0, -66);

      // Facing orientation
      ctx.scale(this.facing, 1);

      // Shadow on ground
      ctx.fillStyle = 'rgba(0, 0, 0, 0.35)';
      ctx.beginPath();
      ctx.ellipse(0, 0, 22, 6, 0, 0, Math.PI * 2);
      ctx.fill();

      // Body Drawing
      const bob = Math.sin(this.legCycle) * 3;

      // Legs
      ctx.strokeStyle = '#1E293B';
      ctx.lineWidth = 5;
      ctx.lineCap = 'round';
      const leg1Angle = Math.sin(this.legCycle) * 12;
      const leg2Angle = -Math.sin(this.legCycle) * 12;

      ctx.beginPath();
      ctx.moveTo(-6, -14);
      ctx.lineTo(-6 + leg1Angle, 0);
      ctx.moveTo(6, -14);
      ctx.lineTo(6 + leg2Angle, 0);
      ctx.stroke();

      // Torso / Armor
      ctx.fillStyle = this.color;
      ctx.beginPath();
      ctx.roundRect(-12, -38 + bob, 24, 26, 5);
      ctx.fill();
      ctx.strokeStyle = '#0F172A';
      ctx.lineWidth = 2;
      ctx.stroke();

      // Head
      ctx.fillStyle = '#FFE0BD';
      ctx.beginPath();
      ctx.arc(0, -48 + bob, 12, 0, Math.PI * 2);
      ctx.fill();

      // Helmet / Hair
      ctx.fillStyle = this.characterType === 0 ? '#475569' : this.characterType === 1 ? '#0F172A' : '#7C3AED';
      ctx.beginPath();
      ctx.arc(0, -51 + bob, 13, Math.PI, Math.PI * 2);
      ctx.fill();

      // Eye
      ctx.fillStyle = '#0F172A';
      ctx.beginPath();
      ctx.arc(4, -48 + bob, 2.5, 0, Math.PI * 2);
      ctx.fill();

      // Weapon
      if (this.state === 'attack') {
        // Swinging weapon forward
        ctx.save();
        ctx.translate(14, -28 + bob);
        ctx.rotate(0.6);
        ctx.fillStyle = '#E2E8F0';
        ctx.fillRect(0, -5, 36, 6); // Sword blade
        ctx.fillStyle = '#F59E0B';
        ctx.fillRect(-6, -7, 6, 10); // Hilt
        ctx.restore();
      } else {
        // Weapon in hand / idle
        ctx.fillStyle = '#94A3B8';
        ctx.fillRect(10, -32 + bob, 4, 24);
        ctx.fillStyle = '#F59E0B';
        ctx.fillRect(8, -26 + bob, 8, 4);
      }

      ctx.restore();
    }
  }

  // Damage Application on Boss
  function applyDamageToBoss(amount, player, isCrit = false, isSuperChat = false) {
    if (boss.isDead) return;

    boss.hp = Math.max(0, boss.hp - amount);
    player.totalDamage += amount;
    boss.isHurt = true;
    boss.hurtTimer = 10;
    screenShake = isSuperChat ? 18 : isCrit ? 10 : 5;

    // Spawn floating damage text
    damageNumbers.push({
      x: boss.x + 40 + (Math.random() * 120),
      y: boss.y + 60 + (Math.random() * 80),
      text: isSuperChat ? `SUPER CHAT! -${amount}` : isCrit ? `CRIT! -${amount}` : `-${amount}`,
      color: isSuperChat ? '#FFD700' : isCrit ? '#FF007F' : '#00F0FF',
      scale: isSuperChat ? 1.6 : isCrit ? 1.3 : 1.0,
      alpha: 1.0,
      vy: -2.5
    });

    // Spawn hit spark particles
    createHitSparks(boss.x + 80, boss.y + 120, isSuperChat ? 25 : 12, isSuperChat ? '#FFD700' : player.color);

    // Update Boss UI
    updateBossHpUI();
    updateLeaderboardUI();

    // Check Boss Defeat
    if (boss.hp <= 0 && !boss.isDead) {
      bossDefeated();
    }
  }

  // Boss Level Up & Respawn
  function bossDefeated() {
    boss.isDead = true;
    boss.deathTimer = 120;
    screenShake = 22;

    // Massive particle explosion
    createHitSparks(boss.x + boss.width / 2, boss.y + boss.height / 2, 80, '#FFD700');
    createHitSparks(boss.x + boss.width / 2, boss.y + boss.height / 2, 80, '#FF0055');

    // Show Level Up Announcement
    announceTitleEl.textContent = `LEVEL UP!`;
    announceSubEl.textContent = `${currentBossType.name} DEFEATED! ADVANCING TO LEVEL ${bossLevel + 1}...`;
    announcementOverlay.classList.add('show');

    setTimeout(() => {
      announcementOverlay.classList.remove('show');
      respawnNextBoss();
    }, 3800);
  }

  function respawnNextBoss() {
    bossLevel++;
    currentBossType = bossTypes[(bossLevel - 1) % bossTypes.length];
    const scaledMaxHp = Math.floor(currentBossType.maxHp * (1 + (bossLevel - 1) * 0.4));

    boss.maxHp = scaledMaxHp;
    boss.hp = scaledMaxHp;
    boss.targetHp = scaledMaxHp;
    boss.isDead = false;
    boss.deathTimer = 0;

    bossNameEl.textContent = currentBossType.name;
    bossLevelTagEl.textContent = `LVL ${bossLevel}`;
    updateBossHpUI();

    // Spawn entrance burst
    createHitSparks(boss.x + boss.width / 2, boss.y + boss.height / 2, 50, currentBossType.color);
  }

  // Particle Generators
  function createDustParticles(x, y, count = 5) {
    for (let i = 0; i < count; i++) {
      particles.push({
        x: x + (Math.random() - 0.5) * 20,
        y: y - 2,
        vx: (Math.random() - 0.5) * 4,
        vy: -Math.random() * 2,
        size: 3 + Math.random() * 4,
        color: 'rgba(200, 210, 230, 0.6)',
        alpha: 1,
        decay: 0.05
      });
    }
  }

  function createHitSparks(x, y, count = 12, color = '#00F0FF') {
    for (let i = 0; i < count; i++) {
      const angle = Math.random() * Math.PI * 2;
      const speed = 4 + Math.random() * 10;
      particles.push({
        x,
        y,
        vx: Math.cos(angle) * speed,
        vy: Math.sin(angle) * speed,
        size: 2 + Math.random() * 5,
        color,
        alpha: 1,
        decay: 0.03 + Math.random() * 0.03
      });
    }
  }

  // Update UI Elements
  function updateBossHpUI() {
    bossCurrentHpEl.textContent = boss.hp;
    bossMaxHpEl.textContent = boss.maxHp;

    const percent = Math.max(0, Math.min(100, (boss.hp / boss.maxHp) * 100));
    hpBarFillEl.style.width = `${percent}%`;

    // Delayed damage lag
    setTimeout(() => {
      hpBarLagEl.style.width = `${percent}%`;
    }, 200);
  }

  function updateLeaderboardUI() {
    // Sort players by totalDamage descending
    const sorted = Array.from(players.values())
      .filter(p => p.totalDamage > 0)
      .sort((a, b) => b.totalDamage - a.totalDamage)
      .slice(0, 5);

    if (sorted.length === 0) {
      leaderboardListEl.innerHTML = '<li class="empty-state">Viewers type commands to join!</li>';
      return;
    }

    const medals = ['🥇', '🥈', '🥉', '4', '5'];
    leaderboardListEl.innerHTML = sorted.map((p, idx) => `
      <li class="leaderboard-item rank-${idx + 1}">
        <div class="player-info">
          <span class="rank-badge">${medals[idx]}</span>
          <span class="player-name" title="${p.username}">${p.username}</span>
        </div>
        <span class="player-damage">${p.totalDamage.toLocaleString()}</span>
      </li>
    `).join('');
  }

  function addCommandToFeed(username, command) {
    recentCommands.unshift({ username, command, id: Date.now() + Math.random() });
    if (recentCommands.length > 5) recentCommands.pop();

    commandFeedListEl.innerHTML = recentCommands.map(c => `
      <li class="feed-item">
        <span class="feed-author">@${c.username}</span>
        <span class="feed-cmd ${c.command === 'attack' ? 'attack' : ''}">${c.command}</span>
      </li>
    `).join('');
  }

  // Dispatch Command to Game
  function handleChatCommand(data) {
    const { username, userId, command, isSuperChat, dollarAmount } = data;
    const key = userId || username;

    // Join automatically on first command!
    let player = players.get(key);
    if (!player) {
      player = new Player(username, key);
      players.set(key, player);
    }

    player.executeCommand(command, isSuperChat, dollarAmount);
    addCommandToFeed(username, command);
  }

  // Quick Manual Test helper
  window.sendQuickCommand = function (command) {
    if (ws && ws.readyState === WebSocket.OPEN) {
      ws.send(JSON.stringify({
        type: 'TEST_COMMAND',
        username: 'You',
        userId: 'you_player',
        command
      }));
    } else {
      handleChatCommand({
        username: 'You',
        userId: 'you_player',
        command
      });
    }
  };

  // Test Mode Toggle
  btnToggleTestMode.addEventListener('click', () => {
    fetch('/api/test-mode', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({})
    })
      .then(res => res.json())
      .then(data => {
        updateTestModeUI(data.isTestMode);
      })
      .catch(() => {
        // Fallback local toggle
        const isNowOn = testModeStateText.textContent !== 'ON';
        updateTestModeUI(isNowOn);
      });
  });

  function updateTestModeUI(isActive) {
    testModeStateText.textContent = isActive ? 'ON' : 'OFF';
    if (isActive) {
      btnToggleTestMode.classList.remove('btn-secondary');
      btnToggleTestMode.classList.add('btn-warning');
      statusPill.classList.remove('connected');
      statusIcon.textContent = '⚡';
      statusText.textContent = 'TEST MODE ACTIVE';
    } else {
      btnToggleTestMode.classList.remove('btn-warning');
      btnToggleTestMode.classList.add('btn-secondary');
      statusIcon.textContent = '⏸️';
      statusText.textContent = 'TEST MODE OFF';
    }
  }

  // YouTube Connect Modal Handlers
  statusPill.addEventListener('click', () => {
    connectModal.classList.add('open');
  });

  btnOpenConnectModal.addEventListener('click', () => {
    connectModal.classList.add('open');
  });

  btnCloseConnectModal.addEventListener('click', () => {
    connectModal.classList.remove('open');
  });

  connectModal.addEventListener('click', (e) => {
    if (e.target === connectModal) {
      connectModal.classList.remove('open');
    }
  });

  btnConnectStream.addEventListener('click', () => {
    const url = inputStreamUrl.value.trim();
    const apiKey = inputApiKey.value.trim();

    if (!url) {
      modalStatusBox.textContent = 'Please enter a YouTube Live URL or Video ID.';
      modalStatusBox.style.color = 'var(--danger-red)';
      return;
    }

    modalStatusBox.textContent = 'Connecting to YouTube Live...';
    modalStatusBox.style.color = 'var(--neon-cyan)';
    btnConnectStream.disabled = true;

    fetch('/api/connect', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ url, apiKey })
    })
      .then(res => res.json())
      .then(data => {
        btnConnectStream.disabled = false;
        if (data.success) {
          modalStatusBox.textContent = `Connected! Live Chat: ${data.status.streamTitle}`;
          modalStatusBox.style.color = 'var(--neon-green)';
          btnDisconnectStream.style.display = 'inline-flex';
          setTimeout(() => connectModal.classList.remove('open'), 1200);
        } else {
          modalStatusBox.textContent = data.status?.error || 'Connection failed';
          modalStatusBox.style.color = 'var(--danger-red)';
        }
      })
      .catch(err => {
        btnConnectStream.disabled = false;
        modalStatusBox.textContent = `Connection error: ${err.message}`;
        modalStatusBox.style.color = 'var(--danger-red)';
      });
  });

  btnDisconnectStream.addEventListener('click', () => {
    fetch('/api/disconnect', { method: 'POST' })
      .then(res => res.json())
      .then(() => {
        modalStatusBox.textContent = 'Disconnected from YouTube Live.';
        btnDisconnectStream.style.display = 'none';
      });
  });

  // WebSocket Setup
  function connectWebSocket() {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const wsUrl = `${protocol}//${window.location.host}/ws`;

    ws = new WebSocket(wsUrl);

    ws.onopen = () => {
      console.log('🔗 WebSocket Connected to Game Server');
    };

    ws.onmessage = (event) => {
      try {
        const msg = JSON.parse(event.data);
        if (msg.type === 'CHAT_COMMAND') {
          handleChatCommand(msg);
        } else if (msg.type === 'STATUS_UPDATE') {
          updateConnectionStatusUI(msg.status);
        } else if (msg.type === 'TEST_MODE_UPDATE') {
          updateTestModeUI(msg.isTestMode);
        } else if (msg.type === 'INIT_STATE') {
          updateConnectionStatusUI(msg.status);
          updateTestModeUI(msg.isTestMode);
        }
      } catch (err) {
        console.error('WebSocket message error:', err);
      }
    };

    ws.onclose = () => {
      clearTimeout(wsReconnectTimer);
      wsReconnectTimer = setTimeout(connectWebSocket, 2000);
    };

    ws.onerror = () => {
      ws.close();
    };
  }
  connectWebSocket();

  function updateConnectionStatusUI(status) {
    if (status.connected) {
      statusPill.classList.add('connected');
      statusIcon.textContent = '🔴';
      statusText.textContent = `LIVE: ${status.streamTitle.slice(0, 20)}...`;
      btnDisconnectStream.style.display = 'inline-flex';
    } else if (status.connecting) {
      statusPill.classList.remove('connected');
      statusIcon.textContent = '⏳';
      statusText.textContent = 'CONNECTING...';
    } else {
      statusPill.classList.remove('connected');
      // If test mode is active
      if (testModeStateText.textContent === 'ON') {
        statusIcon.textContent = '⚡';
        statusText.textContent = 'TEST MODE ACTIVE';
      } else {
        statusIcon.textContent = '⚪';
        statusText.textContent = 'DISCONNECTED';
      }
      btnDisconnectStream.style.display = 'none';
    }
  }

  // ==========================================
  // CANVAS RENDER & DRAWING FUNCTIONS
  // ==========================================

  // Draw Background Arena
  function drawArena(ctx) {
    if (isTransparent) {
      ctx.clearRect(0, 0, V_WIDTH, V_HEIGHT);
      return;
    }

    // Deep space/esports grid background
    ctx.fillStyle = '#080B16';
    ctx.fillRect(0, 0, V_WIDTH, V_HEIGHT);

    // Subtle atmospheric ambient glow behind Boss
    const bossGlow = ctx.createRadialGradient(
      boss.x + boss.width / 2, boss.y + boss.height / 2, 40,
      boss.x + boss.width / 2, boss.y + boss.height / 2, 500
    );
    bossGlow.addColorStop(0, currentBossType.glow + '33');
    bossGlow.addColorStop(1, 'transparent');
    ctx.fillStyle = bossGlow;
    ctx.fillRect(0, 0, V_WIDTH, V_HEIGHT);

    // Ground platform line
    const groundGrad = ctx.createLinearGradient(0, GROUND_Y, V_WIDTH, GROUND_Y);
    groundGrad.addColorStop(0, 'rgba(0, 240, 255, 0.4)');
    groundGrad.addColorStop(0.5, 'rgba(168, 85, 247, 0.6)');
    groundGrad.addColorStop(1, 'rgba(255, 42, 85, 0.7)');

    ctx.strokeStyle = groundGrad;
    ctx.lineWidth = 4;
    ctx.beginPath();
    ctx.moveTo(0, GROUND_Y);
    ctx.lineTo(V_WIDTH, GROUND_Y);
    ctx.stroke();

    // Under-ground tech grid
    ctx.fillStyle = 'rgba(15, 23, 42, 0.6)';
    ctx.fillRect(0, GROUND_Y + 2, V_WIDTH, V_HEIGHT - GROUND_Y);

    ctx.strokeStyle = 'rgba(0, 240, 255, 0.15)';
    ctx.lineWidth = 1;
    for (let x = 0; x < V_WIDTH; x += 60) {
      ctx.beginPath();
      ctx.moveTo(x, GROUND_Y);
      ctx.lineTo(x, V_HEIGHT);
      ctx.stroke();
    }
  }

  // Draw Boss Creature
  function drawBoss(ctx) {
    if (boss.isDead && boss.deathTimer <= 0) return;

    ctx.save();
    ctx.translate(boss.x + boss.width / 2, boss.y + boss.height / 2 + boss.floatOffset);

    // Scale
    const s = currentBossType.scale * boss.breathScale;
    ctx.scale(s, s);

    // Hit flash effect
    if (boss.isHurt) {
      ctx.filter = 'brightness(2.5)';
    }

    // Ground Shadow
    ctx.fillStyle = 'rgba(0, 0, 0, 0.45)';
    ctx.beginPath();
    ctx.ellipse(0, 160 - boss.floatOffset, 120, 24, 0, 0, Math.PI * 2);
    ctx.fill();

    // Aura Glow
    ctx.shadowColor = currentBossType.glow;
    ctx.shadowBlur = boss.isHurt ? 45 : 25;

    // Body Form: Heavy Golem / Titan
    ctx.fillStyle = currentBossType.color;

    // Torso / Main Boulder
    ctx.beginPath();
    ctx.moveTo(-70, -40);
    ctx.lineTo(70, -40);
    ctx.lineTo(95, 70);
    ctx.lineTo(40, 120);
    ctx.lineTo(-40, 120);
    ctx.lineTo(-95, 70);
    ctx.closePath();
    ctx.fill();

    // Torso Core / Magma heart
    ctx.fillStyle = '#FFFFFF';
    ctx.beginPath();
    ctx.arc(0, 30, 22, 0, Math.PI * 2);
    ctx.fill();

    ctx.fillStyle = currentBossType.glow;
    ctx.beginPath();
    ctx.arc(0, 30, 16, 0, Math.PI * 2);
    ctx.fill();

    // Shoulders
    ctx.fillStyle = '#1E1B2E';
    ctx.beginPath();
    ctx.arc(-85, -20, 32, 0, Math.PI * 2);
    ctx.arc(85, -20, 32, 0, Math.PI * 2);
    ctx.fill();

    // Left & Right Floating Fists
    const fistOffset = Math.sin(Date.now() * 0.005) * 8;
    ctx.fillStyle = currentBossType.color;

    // Left Fist
    ctx.beginPath();
    ctx.roundRect(-145, 10 + fistOffset, 42, 52, 10);
    ctx.fill();

    // Right Fist (closer to players)
    ctx.beginPath();
    ctx.roundRect(105, 10 - fistOffset, 42, 52, 10);
    ctx.fill();

    // Head / Skull
    ctx.fillStyle = '#1A1829';
    ctx.beginPath();
    ctx.moveTo(-50, -40);
    ctx.lineTo(50, -40);
    ctx.lineTo(40, -115);
    ctx.lineTo(0, -135);
    ctx.lineTo(-40, -115);
    ctx.closePath();
    ctx.fill();

    // Horns / Spikes
    ctx.fillStyle = currentBossType.glow;
    ctx.beginPath();
    ctx.moveTo(-35, -115);
    ctx.lineTo(-75, -165);
    ctx.lineTo(-20, -125);
    ctx.fill();

    ctx.beginPath();
    ctx.moveTo(35, -115);
    ctx.lineTo(75, -165);
    ctx.lineTo(20, -125);
    ctx.fill();

    // Glowing Eyes
    ctx.fillStyle = boss.isHurt ? '#FFFFFF' : '#FF0055';
    ctx.shadowColor = '#FF0055';
    ctx.shadowBlur = 15;
    ctx.beginPath();
    ctx.arc(-18, -75, 7, 0, Math.PI * 2);
    ctx.arc(18, -75, 7, 0, Math.PI * 2);
    ctx.fill();

    // Demon Mouth / Grin
    ctx.fillStyle = currentBossType.glow;
    ctx.beginPath();
    ctx.moveTo(-24, -50);
    ctx.lineTo(24, -50);
    ctx.lineTo(16, -42);
    ctx.lineTo(-16, -42);
    ctx.closePath();
    ctx.fill();

    ctx.restore();
  }

  // Draw Weapon Slash Effects
  function drawSlashEffects(ctx) {
    for (let i = slashEffects.length - 1; i >= 0; i--) {
      const s = slashEffects[i];
      s.life--;
      if (s.life <= 0) {
        slashEffects.splice(i, 1);
        continue;
      }

      const progress = 1 - (s.life / s.maxLife);
      ctx.save();
      ctx.translate(s.x, s.y);
      ctx.scale(s.facing, 1);

      ctx.strokeStyle = s.color;
      ctx.shadowColor = s.color;
      ctx.shadowBlur = 18;
      ctx.lineWidth = 8 * (1 - progress);
      ctx.lineCap = 'round';

      ctx.beginPath();
      ctx.arc(0, 0, 55 + progress * 20, -0.6, 0.8, false);
      ctx.stroke();

      ctx.restore();
    }
  }

  // Draw Floating Damage Numbers
  function drawDamageNumbers(ctx) {
    for (let i = damageNumbers.length - 1; i >= 0; i--) {
      const d = damageNumbers[i];
      d.y += d.vy;
      d.alpha -= 0.022;

      if (d.alpha <= 0) {
        damageNumbers.splice(i, 1);
        continue;
      }

      ctx.save();
      ctx.font = `900 ${Math.floor(26 * d.scale)}px Chakra Petch`;
      ctx.fillStyle = d.color;
      ctx.shadowColor = d.color;
      ctx.shadowBlur = 12;
      ctx.globalAlpha = Math.max(0, d.alpha);
      ctx.textAlign = 'center';

      // Outline
      ctx.strokeStyle = '#000';
      ctx.lineWidth = 3;
      ctx.strokeText(d.text, d.x, d.y);
      ctx.fillText(d.text, d.x, d.y);

      ctx.restore();
    }
  }

  // Draw Particles
  function drawParticles(ctx) {
    for (let i = particles.length - 1; i >= 0; i--) {
      const p = particles[i];
      p.x += p.vx;
      p.y += p.vy;
      p.alpha -= p.decay;

      if (p.alpha <= 0) {
        particles.splice(i, 1);
        continue;
      }

      ctx.save();
      ctx.fillStyle = p.color;
      ctx.globalAlpha = Math.max(0, p.alpha);
      ctx.beginPath();
      ctx.arc(p.x, p.y, p.size, 0, Math.PI * 2);
      ctx.fill();
      ctx.restore();
    }
  }

  // Main 60fps Game Loop
  function gameLoop() {
    // Screen shake
    let shakeX = 0;
    let shakeY = 0;
    if (screenShake > 0) {
      shakeX = (Math.random() - 0.5) * screenShake;
      shakeY = (Math.random() - 0.5) * screenShake;
      screenShake *= 0.9;
      if (screenShake < 0.5) screenShake = 0;
    }

    ctx.save();
    // Clear whole physical canvas
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    // Apply scale & screen shake
    ctx.translate(offsetX + shakeX, offsetY + shakeY);
    ctx.scale(scale, scale);

    // 1. Draw Arena Background
    drawArena(ctx);

    // 2. Boss Hover Physics & Update
    if (!boss.isDead) {
      boss.floatOffset = Math.sin(Date.now() * 0.003) * 16;
      boss.breathScale = 1 + Math.sin(Date.now() * 0.004) * 0.03;
    }
    if (boss.hurtTimer > 0) {
      boss.hurtTimer--;
      if (boss.hurtTimer === 0) boss.isHurt = false;
    }

    // 3. Draw Boss
    drawBoss(ctx);

    // 4. Update & Draw All Viewers / Players
    players.forEach((player) => {
      player.update();
      player.draw(ctx);
    });

    // 5. Draw Weapon Slashes
    drawSlashEffects(ctx);

    // 6. Draw Particles & Damage Numbers
    drawParticles(ctx);
    drawDamageNumbers(ctx);

    ctx.restore();

    requestAnimationFrame(gameLoop);
  }

  // Start 60fps Game Loop
  requestAnimationFrame(gameLoop);

  // Initialize UI
  updateBossHpUI();
  updateLeaderboardUI();
})();
