# ABJT-Auto-Bootup-Japaneese-Translater

Minecraft 1.20.1 / Forge 47.4.28 / Java 17 client mod that generates and applies Japanese language resource packs for installed mods.

## Implemented in the current development version

- TranslateGemma 12B through a local Ollama server is the default translation engine (translategemma:12b).
- Google Translate can be selected as the primary engine or used as a fallback.
- Ollama requests wait for the full model response before moving to the next entry.
- Scans assets/<modid>/lang/en_us.json inside JAR files in the Minecraft mods directory.
- Skips a MOD when the same JAR contains assets/<modid>/lang/ja_jp.json.
- Writes generated language files to resourcepacks/ABJT-Japanese.
- Stores translation cache and excluded-MOD cache in the Minecraft config directory.
- Runs automatic generation on the first launch only (configurable); F8 or the title-screen button starts manual regeneration.
- Regeneration replaces the previous generated assets.
- Title-screen settings and a progress screen show current MOD, entry counts, progress, speed, and estimated remaining time.

## Build requirements

- Java 17
- Minecraft 1.20.1
- Minecraft Forge 47.4.28
- Gradle 8.8

Run gradle build to build the mod. The jar is created under build/libs.

## Ollama setup

Install Ollama, then run: ollama pull translategemma:12b

Keep the Ollama server running locally. The default endpoint is http://127.0.0.1:11434.

## Current status

Development implementation is committed. GitHub Actions is running the first Forge build; treat the project as unverified until that build completes and the mod is tested in Minecraft.
