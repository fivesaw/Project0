# ⚔️ Legit Client 1.8.9 — Master Project File

> **READ THIS ENTIRE FILE BEFORE WRITING A SINGLE LINE OF CODE.**
> This is the single source of truth. Every AI agent session must read it fully, check the Dev Log at the bottom, do only the current phase task, then append a new Dev Log entry before ending. The next agent reads the log and picks up exactly where you left off.

---

## 📌 Project Overview

A **Forge 1.8.9 client-side mod** that enhances gameplay through legitimate input automation and rendering. No packet manipulation. No memory injection. No server-side detection surface. Every action the mod takes is physically identical to a human pressing a key or moving a mouse. Works universally on any 1.8.9 server — cracked or premium, any minigame, any gamemode.

**Target:** Minecraft 1.8.9 | Forge `11.15.1.2318-1.8.9` | Java 8 (use JDK 8u202 or earlier — see setup notes)
**Dev launch:** `./gradlew runClient` with random offline username (pre-configured in build.gradle)
**IDE:** IntelliJ IDEA recommended

---

## 🤖 Model Usage Guide

| Task | Model | Thinking Level | Notes |
|---|---|---|---|
| Core module logic (combat timing, math) | **Gemini 3 Flash** | High | Most complex logic lives here |
| Rendering (ESP, HUD, overlays) | **Gemini 3 Flash** | Medium | OpenGL/RenderGameOverlayEvent |
| Boilerplate, file structure, simple getters | **Gemini 3 Flash** | Minimal | Fast and cheap |
| Forge 1.8.9 API bugs / crash debugging | **Gemini 3 Flash** | High | If fails twice → Claude Opus 4.6 |
| Architecture decisions | **Human (you) + this file** | — | Don't let AI redesign core structure |

> ⚠️ **CRITICAL FOR ALL JAVA AGENTS:** Minecraft 1.8.9 uses MCP stable_20 mappings. Method names are different from modern Minecraft. Always use:
> - `mc.thePlayer` not `mc.player`
> - `mc.theWorld` not `mc.level`  
> - `entity.getHealth()` not `entity.health`
> - `mc.thePlayer.swingItem()` not `mc.player.swing()`
> - `mc.gameSettings.keyBindAttack` not `mc.options.keyAttack`
> - `RenderGameOverlayEvent.ElementType` for HUD rendering
> - Event bus: `MinecraftForge.EVENT_BUS.register(this)`
> - Tick event: `@SubscribeEvent public void onTick(TickEvent.ClientTickEvent event)`

> ⚠️ **GOLDEN RULE:** The mod is READ-ONLY toward Minecraft internals. It reads game state via the official API. It acts only by simulating what a human hand would do: pressing keys via `KeyBinding.setKeyBindState()`, moving the mouse via `mc.thePlayer.rotationYaw/Pitch`, clicking via `mc.playerController`. NEVER touch packets, NEVER modify entity fields directly, NEVER use reflection to change private game values.

---

## 🏗️ Project Structure

```
legit-client/
├── LEGIT_CLIENT_MASTER.md          ← THIS FILE
├── DEV_LOG.md                      ← Session log (append only)
├── build.gradle                    ← Pre-configured, do not touch unless instructed
├── gradle/wrapper/
│   └── gradle-wrapper.properties   ← Gradle 4.0 (do not change version)
├── src/main/java/com/legitclient/
│   ├── LegitClient.java            ← @Mod main class, init, event bus registration
│   ├── module/
│   │   ├── Module.java             ← Abstract base class for all modules
│   │   ├── ModuleManager.java      ← Loads, stores, ticks all modules
│   │   ├── combat/
│   │   │   ├── AutoClicker.java
│   │   │   ├── RightClicker.java
│   │   │   ├── WTap.java
│   │   │   ├── STap.java
│   │   │   ├── JumpReset.java
│   │   │   ├── BlockHit.java
│   │   │   ├── AimAssist.java
│   │   │   ├── CritHelper.java
│   │   │   └── AutoSprint.java
│   │   ├── movement/
│   │   │   ├── StrafeAssist.java
│   │   │   ├── ScaffoldLegit.java
│   │   │   ├── ScaffoldGodBridge.java
│   │   │   ├── ScaffoldClutch.java
│   │   │   └── MouseDelayFix.java
│   │   ├── visual/
│   │   │   ├── PlayerESP.java
│   │   │   ├── Tracers.java
│   │   │   ├── NameTags.java
│   │   │   ├── ArmorESP.java
│   │   │   ├── StorageESP.java
│   │   │   ├── BlockHighlight.java
│   │   │   ├── Fullbright.java
│   │   │   ├── Zoom.java
│   │   │   ├── Freelook.java
│   │   │   ├── HitColor.java
│   │   │   └── NoOverlay.java
│   │   └── utility/
│   │       ├── ChestStealer.java
│   │       ├── AutoTool.java
│   │       ├── AutoArmor.java
│   │       ├── FastPlace.java
│   │       ├── InventorySorter.java
│   │       ├── AntiBlindness.java
│   │       └── ScreenshotManager.java
│   ├── gui/
│   │   ├── ClickGUI.java           ← Main mod menu (opened with RIGHT SHIFT)
│   │   ├── Panel.java              ← Category panel (Combat, Movement, Visual, Utility)
│   │   ├── ModuleButton.java       ← Per-module toggle + settings expand
│   │   └── components/
│   │       ├── SliderComponent.java
│   │       ├── ToggleComponent.java
│   │       └── TextComponent.java
│   ├── hud/
│   │   ├── HUDRenderer.java        ← RenderGameOverlayEvent handler
│   │   ├── KeystrokesHUD.java
│   │   ├── ArmorHUD.java
│   │   ├── PotionHUD.java
│   │   └── InfoHUD.java            ← FPS, Coords, Ping, Direction
│   ├── config/
│   │   ├── ConfigManager.java      ← Save/load JSON config files
│   │   └── profiles/               ← Config profiles (default.json, hypixel.json etc)
│   └── utils/
│       ├── RotationUtils.java      ← Yaw/pitch math, smooth rotation helpers
│       ├── EntityUtils.java        ← Target finding, LOS checks, distance
│       ├── BlockUtils.java         ← Block placement math, physics, raycasting
│       ├── TimerUtils.java         ← Tick-accurate timing helpers
│       └── RenderUtils.java        ← OpenGL helpers (drawRect, drawBorderedRect, etc)
└── src/main/resources/
    ├── mcmod.info
    └── META-INF/MANIFEST.MF
```

---

## 📦 Module Specifications

### ─── COMBAT ───

**AutoClicker**
- Simulates left clicks while left mouse button is held
- Settings: `minCPS` (1–20), `maxCPS` (1–20), `breakBlocks` (bool), `targetOnly` (bool)
- `targetOnly`: only clicks when crosshair is on a living entity's hitbox (use `mc.objectMouseOver`)
- Humanization: interval = `random.nextGaussian() * stdDev + mean` where mean = `1000 / avgCPS`
- Does NOT click if inventory is open, if `breakBlocks=false` and looking at a block
- Implementation: `TickEvent.ClientTickEvent` — track elapsed ms since last click, fire when threshold met

**RightClicker**
- Simulates right clicks while right mouse button is held
- Use cases: rods (fishing rod kb), bow charging, block placing speed
- Settings: `minCPS`, `maxCPS` — same humanization as AutoClicker
- Does NOT affect eating (eating is a hold, not a click rate — do not touch food logic)
- Does NOT fire if inventory is open

**W-Tap**
- On every successful hit (listen for `AttackEntityEvent` or detect `hurtTime` change on target), briefly releases and re-presses the W key
- Timing: release W → wait 1 tick → re-press W
- This resets sprint state so every hit gets "first hit" knockback multiplier
- Settings: `enabled` only — no further config needed, timing is fixed to 1 tick
- Guard: only fires when player IS sprinting (`mc.thePlayer.isSprinting()`)

**S-Tap**
- Alternative to W-Tap. On hit, briefly presses S instead of releasing W
- More precise distance control during combos — keeps you just out of enemy reach
- Settings: `tapDuration` (ms, default 60ms), `mode` (enum: WTAP / STAP / BOTH)
- Guard: only fires when sprinting and moving forward

**Jump Reset**
- On hit, triggers a jump if player is on ground (`mc.thePlayer.onGround`)
- Timing: fire spacebar press 1–2 ticks after hit registers
- This resets the knockback trajectory mid-combo
- Settings: `delay` (ticks, 1–3), `onlyWhenCombo` (bool — only jump if last hit was <500ms ago)

**Block Hit**
- Modes:
  - `MANUAL`: right-click sword every N hits. Settings: `frequency` (every 1/2/3 hits)
  - `AUTO`: right-click immediately after every hit you land
  - `PREDICT`: monitor `mc.thePlayer.hurtTime` — when it drops below threshold (incoming hit predicted), pre-block
- Right-click = `mc.thePlayer.isBlocking()` context — simulate right click hold/release
- Guard: only when holding a sword (`mc.thePlayer.getCurrentEquippedItem()` instanceof `ItemSword`)

**Aim Assist**
- DOES NOT lock onto players or auto-aim when not fighting
- ONLY activates when: player is left-clicking (autoclicker OR manual) AND a target entity is within FOV cone AND within range
- Behavior: applies a smooth yaw/pitch delta per tick nudging crosshair toward nearest point on target hitbox (NOT center — nearest point to current crosshair)
- Uses `RotationUtils.smoothRotate(currentYaw, targetYaw, speed)` — never teleports aim
- Settings: `fov` (degrees, 30–120), `speed` (smoothing factor 1–10), `verticalCorrection` (bool), `range` (blocks, 3–8)
- Target priority: closest to crosshair within FOV, not just closest by distance
- Deactivates instantly if no valid target or player stops clicking

**Crit Helper**
- Detects when `mc.thePlayer.fallDistance > 0` and `mc.thePlayer.motionY < 0` (falling = crit frame)
- Attempts to land attack during crit frames for 150% damage
- Settings: `mode` — `FALL` (only attack when naturally falling), `HOP` (trigger small jump before attack)
- `HOP` mode: press jump 1 tick before swing so you're falling by the time hit lands
- Guard: only when a target is within reach distance

**AutoSprint**
- Calls `mc.thePlayer.setSprinting(true)` every tick
- Smart cancel: does NOT sprint when blocking, sneaking, in water, in GUI
- Settings: `enabled` only

---

### ─── MOVEMENT ───

**Strafe Assist**
- Smoothly adjusts A/D input to orbit around the current target
- Reads enemy position, calculates optimal strafe angle to maintain ~2.5–3.5 block distance
- Applies directional input adjustment (not teleportation, just key state modulation)
- Settings: `mode` (CIRCLE / SEMI / RANDOM), `radius` (blocks), `speed` (multiplier)
- `RANDOM` mode: introduces slight timing noise to strafe direction changes so it's not mechanical

**Scaffold — Legit Mode**
- Detects player is at block edge (uses `BlockUtils.isAtEdge()` — checks block beneath feet and one step forward)
- Auto-sneak at the exact tick before the edge to prevent fall
- Settings: `enabled` only — pure timing, no block placement

**Scaffold — GodBridge Mode**
- Automates GodBridging: jump → place block → sneak → unsSneak at precise tick windows
- Timing is based on actual 1.8.9 bridge mechanics (20 TPS physics)
- Slows input cadence slightly after each place to match Hypixel anticheat timing expectations
- Settings: `speed` (slow/medium/fast), `direction` (auto-detect from movement keys)

**Scaffold — Clutch Mode**
- PHYSICS-AWARE fall detection: reads `mc.thePlayer.motionY`, `mc.thePlayer.posY`, nearby block grid
- Calculates: current velocity → time to impact → blocks needed → placement sequence
- Checks nearby surfaces: walls, ledges, partial blocks — uses closest surface if falling near a wall
- Block slot: scans inventory for placeable solid blocks, switches to best available slot
- Places blocks in calculated sequence using `mc.playerController.onPlayerRightClick()`
- Guard: activates only when `motionY < -0.5` (actually falling fast, not just a normal jump)
- Settings: `minHeight` (min blocks from ground to activate, default 4)

**Mouse Delay Fix**
- Sets `mc.gameSettings.smoothCamera = false` every tick
- Forces raw mouse input by resetting any cinematic camera drift
- Settings: `enabled` only — set it and forget it

---

### ─── VISUAL ───

**Player ESP**
- Renders colored boxes around all `EntityPlayer` instances in render distance
- Box = entity bounding box (`entity.getEntityBoundingBox()`)
- Modes: `BOX` (full wireframe), `CORNERS` (just corner lines — subtler), `OUTLINE` (2D screen outline)
- Settings: `mode`, `color` (RGBA), `showTeammates` (bool), `maxRange` (blocks)
- Rendered in `RenderWorldLastEvent` using GL11 depth test disabled so visible through walls

**Tracers**
- Draws line from screen center to each visible player
- Settings: `color`, `showTeammates`, `maxRange`
- Rendered in `RenderWorldLastEvent`

**NameTags (Enhanced)**
- Replaces default nametags with larger, customizable ones visible through walls
- Shows: name, health bar (colored green→yellow→red), distance (blocks), armor icons
- Settings: `showHealth`, `showDistance`, `showArmor`, `scale`, `maxRange`
- Rendered in `RenderLivingEvent` — disable vanilla nametag first

**Armor ESP**
- Shows armor/weapon of nearby players as small icons rendered near their entity
- Uses `entity.inventory.armorInventory[]` to get equipped items
- Rendered in `RenderWorldLastEvent`

**Storage ESP**
- Highlights `TileEntityChest`, `TileEntityFurnace`, `TileEntityDispenser` through walls
- Settings: `chests` (bool), `furnaces` (bool), `dispensers` (bool), `color`, `maxRange`

**Block Highlighter / Search**
- User specifies target block type(s) in GUI
- Renders glowing outline on all matching blocks in render distance
- Settings: `blockList` (list of block IDs), `color`, `maxRange`

**Fullbright**
- Sets `mc.gameSettings.gammaSetting = 1000.0` when enabled, restores previous value on disable
- Settings: `enabled` only

**Zoom**
- On zoom key hold: lerp FOV from `mc.gameSettings.fovSetting` down to 10
- Scroll wheel changes zoom level while held
- On release: lerp FOV back to original
- Settings: `keybind`, `smoothness` (lerp speed), `minFOV`

**Freelook**
- Hold key to detach camera rotation from player head
- Store real yaw/pitch, let camera rotate freely, restore when released
- Settings: `keybind`

**Hit Color**
- Overrides the red damage flash color with custom color
- Hook: `RenderLivingEvent` — modify entity's `hurtTime`-based color
- Settings: `color` (RGBA)

**No Overlay (NoFire / NoPumpkin)**
- Cancels render of fire overlay: cancel `RenderGameOverlayEvent` for `ElementType.ALL` when fire overlay condition met
- Cancels pumpkin overlay similarly
- Settings: `noFire` (bool), `noPumpkin` (bool)

---

### ─── UTILITY ───

**ChestStealer**
- On chest open (`GuiChest`), shift-clicks all items one by one with configurable delay
- Settings: `delay` (ms between clicks, 50–200 — humanized), `mode` (ALL / VALUABLES_ONLY)
- VALUABLES_ONLY: only takes armor, weapons, food, rare items — ignores cobblestone etc

**AutoTool**
- On `LeftClickBlock` event, scans hotbar for best tool for that block type
- Uses `item.getStrVsBlock(block)` efficiency values to pick fastest tool
- Switches back to previous slot on block break or after 2 seconds
- Settings: `enabled`, `switchBack` (bool)

**AutoArmor**
- On inventory open OR on periodic tick check, compares worn armor vs inventory armor
- Calculates total armor points + toughness, switches to highest value set
- Settings: `enabled`, `onlyWhenBetter` (bool — threshold before switching)

**FastPlace**
- Removes right-click placement delay by setting `rightClickDelayTimer = 0` each tick
- Settings: `enabled` only

**Inventory Sorter**
- On keybind press, sorts inventory: armor→weapons→tools→food→blocks→misc
- Within each category: sorted by quality/value
- Settings: `keybind`

**Anti-Blindness**
- When blindness potion is active, sets gamma high enough to counteract darkness
- Settings: `enabled` only

**Screenshot Manager**
- On F2, saves screenshot with timestamp filename
- Settings: `autoUpload` (bool — future feature), `saveDir` (path)

---

### ─── GUI & FRAMEWORK ───

**ClickGUI**
- Opened with RIGHT SHIFT (configurable)
- Panels per category: Combat | Movement | Visual | Utility
- Each panel: draggable, collapsible
- Each module: toggle button + expand arrow for settings
- Settings components: Slider (float/int), Toggle (bool), Enum (dropdown cycle)
- Search bar at top — filters modules by name
- Blurred background behind GUI (if performance allows — optional)
- ESC closes GUI

**Config System**
- On client shutdown: save all module states + settings to `config/legitclient/default.json`
- On client start: load config
- Profiles: multiple named configs switchable from GUI
- Format: `{ "moduleName": { "enabled": true, "settings": { "cps": 14.0 } } }`

**Notification System**
- Toast popup in screen corner when module toggled
- Shows: module name, ON (green) / OFF (red)
- Fades out after 1.5 seconds
- Settings: position (corner), duration

**Keybind System**
- Each module has assignable keybind
- Default: none (must be set in GUI)
- Keybind screen in GUI for quick overview of all binds
- Conflicts highlighted in yellow

---

## 🔧 Technical Notes (MUST READ for all agents)

### Forge 1.8.9 Setup Gotchas
- **Java version:** MUST use JDK 8. Specifically use **JDK 8u202 or earlier**. JDK 8u242+ breaks `runClient` due to `NullPointerException` in LWJGL/AWT. Download from: https://www.java.net/download/java_archive
- **Gradle version:** Must be 4.0. The `gradle-wrapper.properties` is pre-configured. Do NOT change it.
- **ForgeGradle:** Using `net.minecraftforge.gradle:ForgeGradle:2.1-SNAPSHOT`. This is correct for 1.8.9.
- **MCP Mappings:** `stable_20` — designed for 1.8.8, works for 1.8.9 with expected warning. This is fine.
- **First setup:** run `./gradlew setupDecompWorkspace` ONCE before first `runClient`. This takes a few minutes.

### Mouse Rotation (CRITICAL)
- Set `mc.thePlayer.rotationYaw` and `mc.thePlayer.rotationPitch` directly for rotation changes
- Do NOT use `mc.thePlayer.setAngles()` — deprecated and inconsistent
- Yaw range: -180 to 180 (wraps). Pitch range: -90 (look up) to 90 (look down)
- For smooth rotation: interpolate toward target by `speed * delta` per tick — never jump directly to target

### Rendering
- ALL world-space rendering (ESP, tracers): use `RenderWorldLastEvent`
- ALL screen-space rendering (HUD): use `RenderGameOverlayEvent`
- Always wrap GL calls with `GL11.glPushMatrix()` / `GL11.glPopMatrix()`
- Disable depth test for through-wall rendering: `GL11.glDisable(GL11.GL_DEPTH_TEST)` → render → `GL11.glEnable(GL11.GL_DEPTH_TEST)`
- Disable lighting: `GL11.glDisable(GL11.GL_LIGHTING)` during custom renders

### Input Simulation
- Key press: `KeyBinding.setKeyBindState(mc.gameSettings.keyBindXxx.getKeyCode(), true/false)`
- Attack/click: `mc.leftClickCounter = 0` + `mc.playerController.attackEntity(player, target)`
- Right click: `mc.rightClickMouse()` — this is the safest method for right click simulation
- Sprint: `mc.thePlayer.setSprinting(true)` directly

---

## 📋 Phase Plan

### ✅ Phase 0 — Project Setup
- [ ] Verify `build.gradle` and `gradle-wrapper.properties` are correct
- [ ] Run `./gradlew setupDecompWorkspace`
- [ ] Run `./gradlew runClient` — game launches with random username in offline mode
- [ ] Verify mod loads (check logs for mod ID)

### 🔲 Phase 1 — Framework (No modules yet)
- [ ] `Module.java` — abstract base: name, category, enabled bool, keybind, settings list
- [ ] `ModuleManager.java` — ArrayList of modules, tick all enabled, register events
- [ ] `LegitClient.java` — @Mod class, init, register ModuleManager to event bus
- [ ] `ConfigManager.java` — basic save/load JSON
- [ ] `ClickGUI.java` — minimal working GUI (just panels + toggles, no settings yet)
- [ ] Notification toast system
- [ ] Test: open GUI, toggle a dummy module, see notification

### 🔲 Phase 2 — Combat Modules
Order: AutoSprint → AutoClicker → WTap → JumpReset → BlockHit → STap → CritHelper → AimAssist

### 🔲 Phase 3 — HUD Modules
Order: InfoHUD (FPS/Coords) → KeystrokesHUD → ArmorHUD → PotionHUD

### 🔲 Phase 4 — Visual Modules
Order: Fullbright → Zoom → PlayerESP → Tracers → NameTags → ArmorESP → StorageESP → BlockHighlight → Freelook → HitColor → NoOverlay

### 🔲 Phase 5 — Utility Modules
Order: AutoSprint (done) → FastPlace → AutoTool → ChestStealer → AutoArmor → InventorySorter → AntiBlindness

### 🔲 Phase 6 — Movement Modules
Order: MouseDelayFix → ScaffoldLegit → StrafeAssist → ScaffoldGodBridge → ScaffoldClutch

### 🔲 Phase 7 — Polish
- [ ] Full ClickGUI with sliders, settings per module
- [ ] Config profiles (multiple presets)
- [ ] Keybind conflict detection
- [ ] Performance pass (reduce per-tick allocations)
- [ ] Final test across cracked servers

---

## 📋 DEV LOG

> **RULE:** Every session MUST append a new entry here. Format exactly as shown. Be specific — the next agent should know its first action immediately without guessing.

---

### Session 0 — 2026-03-02 — Planning (Human + Claude Sonnet 4.6)
**Done:** Full architecture designed. All modules specified. Build system documented. Phase plan written. This file created.

**Files created:** `LEGIT_CLIENT_MASTER.md`, `build.gradle`, `gradle-wrapper.properties`, `DEV_LOG.md`

**Nothing coded yet. Zero Java source files exist.**

**Next agent first task — Phase 0 + Phase 1 start:**
1. Read this file fully
2. Create the full directory structure from "Project Structure" section above (all folders + empty placeholder files)
3. Write `LegitClient.java` — @Mod skeleton only, no modules yet
4. Write `Module.java` — abstract base class with: name (String), category (enum), enabled (boolean), keybind (int = -1), toggle(), onEnable(), onDisable()
5. Write `ModuleManager.java` — holds `List<Module>`, `register(Module)`, `getModule(String)`, `onTick()` that calls each enabled module's tick
6. Run `./gradlew runClient` — game must launch without crash
7. Append Session 1 entry to DEV_LOG with exact status

**Model:** Gemini 3 Flash, thinking: medium for framework Java, minimal for boilerplate
