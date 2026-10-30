# ABJT-Auto-Bootup-Japaneese-Translater
## English
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

## Ollama

Keep the Ollama server running locally. The default endpoint is http://127.0.0.1:11434.

## Using AI
GPT-6 Luna (free)

## Testing Device
OS: Windows 11 Home (64bit)
CPU: Intel Core i7 13700F
RAM: DDR4 48GB (2133 MT/s)
GPU: NVIDIA GeForce RTX 3060 12GB
Storage: SSD 2.5TB

## Current status

WIP

## 日本語
現在の開発版での実装機能
ローカルの Ollama サーバー上の TranslateGemma 12B をデフォルトの翻訳エンジンとして使用 (translategemma:12b)。

プライマリエンジンとして Google 翻訳を選択可能、またはフォールバックとして使用。

Ollama へのリクエストは、次のエントリに進む前にモデルからの完全な応答を待機。

Minecraft の mods ディレクトリ内にある JAR ファイル内の assets/<modid>/lang/en_us.json をスキャン。

同じ JAR 内に assets/<modid>/lang/ja_jp.json が含まれている MOD はスキップ。

生成された言語ファイルを resourcepacks/ABJT-Japanese に書き込み。

翻訳キャッシュと除外 MOD のキャッシュを Minecraft の config ディレクトリに保存。

初回起動時のみ自動生成を実行（設定可能）；F8 キーまたはタイトル画面のボタンで手動再生成を開始。

再生成時は、以前に生成されたアセットを置換。

タイトル画面の設定および進捗画面に、現在の MOD、エントリ数、進捗状況、速度、および推定残り時間を表示。

## ビルド要件
Java 17
Minecraft 1.20.1
Minecraft Forge 47.4.28
Gradle 8.8

## Ollama
Ollama サーバーをローカルで実行し続けた状態にしてください。デフォルトのエンドポイントは [http://127.0.0.1:11434] です。

## 使用AI 
GPT-6 Luna (free)

## テスト環境
OS: Windows 11 Home (64bit)
CPU: Intel Core i7 13700F
RAM: DDR4 48GB (2133 MT/s)
GPU: NVIDIA GeForce RTX 3060 12GB
ストレージ: SSD 約2.5TB

## 開発状況
アルファ版完成

