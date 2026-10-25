# Airport Architect — Forge 1.20.1 (Phase 1)

A first playable foundation for a Minecraft airport terminal generator.

## Target environment
- Minecraft 1.20.1
- Minecraft Forge 47.x (build.gradle currently pins 47.2.0)
- Java 17

## Phase 1 features
- Registered Airport Blueprint Tool.
- Basic client-side designer screen with Small, Medium, and Large presets.
- Server packet validates Creative mode before generation.
- Deterministic modular terminal generator: polished-andesite floor, glass facade, structural columns, flat roof/skylight, counters, and gate-lounge markers.
- Instant generation; no materials consumed.

## Build and run
1. Install a JDK 17 and an IDE with Gradle support.
2. Download/extract the official Forge 1.20.1 MDK into this project directory if you need the Gradle wrapper files (`gradlew`, `gradlew.bat`, and `gradle/wrapper/gradle-wrapper.jar`). Keep this project's source and build files.
3. Run `gradlew genIntellijRuns` or `gradlew genEclipseRuns` as appropriate, then launch the `runClient` configuration. Alternatively run `gradlew runClient`.
4. To build, run `gradlew build`; the mod jar will be in `build/libs/`.
5. Create a Creative world, take the Airport Blueprint Tool from the creative inventory only after adding an item-group entry, or use `/give @p airportarchitect:blueprint_tool`, then right-click it and choose a size.

## Important current limitations
- The blueprint tool is obtainable with `/give @p airportarchitect:blueprint_tool`; Phase 1 does not yet add a dedicated creative tab.
- The generator writes blocks in the selected footprint, so **clear the build area first**. It does not yet inspect terrain, warn about occupied blocks, rotate the building to match player direction, or provide an undo operation.
- The current designer is a preset selector, not yet the grid-based Modular Designer or Freeform Planner. Both will use a shared `TerminalBlueprint` data model added in Phase 2.
- No claim of compilation/testing is made from this package; build it with the Forge MDK and report any Gradle/compiler output for iteration.

## Planned architecture
- `client/`: all GUI and preview-only code
- `blueprint/`: serializable terminal design data, modules, ports, and orientation
- `world/`: deterministic planner and server-side placement
- `network/`: validated client-to-server design requests
- `registry/`: registered blocks/items

## Safety/performance notes
The server caps preset dimensions to 40 x 54 x 12. Instant generation is intentionally limited to these sizes. Larger terminals should later use a staged placement queue to avoid long server ticks.

## Build online for free with GitHub Actions

This project includes `.github/workflows/build.yml` so GitHub can attempt to compile the mod in the cloud.

1. Sign in to GitHub and create a new repository. For the easiest free workflow, make it public; don't upload passwords, tokens, or other secrets.
2. Upload the contents of this project (including the hidden `.github` folder) to the repository and commit them to `main`.
3. Open the repository's **Actions** tab and select **Build Airport Architect**.
4. Click **Run workflow**. Wait for the run to finish. A failed run means the source or build setup needs fixes; it does not produce a working mod.
5. If the run succeeds, open that run and download the **airport-architect-jar** artifact. Extract the downloaded artifact ZIP to get the `.jar` file.
6. Put the `.jar` in the `mods` folder for a Minecraft 1.20.1 Forge installation. Back up worlds and test in a disposable world first.

The first cloud build may take several minutes while Gradle downloads Forge and its dependencies. This workflow is a build attempt, not a guarantee that the current source compiles or runs correctly.
