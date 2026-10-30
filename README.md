# ABJT-Auto-Bootup-Japaneese-Translater

Minecraft 1.20.1 / Forge 47.4.28 / Java 17 mod for generating and applying Japanese language resource packs for installed mods.

## Planned features
- TranslateGemma 12B through Ollama as the default translation provider; Google Translate as a secondary provider.
- Scan mod JAR files for `assets/<modid>/lang/en_us.json` and skip translation when a sibling `ja_jp.json` already exists.
- Cache translated entries and excluded mods, including mod file identity so updates can be detected.
- Generate and apply a Japanese resource pack.
- Automatically generate translations only on the first launch; use F8 for manual regeneration later.
- In-game settings accessible from the title screen.
- A separate progress window showing percentage, current mod, text counts, translation speed, and estimated remaining time.
- Wait for TranslateGemma generation to finish before marking translations complete.

## Build requirements
- Java 17
- Minecraft 1.20.1
- Minecraft Forge 47.4.28

## Status
Project repository initialized. Implementation is in progress.
