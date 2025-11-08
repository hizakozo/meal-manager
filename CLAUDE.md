# 技術スタック
### API
- Kotlin
- Spring Boot
- Arrow kt (Either)
### Database
- PostgreSQL

### Storage
- Amazon S3

### Distribution
- Amazon CloudFront

### Dev Infrastructure
- Docker

### Authentication
- Auth0 (JWT認証)

# APIの設計方針
軽量DDDに基づく

## ディレクトリ構成
#### controller: 
APIのエンドポイントを定義するコントローラー
${featureName}ControllerImpl.kt: 自動生成されたコントローラーの実装
UseCaseに依存する
  - generated: 自動生成されたコード
    - model: 自動生成されたデータモデル()
    - api: 自動生成されたAPIインターフェース
      - ${featureName}Controller.kt: 自動生成されたコントローラーでここにはRoute定義とControllerのインターフェースが含まれる
UseCaseの戻り値のOutPutの拡張関数としてOutPut.toResponse(): 生成されたモデル　を定義し、ControllerImplで使用する
#### useCase:
ファイル名：${featureName}UseCase.kt
アプリケーションロジックを実装するユースケース
UseCaseError: ユースケース固有のエラーを定義するinterface
１ユースケースあたり１ファイル
関数シグネチャ：execute(input: Input): Either<${featureName}UseCaseError, Output>
このinterfaceを継承したsealed class ${featureName}UseCaseErrorで具体的なエラーケースを定義する
#### domain:
ドメインモデルとビジネスロジックを定義する
どこにも依存しない純粋なKotlinコード
- model: ドメインモデルを定義する
  - repository: リポジトリインターフェースを定義する
  -　集約単位でrepositoryインターフェースを配置する 
  - ${集約}Repository.kt: リポジトリインターフェース
#### gateway:
repositoryインターフェースの実装
${集約}RepositoryImpl.kt
#### driver:
外部システムとの連携を担当する
${TableName}Driver.kt: データベースアクセスオブジェクト

## API作成手順

新しいAPIエンドポイントを作成する際は、以下の手順に従う：

### 1. OpenAPI定義を作成
`/Users/yasuikendo/dev/meal-manager/open-api/openapi.yaml` にエンドポイント定義を追加
- パス、HTTPメソッド、operationId、リクエスト/レスポンススキーマを定義
- スキーマは `components.schemas` に定義し、`$ref` で参照する

### 2. コード生成
```bash
./gradlew openApiGenerate
```
- `controller/gen/api` にControllerインターフェースとRouterが生成される
- `controller/gen/model` にリクエスト/レスポンスモデルが生成される

### 3. Domain層の実装（必要に応じて）
- ドメインモデル (`domain/model`)
- リポジトリインターフェース (`domain/repository`)
- 値オブジェクトのバリデーションロジック

### 4. Driver層の実装（必要に応じて）
- データベースアクセス (`driver/${TableName}DbDriver.kt`)
- R2DBCの`ReactiveCrudRepository`を継承
- カスタムINSERT/UPDATE用の`@Query`メソッドを定義
  ```kotlin
  @Query("""
      INSERT INTO table_name (column1, column2)
      VALUES (:value1, :value2)
      RETURNING *
  """)
  fun insert(value1: Type1, value2: Type2): Mono<EntityType>
  ```

### 5. Gateway層の実装（必要に応じて）
- リポジトリの実装 (`gateway/${集約}RepositoryImpl.kt`)
- Driverを使ってデータアクセスを実装
- Entity <-> Domain モデル変換ロジック

### 6. UseCase層の実装
- ファイル名: `useCase/${FeatureName}UseCase.kt`
- ビジネスロジックを実装
- 関数シグネチャ: `suspend fun execute(input: Input): Either<UseCaseError, Output>`
- エラー定義:
  ```kotlin
  sealed class ${FeatureName}UseCaseError(
      override val code: ErrorCode,
      override val message: String
  ) : UseCaseError

  sealed class ${FeatureName}UseCaseErrors {
      data class ValidationError(val domainError: DomainError) :
          ${FeatureName}UseCaseError(ErrorCode.VALIDATION_ERROR, domainError.message)
  }
  ```

#### Eitherのエラーハンドリング
- **`raise`は使用しない** - 代わりに`ensure`や`ensureNotNull`を使用する
- `ensure(条件) { エラー }`: 条件がfalseの場合にエラーを返す
  ```kotlin
  // ❌ Bad
  if (meal.userId != input.userId) {
      raise(GetMealUseCaseErrors.Forbidden())
  }

  // ✅ Good
  ensure(meal.userId == input.userId) {
      GetMealUseCaseErrors.Forbidden()
  }
  ```
- `ensureNotNull(値) { エラー }`: 値がnullの場合にエラーを返す
  ```kotlin
  // ❌ Bad
  val meal = mealRepository.findById(input.mealId)
      ?: raise(GetMealUseCaseErrors.MealNotFound())

  // ✅ Good
  val meal = ensureNotNull(mealRepository.findById(input.mealId)) {
      GetMealUseCaseErrors.MealNotFound()
  }
  ```

### 7. Controller層の実装
- ファイル名: `controller/${FeatureName}ControllerImpl.kt`
- 生成されたインターフェース (`I${FeatureName}Controller`) を実装
- `@Component` アノテーションを付与
- UseCaseを呼び出し、結果を変換してレスポンスを返す
- Output -> Response 変換用の拡張関数を定義:
  ```kotlin
  private fun Output.toResponse(): GeneratedResponse {
      return GeneratedResponse(...)
  }
  ```
- エラーハンドリングは `ErrorController.toResponse()` を使用

### 8. Integration Testの実装
- ファイル名: `test/kotlin/.../controller/${FeatureName}IntegrationTest.kt`
- `@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)`
- `@ActiveProfiles("local")`
- `WebTestClient` を使ってHTTPエンドポイントをテスト
- TestContainers (PostgreSQL + LocalStack S3) を使用
- テスト内容:
  - HTTPレスポンスの検証
  - データベースへのデータ永続化確認
  - S3へのファイル操作確認（画像関連の場合）
  - バリデーションエラーケース

### 9. テスト実行
```bash
./gradlew test --tests "パッケージ名.クラス名"
```

### 10. テスト結果を確認して修正
- 失敗したテストのレポートを確認: `build/reports/tests/test/index.html`
- エラー原因を特定して実装を修正
- 再度テスト実行

### 注意点
- R2DBCの`save()`メソッドは既存データのUPDATEを試みるため、新規作成時は必ずカスタム`insert()`メソッドを使用する
- 画像を扱う場合、`Image.create()`は新しいUUIDを生成するため、既存のimageIdを使う場合は`Image.of(imageId, uploadedAt)`を使用する
- エラーハンドリングは統一的に`ErrorController`を使用し、ErrorCodeに応じたHTTPステータスを返す
- **UseCase層では`try-catch`を使用しない** - Repository呼び出しやドメインサービス呼び出しに対してtry-catchでラップしない。例外は上位層で処理される

## API testing
Kotest + Testcontainersを使用する

## 認証・認可

### 認証方式
- Auth0によるJWT認証を使用
- Spring Security OAuth2 Resource Serverで実装

### JWT検証
- **Issuer検証**: JWTが正しいAuth0テナントから発行されたものかを検証
  - 設定: `spring.security.oauth2.resourceserver.jwt.issuer-uri`
  - 環境変数: `AUTH0_ISSUER`（例: `https://your-tenant.auth0.com/`）
- **Audience検証**: JWTがこのAPI向けに発行されたものかを検証
  - 設定: `auth0.audience`
  - 環境変数: `AUTH0_AUDIENCE`（例: `https://api.meal-manager.com`）

### 設定ファイル (application.yml)
```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: ${AUTH0_JWK_SET_URI:https://dev-auth0.auth0.com/.well-known/jwks.json}
          issuer-uri: ${AUTH0_ISSUER:https://dev-auth0.auth0.com/}

auth0:
  audience: ${AUTH0_AUDIENCE:https://api.meal-manager.com}
```

### ユーザーID管理
- Auth0の`sub`クレーム（例: `auth0|test-user-123`）をユーザー識別子として使用
- DBのusersテーブルに`auth0_sub`カラムでAuth0のsubを保存
- Controllerで`WithAuthController.getCurrentUserId()`を使用してユーザーIDを取得

### 認可処理
- **UseCase層で実装する** - 認可ロジックはビジネスロジックの一部
- リソースが要求ユーザーに属するかを検証
- 例: 食事詳細取得API
  ```kotlin
  suspend fun execute(input: GetMealInput): Either<GetMealUseCaseError, GetMealOutput> = either {
      val meal = ensureNotNull(mealRepository.findById(input.mealId)) {
          GetMealUseCaseErrors.MealNotFound()
      }

      // 認可チェック: 食事が要求ユーザーのものか確認
      ensure(meal.userId == input.userId) {
          GetMealUseCaseErrors.Forbidden()
      }

      // ... 処理続行
  }
  ```

### エラーレスポンス
- 認証エラー (401 Unauthorized): JWTが無効、期限切れ、署名検証失敗など
- 認可エラー (403 Forbidden): リソースへのアクセス権限がない
- リソース未検出 (404 Not Found): リソースが存在しない

### テスト設定
- `TestSecurityConfig`で認証を無効化せず、モックJWTを返すように設定
- トークン値に応じて異なるユーザーIDを返すことで、マルチユーザーのテストが可能
  ```kotlin
  @Bean
  @Primary
  fun testJwtDecoder(): ReactiveJwtDecoder {
      return ReactiveJwtDecoder { token ->
          val subject = when (token) {
              "other-user-token" -> "auth0|other-user"
              else -> "auth0|test-user-123"
          }
          // JWT生成...
      }
  }
  ```

## 画像のアップロードと配信

### 画像のアップロード
upload用のバケットと配信用のバケットを分ける
アップロード用バケットには直接アクセスせず、一時的な署名付きURLを発行してクライアントに提供する
upload用バケットから配信用バケットへのコピーはサーバー側で行う
upload用バケットは24時間で自動削除されるように設定する

#### アップロードの流れ
1. クライアントがアップロードリクエストを送信
2. サーバーがS3の署名付きURLを生成してクライアントに返す
3. クライアントが署名付きURLを使用してS3に画像をアップロード
4. アップロードが完了したら、サーバーに通知してメタデータをupload用バケットから配信用バケットにコピーする