# XPlus Autofish

XPlus Autofish is an update of [MrTroot's Autofish mod](https://www.curseforge.com/minecraft/mc-mods/autofish) for Minecraft 1.19.4+, with additional features and bug fixes. The project provides Fabric, Forge, and NeoForge versions on their respective branches.

In short, just cast the fishing rod into the water once, and it will automatically reel in at the correct time and recast after a couple second wait. You can open the config screen of the mod via hotkey (V by default).

## Download

You can download the mod from:

| Link type  | Link                                                         |
| ---------- | ------------------------------------------------------------ |
| Modrinth   | [https://modrinth.com/mod/x+-autofish](https://modrinth.com/mod/x+-autofish) |
| Curseforge | [https://www.curseforge.com/minecraft/mc-mods/x-autofish](https://www.curseforge.com/minecraft/mc-mods/x-autofish) |

## Build from source

The repository includes a Gradle Wrapper, so a separate Gradle installation is unnecessary. Run the commands from the repository root. The first build requires an internet connection to download Gradle and project dependencies.

Clone the repository:

```sh
git clone https://github.com/Wudji/XPlus-AutoFish.git
cd XPlus-AutoFish
```

Build on Windows (PowerShell):

```powershell
.\gradlew.bat build
```

Build on Linux or macOS:

```sh
chmod +x gradlew
./gradlew build
```

Build artifacts are written to `build/libs/`. The `-sources.jar` file contains source code and is not the distributable mod.

## Tests

`build` also runs the automated tests. To run only the tests:

```powershell
# Windows
.\gradlew.bat test
```

```sh
# Linux / macOS
./gradlew test
```

## Development build

Launch a development client using the Gradle Wrapper:

```powershell
# Windows
.\gradlew.bat runClient
```

```sh
# Linux / macOS
./gradlew runClient
```

To remove generated build files before rebuilding, run `clean build` instead of `build`. If dependencies are already cached, append `--offline` to build without downloading them.

## License

See [LICENSE](LICENSE) for the project's license.

