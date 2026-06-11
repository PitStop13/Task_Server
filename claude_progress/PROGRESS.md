# Claude Progress — TaskServer Android App

## Progetto
- **Path**: `C:\Users\pietr\Desktop\App_Android\Task-Server - Copia`
- **Stack**: Kotlin + Jetpack Compose (Material 3) + Hilt + Room + SSHJ
- **Target**: Google Pixel 8, dark mode only

---

## Struttura chiave

### Theme
- `ui/theme/Color.kt` — palette colori + alias backwards-compat
- `ui/theme/Theme.kt` — MaterialTheme (dark), shapes, statusBar
- `ui/theme/Type.kt` — tipografia completa (SansFontFamily, MonospaceFontFamily)

### Componenti UI
- `ui/components/Glass3.kt` — GlassSurface (card principale), BentoCard (grid layout home)
- `ui/components/GlassComponents.kt` — GlassCard, GlowBorder, shimmer, AccentLine, GlowDot
- `ui/components/ServerCard.kt` — PROBABILMENTE INUTILIZZATO (inline in ServersScreen)
- `ui/components/TaskCard.kt` — PROBABILMENTE INUTILIZZATO (inline in TasksScreen)
- `ui/components/StatusBadge.kt` — badge stato server
- `ui/components/TerminalOutput.kt` — output terminale SSH
- `ui/components/CustomSnackbar.kt` — snackbar personalizzata

### Schermate principali
- `ui/screens/home/HomeScreen.kt` + HomeViewModel.kt
- `ui/screens/servers/ServersScreen.kt` + ServerEditScreen.kt + ServerViewModel.kt
- `ui/screens/tasks/TasksScreen.kt` + TaskBuilderScreen.kt + TaskViewModel.kt
- `ui/screens/logs/LogsScreen.kt` + LogsViewModel.kt
- `ui/screens/terminal/TerminalScreen.kt` + TerminalViewModel.kt
- `ui/screens/ssh/SshScreen.kt`
- `ui/screens/settings/SettingsScreen.kt` + SettingsViewModel.kt

### Navigazione
- `ui/navigation/AppNavigation.kt` — NavHost + bottom nav bar + sealed class Screen

### Data layer
- Room DB: Server, Task, ExecutionLog
- SSH via SSHJ: SshManager, InteractiveSshSession
- Hilt DI: AppModule
- Security: BiometricHelper, CredentialManager

---

## Sessione 1 — Redesign Grafica (11/06/2026)

### Problema identificato
- Palette cyan (#3ECFFF) + viola (#A78BFA): gaming/neon, non professionale
- GlassSurface usa Color(0xFFFFFFFF).copy(alpha=0.06f): quasi invisibile sul bg nero
- BentoCard home: layout bento confuso con altezze fisse, informazioni poco chiare
- Bottom nav: floating pill con padding troppo consumer-app
- Corner radius 28-36dp: troppo arrotondato per un tool professionale

### Strategia redesign
- Ispirazione: Linear.app, Vercel Dashboard, GitHub Dark
- Palette: single blue accent #4D80FF + surfaces dark blue-grey (no piu cyan+viola)
- Cards: solide (SurfaceOne=#161A28), con border sottile visibile (#252E48)
- Bottom nav: standard NavigationBar flush, no floating pill
- Corner radius max 16dp per cards (meno bubbly, piu enterprise)

### Nuova palette (Color.kt)
```
BackgroundDeep  = #0B0D14   <- app background
SurfaceOne      = #161A28   <- card / sheet
SurfaceTwo      = #1E2338   <- elevated
BorderNormal    = #252E48   <- border cards
AccentBlue      = #4D80FF   <- primary CTA
StatusOnline    = #2DD4BF   <- online (teal-green)
StatusOffline   = #FF4757   <- offline
StatusChecking  = #FF9F40   <- checking / warning
TextPrimary     = #E2E8F5   <- headings
TextSecondary   = #7A8299   <- labels
```

### File modificati questa sessione
1. [x] claude_progress/PROGRESS.md — questo file
2. [x] ui/theme/Color.kt
3. [x] ui/theme/Theme.kt
4. [x] ui/components/Glass3.kt
5. [x] ui/components/GlassComponents.kt
6. [x] ui/screens/home/HomeScreen.kt
7. [x] ui/navigation/AppNavigation.kt

### Da fare prossime sessioni
- [ ] ServersScreen: piccole cleanup (colori propagano dal tema, ma verificare)
- [ ] TasksScreen: idem
- [ ] TaskBuilderScreen: UX del drag-reorder comandi
- [ ] TerminalScreen / SshScreen: verificare look con nuovi colori
- [ ] LogsScreen / SettingsScreen: verificare
- [ ] Audit funzionale SSH, task execution, Room DB
- [ ] Cleanup: verificare e rimuovere ServerCard.kt e TaskCard.kt se inutilizzati

---

## Note tecniche da ricordare

### ServersScreen.kt e TasksScreen.kt
- Usano `GlassSurface(cornerRadius = 28.dp)` — troppo arrotondato con il nuovo look.
- I COLORI si aggiornano automaticamente dal tema.
- Il CORNER RADIUS va aggiornato manualmente a 16.dp nella prossima sessione.

### Imports rimasti inutilizzati (non bloccanti, solo warning)
- `HomeScreen.kt` importa `RoundedCornerShape` ma non lo usa direttamente.
- Pulire nella prossima sessione se danno fastidio.

### GlassSurface: parametro `intensity`
- Mantenuto per compat con l'API originale ma ignorato internamente.
- Può essere rimosso in futuro.

### GlassSecondary (ex viola #A78BFA)
- Rimappato su AccentBlue — tutte le schermate che usavano GlassSecondary
  ora mostrano il blu professionale invece del viola gaming.
- TasksScreen usava GlassSecondary come colore del FAB e delle card.

### Bottom Nav
- Rimosso il floating pill con padding (consumer look).
- Ora: NavigationBar standard con SurfaceOne background + HorizontalDivider sopra.
- NavigationBarColor della system bar aggiornato a SurfaceOne.
