# Meal Manager

献立管理アプリケーション

## 📚 API Documentation

Swagger UIでAPIドキュメントを確認できます：

**[📖 API Documentation (Swagger UI)](https://hizakozo.github.io/meal-manager/)**

## 技術スタック

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

## 開発環境

### 前提条件
- JDK 21
- Docker & Docker Compose

### セットアップ

1. リポジトリをクローン
   ```bash
   git clone https://github.com/hizakozo/meal-manager.git
   cd meal-manager
   ```

2. Docker環境を起動
   ```bash
   docker-compose up -d
   ```

3. アプリケーションをビルド
   ```bash
   cd meal-manager-api
   ./gradlew build
   ```

4. テストを実行
   ```bash
   ./gradlew test
   ```

## API開発手順

詳細な開発手順は [CLAUDE.md](./CLAUDE.md) を参照してください。

### 基本的な流れ

1. `docs/openapi.yaml` にエンドポイント定義を追加
2. `./gradlew openApiGenerate` でコードを生成
3. UseCase、Controller、テストを実装
4. `./gradlew test` でテストを実行

## ライセンス

MIT
