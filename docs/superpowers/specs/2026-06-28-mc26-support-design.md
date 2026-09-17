# MC 26系（26.1.2 / 26.2）対応 設計

- 日付: 2026-06-28
- 対象: CastleShift を Minecraft 26系（26.1.2, 26.2）に対応させる
- ローダー: Fabric / NeoForge / Forge（全3種）

## 背景

Minecraft が年ベースの新バージョニング（`1.21.x` → `26.x`）に移行した。現状の対応は
`1.20.1` および `1.21.1`〜`1.21.11`。本作業で 26系の 2 リリース系統を追加する。

リリース状況（2026-06-28 時点、Mojang version manifest で確認）:

- 26.1 系のパッチ: `26.1`, `26.1.1`, `26.1.2`（最新パッチは `26.1.2`）
- 26.2 系: `26.2`（パッチ無し、`26.3` はスナップショット）
- **対象は各系統の最新パッチ = `26.1.2` と `26.2`**。Forge / NeoForge / Fabric API は
  いずれも `26.1.2` にのみビルドを提供しており、`26.1.0`/`26.1.1` 向けの成果物は存在しないため、
  26.1 系は `26.1.2` を採る（既存も `1.21.11` のように最新パッチ単位で対応している方針と整合）。
- 各ローダーの提供状況:
  - Fabric API: `0.153.0+26.1.2`（26.1.2）, `0.153.0+26.2`（26.2）
  - NeoForge: `26.1.2.76`（正式）, `26.2.0.7-beta`
  - Forge: `26.1.2-64.0.10`（major 64）, `26.2-65.0.1`（major 65）
- **Java 要件: 26.1.2 / 26.2 ともに Java 25**（1.21.x の Java 21 から引き上げ）

## 方針

既存の「バージョンごとにディレクトリを複製し、`props/{version}.properties` で依存を切り替える」
マルチバージョン構成を踏襲する。ビルドは `target_mc_version` 文字列とディレクトリ名で駆動され、
`1.21.x` 形式に依存した数値比較を持たない（`forge_major_version` 等は props の明示値）ため、
`26.1.2` / `26.2` という新形式の文字列でもそのまま載る。

本質的な作業はディレクトリ複製ではなく、以下 2 つの不確実性の解消である:

- **A. ツールチェーンの 26系 / Java 25 対応**: Architectury Loom / ForgeGradle / architectury-plugin /
  Gradle wrapper が MC26・Java25・Forge65 を扱えるか。必要に応じてプラグイン版を更新する。
- **B. MC 26系の API 変更へのコード追従**: mixin・StructureProcessor 等、コンパイルして初めて判明する
  Java API / Mojang マッピング差分への修正。

## アプローチ（順序）: 縦切り先行

ツールチェーン互換性が最大のリスクなので、最小の縦切りでリスクを前倒しで潰す。

1. **26.2 × Fabric のみ** をスキャフォールドし、ビルド成立まで持っていく。
   ここで Java25 / Loom / architectury-plugin / Gradle のバージョン問題を解消する。
2. 同 26.2 に **NeoForge**・**Forge** を追加（ForgeGradle / NeoForge 固有のツールチェーン問題を解消）。
3. 確立した手順で **26.1.2**（全3ローダー）へ複製。
4. 既存バージョンの非回帰を確認。

## バージョンごとに追加する成果物

各バージョン（26.1.2, 26.2）について:

| 種別 | 追加物 |
|---|---|
| props | `props/26.1.2.properties`, `props/26.2.properties` |
| common | `common/26.1.2/`, `common/26.2/`（build.gradle + src + resources、`common/1.21.11` から複製し API 追従） |
| fabric | `fabric/26.1.2/`, `fabric/26.2/`（build.gradle の `commonModule` を `:common-26.x` へ + `fabric.mod.json`） |
| neoforge | `neoforge/26.1.2/`, `neoforge/26.2/`（build.gradle + gradle.properties + `META-INF/neoforge.mods.toml`） |
| forge | `forge/26.1.2/`, `forge/26.2/`（build.gradle + gradle.properties + `META-INF/mods.toml`） |
| 一覧 | `gradle/multi-version-tasks.gradle` の `supportedVersions` に `26.2`, `26.1.2` を追加 |

`run/` 配下や `.gradle/` などのランタイム生成物は複製しない（コミット対象外）。

### props の主な値

- `minecraft_version`: `26.1.2` / `26.2`
- `pack_format`: 各バージョンの jar 内 `version.json`（`pack_version.resource`）から実装時に確定
- `java_version`: **`25`**（toolchain が JDK25 を自動ダウンロード）
- `fabric_loader_version`: 最新版を確認して設定（現状最新は `0.19.3`）
- `fabric_api_version`: `0.153.0+26.1.2`（26.1.2） / `0.153.0+26.2`（26.2）
- `neoforge_version`: `26.1.2.76`（26.1.2） / `26.2.0.7-beta`（26.2）
- `forge_version` / `forge_major_version`: 26.1.2 → `64.0.10` / major `64`、26.2 → `65.0.1` / major `65`
  （いずれも 56 以上なので `forge/base-56`（EventBus 7）ソースセットを使用）
- `enabled_platforms`: `fabric,neoforge,forge`

### ローダー build.gradle の差し替え点

- 各ローダー build.gradle 内の `def commonModule = ':common-1.21.11'` を `:common-26.1.2` / `:common-26.2` へ。
- それ以外は `project.ext.minecraft_version` 経由で解決されるため変更不要（想定）。

## 未確定・リスク（実装中に解消する）

- Architectury Loom `1.13-SNAPSHOT` / architectury-plugin `3.4-SNAPSHOT` / ForgeGradle `[6.0.24,6.2)` が
  MC26・Java25・Forge65 を扱えるか。より新しい版が必要な可能性がある。Gradle wrapper 自体の
  Java25 toolchain 対応も要確認。これらはルート `build.gradle` / `gradle.properties` /
  `settings.gradle` のプラグイン版更新で対応する（全バージョン共通のため、既存バージョンへの
  非回帰を必ず確認する）。
- MC 26系の Java API / Mojang マッピング差分によるソース修正量。対象は主に
  `common/26.x/src/main/java/com/castleshift/` 配下の `CastleShift.java`,
  `mixin/StructureStartMixin.java`, `world/processor/*`。
- `forge/base-56` が Forge 64/65 でそのまま通るか、新たな base が必要か。
- `eventbus-validator` のアノテーションプロセッサ版（現状 `7.0-beta.10`）が Forge65 で適合するか。

## テスト / 検証

- 各バージョンでビルド成功:
  - `./gradlew build -Ptarget_mc_version=26.2`
  - `./gradlew build -Ptarget_mc_version=26.1.2`
- processor 単体テスト通過: `./gradlew test26_2` / `./gradlew test26_1_2`。
- 既存バージョンの非回帰: 代表として `./gradlew build1_21_11`（およびルートプラグイン版を変更した場合は
  `1.20.1` を含む複数版）が引き続き成功すること。

## スコープ外

- `26.3`（スナップショット）対応。
- `mod_version` / CHANGELOG の更新やリリース作業（別タスク）。
- 既存バージョンの機能変更・リファクタリング。
