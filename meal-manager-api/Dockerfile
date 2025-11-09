# ビルドステージ
FROM gradle:8.14.3-jdk21 AS build

WORKDIR /app

# Gradleのキャッシュを活用するため、依存関係を先にダウンロード
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle gradle
RUN gradle dependencies --no-daemon || true

# ソースコードをコピーしてビルド
COPY src ./src
RUN gradle clean build -x test --no-daemon

# 実行ステージ
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# ビルド成果物をコピー
COPY --from=build /app/build/libs/*.jar app.jar

# ヘルスチェック用のポート（Actuatorを使用する場合）
EXPOSE 8080

# アプリケーション実行
ENTRYPOINT ["java", "-jar", "app.jar"]
