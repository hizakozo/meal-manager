# Meal Manager

献立管理アプリケーション

## 📚 API Documentation

Swagger UIでAPIドキュメントを確認できます：

**[📖 API Documentation (Swagger UI)](https://hizakozo.github.io/meal-manager/)**

## 技術スタック

### Client
- Kotlin Multiplatform (KMP)
- Compose Multiplatform
- クロスプラットフォーム対応 (iOS / Android / Web / Desktop)

### API
- Kotlin
- Spring Boot (WebFlux)
- Arrow kt (Either)

### Database
- PostgreSQL
- R2DBC (Reactive Database Connectivity)

### Storage
- Amazon S3
- Amazon CloudFront

### Authentication
- Auth0 (JWT認証)

### Dev Infrastructure
- Docker
- Docker Compose
- Testcontainers

## プロジェクト構成

```
meal-manager/
├── meal-manager-api/    # バックエンドAPI (Spring Boot)
├── meal-manager-app/    # クライアントアプリ (Kotlin Multiplatform)
├── docs/                # API Documentation (Swagger UI)
└── docker-compose.yml   # 開発環境 (PostgreSQL, LocalStack)
```

## 開発環境

### 前提条件

#### バックエンド開発
- JDK 21
- Docker & Docker Compose

#### クライアント開発
- JDK 21
- IntelliJ IDEA Ultimate（推奨）または Android Studio
- Xcode（iOSビルドに必要、macOSのみ）

### セットアップ

#### バックエンドAPI

1. リポジトリをクローン
   ```bash
   git clone https://github.com/hizakozo/meal-manager.git
   cd meal-manager
   ```

2. Docker環境を起動
   ```bash
   docker-compose up -d
   ```

3. APIをビルド
   ```bash
   cd meal-manager-api
   ./gradlew build
   ```

4. テストを実行
   ```bash
   ./gradlew test
   ```

#### クライアントアプリ

1. プロジェクトをIDEで開く
   ```bash
   cd meal-manager-app
   # IntelliJ IDEA または Android Studio で開く
   ```

2. Androidアプリを実行
   ```bash
   ./gradlew :composeApp:run
   ```

3. iOSアプリを実行（macOSのみ）
   - IntelliJ IDEA: Run Configuration から `iosApp` を選択して実行
   - または Xcode で `iosApp/iosApp.xcodeproj` を開いて実行

## API開発手順

詳細な開発手順は [CLAUDE.md](./CLAUDE.md) を参照してください。

### 基本的な流れ

1. `docs/openapi.yaml` にエンドポイント定義を追加
2. `./gradlew openApiGenerate` でコードを生成
3. UseCase、Controller、テストを実装
4. `./gradlew test` でテストを実行

## ライセンス

MIT
