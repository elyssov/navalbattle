# Naval Battle: Strategic Edition

Native Android (Kotlin + Jetpack Compose) port of the strategic battleship game with TARKR, carriers, submarines, PCR Vulkan missiles, T-15 nuclear torpedoes and the full nuclear release ceremony.

## Status

**v1.0.0** — first native Compose build (replacing the WebView-based version that suffered DPI/scaling issues across Android devices).

### What's in

- Menu / Settings / Placement / Battle / Victory screens (Material3)
- RU + EN localization
- **Pinch-zoom, pan, long-press rotate** on every battlefield canvas (the original `vh/vw` scaling drama is gone)
- 7 ship classes with sprites: missile boat, MRK, destroyer, cruiser, TARKR, carrier, submarine
- 3 field sizes: Skirmish 30×30 / Squadron 50×50 / Admiral 100×100
- 4 landscape types with connectivity check: Open Ocean / Wrecks / Islands / Archipelago
- AI opponent (hunt-and-shoot heuristic, checkerboard fallback)
- Reactor detonation, magazine chain explosions
- **Nuclear release ritual** — 4-act ceremony (СДВ «ЗЕВС» / TACAMO SKYKING), 6-digit launch code, 45-second auto-lockout timer
- 11×11 nuclear blast zone with 3-turn contamination
- 8 sunk-ship narratives + 15 captain's last words (literature, not stubs)
- Vietcombank/PayPal tip pop-up

### Coming next

- Hotseat handoff overlay
- Audio engine (SoundPool sfx + ambient loop)
- PCR Vulkan / Torpedo / Striker / Manual radar UI bindings
- Submarine deploy UI
- P2P over local Wi-Fi (Nearby Connections)

## Build

```
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`. The signed-debug APK is also copied to repo root as `NavalBattle-v1.0.0-debug.apk` for quick install.

Requires Android SDK at `C:\Android\sdk` (configured in `local.properties`), JDK 17, Gradle 8.7 (wrapper included).

## Tech

- Kotlin 2.0.21, AGP 8.5.2, Compose BOM 2024.09.03, Material3
- `kotlinx-serialization-json` for save-game (planned)
- Single-Activity, ViewModel-driven (`GameViewModel` with `StateFlow`)
- All canvas work via custom `FleetGrid` composable with `awaitEachGesture` for pinch/pan/long-press

## License

MIT (see [LICENSE](LICENSE)).
