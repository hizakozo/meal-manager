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
- Auth0

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

## API testing
Kotest + Testcontainersを使用する

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