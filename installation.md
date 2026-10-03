# Installing and running ROTP Fusion

ROTP Fusion runs from a folder; it does not require a setup wizard or a system-wide installation. You do not need to build the source code to play a packaged release.

## Choose the right download

Open the GitHub repository's **Releases** page and expand **Assets** for the release you want. The date in each filename identifies the version.

| File | What it is | Java installation needed? |
| --- | --- | --- |
| `rotp-Fusion-YYYY-MM-DD-windows.zip` | Windows package containing the game EXE and a `jre` folder | No; Java is bundled |
| `rotp-Fusion-YYYY-MM-DD.jar` | Full game, including media, for Windows, Linux or macOS | Yes; Java 17 or newer |
| `rotp-Fusion-YYYY-MM-DD-mini.jar` | Smaller game package using compressed images and audio | Yes; Java 17 or newer |
| **Source code (zip)** / **Source code (tar.gz)** | Source files for developers | Not a ready-to-play download |

A release from another repository may not include this checkout's local hotseat changes. For this checkout, use the instructions below under **Local hotseat build**.

## Windows: packaged release

1. Download the file ending in **`-windows.zip`** from the release's Assets.
2. Right-click the ZIP and choose **Extract All**. Use a writable folder such as `C:\Users\YourName\Games\ROTP-Fusion`, outside `Program Files`.
3. Open the extracted folder and double-click **`rotp-Fusion-YYYY-MM-DD.exe`**.

Extract the entire ZIP before starting. Keep the **`jre` folder next to the EXE**; do not move only the EXE to your desktop. Create a shortcut to it instead. The EXE is the game launcher, not an installer.

## Windows: full JAR alternative

1. Install Java 17 or newer (a JRE or JDK).
2. Download the full **`.jar`** file and place it in a writable game folder.
3. Open PowerShell in that folder and run the following, replacing the example filename with the file you downloaded:

```powershell
java -Xmx3g -jar ".\rotp-Fusion-YYYY-MM-DD.jar" arg1
```

Double-clicking the JAR also works if Windows associates JAR files with Java. If it opens an archive tool or nothing happens, use the command above. Run `java -version` to check which Java version is available.

`-Xmx3g` allows up to 3 GB of Java heap memory. `arg1` tells the game to use that allocation without automatically relaunching itself.

## Local hotseat build (this development checkout)

Use **`target/rotp-Fusion-2026-10-02-windows.zip`** for the repaired Windows build. Extract it into a new writable folder and run **`rotp-Fusion-2026-10-02.exe`**, keeping `jre` beside it. This package includes the local hotseat changes and all converted media. The old October 1 EXE/ZIP was built without converted media and should not be used.

In this workspace, double-click **`Play Rotp Hotseat.cmd`** in the project root. It launches the current EXE from `target/windows-native-ui` with its bundled Java runtime. This extracted package includes the later fixes that use the game's own research, report and decision screens. The older `target/windows-exe-verified` folder predates those fixes.

For the current full-JAR alternative, run this from the project root in PowerShell:

```powershell
& .\target\phase1-tools\jdk-17.0.20.1+1\bin\javaw.exe -Xmx3g -jar .\target\rotp-Fusion-2026-10-02.jar arg1
```

That command and shortcut depend on this checkout's existing build files; they are not standalone installers. To play on another computer, copy the full JAR and install Java 17 or newer there, then use the full-JAR instructions above.

To enable hotseat, choose **New Game**, continue to galaxy setup, click **Players: Single Player**, assign at least two **Human** players with unique names, and click **Use Hot Seat**. See [the hotseat guide](doc/multiplayer/hot-seat.md) for play and save instructions.

## Linux and macOS

1. Install Java 17 or newer for your operating system. On Linux, use your distribution's package manager.
2. Download the full `.jar` file into a writable folder, such as `~/Games/ROTP-Fusion`.
3. Open a terminal, change to that folder, and run the command below with your actual filename:

```sh
cd ~/Games/ROTP-Fusion
java -Xmx3g -jar rotp-Fusion-YYYY-MM-DD.jar arg1
```

The Windows ZIP and EXE are for Windows only.

## Saves, updates and startup problems

- Keep the game folder writable: saves and settings normally live alongside the game.
- Before replacing a version, back up your game folder, including saves and settings.
- If the loading screen disappears without opening the game, try the full JAR from a terminal so startup errors remain visible. In this local hotseat checkout, use the launcher described above.
- If `java` is not recognized, install Java 17 or newer and reopen your terminal, or run Java using its full executable path.
- If the Windows EXE cannot find Java, check that the extracted `jre` folder is still beside it.

## Steam Deck

See the community [guide to installing ROTP on Steam Deck](https://old.reddit.com/r/rotp/comments/xgu9i7/guide_to_installing_rotp_on_the_steam_deck_2022/). Its example versions may differ from your download; use the actual filename and Java 17 or newer.

## Building the Windows package from source

Use a JDK (Java 17 or newer) and Maven. The media-conversion tools `cwebp` and `oggenc` must be on `PATH`; Windows copies are included in the project root. From the project root in PowerShell:

```powershell
$env:Path = "$($PWD.Path);$env:Path"
mvn package -DskipTests -Dmaven.javadoc.skip=true
```

This packages the game without running the test suite. The outputs are in `target`; distribute the file ending in `-windows.zip`, including its bundled Java runtime.

**Do not skip the Maven Antrun media-conversion step when producing a Windows release.** The EXE wraps the mini JAR, which excludes PNG/JPG/WAV originals and relies on converted WebP/OGG files. A build using `-Dmaven.antrun.skip=true` can report success yet create an EXE that crashes on startup if converted media are absent.

When building from VS Code, temporarily disable Java auto-build (`java.autobuild.enabled`) while Maven runs. Both builders use `target/classes`; concurrent rebuilds can remove generated media and metadata. Restore the editor setting after packaging.

Before sharing a package, extract its ZIP into a new folder and launch the EXE there. Confirm the main menu opens using the bundled `jre`, and check the hotseat setup if distributing a hotseat build.
