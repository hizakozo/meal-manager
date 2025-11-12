# ビルドステージ
FROM gradle:8.14.3-jdk21 AS build

WORKDIR /app

# Gradleのキャッシュを活用するため、依存関係を先にダウンロード
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle gradle

# BuildKitのキャッシュマウントを使用して依存関係を高速化
RUN --mount=type=cache,target=/root/.gradle \
    gradle dependencies --no-daemon || true

# ソースコードをコピーしてビルド
COPY src ./src
RUN --mount=type=cache,target=/root/.gradle \
    gradle clean build -x test --no-daemon

# 実行ステージ
FROM eclipse-temurin:21-jre-alpine

# ヘルスチェック用にcurlをインストール
RUN apk add --no-cache curl

WORKDIR /app

# ビルド成果物をコピー
COPY --from=build /app/build/libs/*.jar app.jar

# ヘルスチェック用のポート（Actuatorを使用する場合）
EXPOSE 8080

# アプリケーション実行
ENTRYPOINT ["java", "-jar", "app.jar"]
