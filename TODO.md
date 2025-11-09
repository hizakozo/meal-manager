# Meal Manager アプリ 実装TODO

> 最終更新: 2025-11-10
>
> **プロジェクトステータス**: Phase 0-3完了、Phase 4以降未着手
>
> **関連ドキュメント**:
> - [画面仕様書](docs/app-screens.md)
> - [実装計画書](IMPLEMENTATION_PLAN.md)
> - [アーキテクチャ方針](CLAUDE.md)

---

## 📊 進捗サマリー

| フェーズ | ステータス | 進捗率 | 見積時間 | 実績時間 |
|---------|-----------|--------|---------|---------|
| Phase 0: API通信基盤 | ✅ 完了 | 100% | 1時間 | 1時間 |
| Phase 1: 基盤とモデル層 | ✅ 完了 | 100% | 30分 | 30分 |
| Phase 2: ナビゲーション設定 | ✅ 完了 | 100% | 30分 | 30分 |
| Phase 3: Meal一覧画面 | ✅ 完了 | 100% | 60分 | 90分 |
| Auth0ログイン統合 | ✅ 完了 | 100% | - | 120分 |
| Docker環境構築 | ✅ 完了 | 100% | - | 60分 |
| Phase 4: Meal詳細画面 | 🔲 未着手 | 0% | 45分 | - |
| Phase 5: Meal作成画面 | 🔲 未着手 | 0% | 120分 | - |
| Phase 6: 統合テスト | 🔲 未着手 | 0% | 30分 | - |

**合計進捗**: 73% (5時間30分 / 7時間30分)

---

## ✅ Phase 0: API通信基盤（完了）

### 依存関係
- [x] Ktor Client依存関係追加（ktor 3.0.3）
- [x] kotlinx.serialization プラグイン追加

### 実装ファイル
- [x] `infrastructure/http/HttpClientFactory.kt`
  - HttpClient作成
  - JSON設定（Content Negotiation）
  - ログ設定
  - タイムアウト設定
- [x] `infrastructure/http/AuthInterceptor.kt`
  - Auth0トークンを自動付与するインターセプター

### 動作確認
- [x] コンパイルテスト成功

---

## ✅ Phase 1: 基盤とモデル層（完了）

**見積時間**: 30分 | **実績時間**: 30分

### ドメインモデル

- [x] `feature/meal/model/Meal.kt`
  - [x] `Meal` data class
  - [x] `Image` data class
  - [x] `User` data class

### State定義

- [x] `feature/meal/model/MealListState.kt`
  - [x] `Loading` state
  - [x] `Success` state
  - [x] `Error` state

- [x] `feature/auth/model/AuthState.kt`
  - [x] `Initial` state
  - [x] `Unauthenticated` state
  - [x] `Authenticated` state
  - [x] `LoggingIn` state
  - [x] `LoggingOut` state
  - [x] `Error` state

### API呼び出し関数

- [x] `feature/meal/api/MealApi.kt`
  - [x] `getMeals()`: 食事一覧取得

- [x] `infrastructure/http/HttpClientFactory.kt`
  - [x] HttpClient作成
  - [x] 認証トークン自動付与

### 動作確認
- [x] コンパイルエラーがないこと
- [x] Serialization が正しく動作すること

---

## ✅ Phase 2: ナビゲーション設定（完了）

**見積時間**: 30分 | **実績時間**: 30分

### 依存関係追加

- [x] Navigation Compose依存関係追加
  - [x] `gradle/libs.versions.toml` に追加
  - [x] `composeApp/build.gradle.kts` に追加（androidMain専用）

### ナビゲーション実装

- [x] `navigation/Screen.kt`
  - [x] `Screen` sealed class定義
    - [x] `Login` route
    - [x] `MealList` route
    - [x] `MealDetail` route（mealIdパラメータ）
    - [x] `MealCreate` route

### MainActivity更新

- [x] `MainActivity.kt`
  - [x] NavHost統合
  - [x] 初期画面をLoginに設定
  - [x] 画面遷移ロジック実装

### 動作確認
- [x] アプリ起動でログイン画面が表示されること
- [x] ログイン成功で一覧画面へ遷移すること

---

## ✅ Phase 3: Meal一覧画面（完了）

**見積時間**: 60分 | **実績時間**: 90分

### 依存関係追加

- [x] Coil（画像読み込みライブラリ）
  - [x] `gradle/libs.versions.toml` に追加（Coil 3.0.4）
  - [x] `composeApp/build.gradle.kts` に追加
  - [x] Material Icons Extended追加

### ViewModel実装

- [x] `feature/meal/viewmodel/MealListViewModel.kt` (androidMain)
  - [x] HttpClient統合
  - [x] `loadMeals()` 実装
  - [x] `refresh()` 実装
  - [x] StateFlow<MealListState> 実装
  - [x] エラーハンドリング

### UI実装

- [x] `feature/meal/ui/MealListScreen.kt`
  - [x] Scaffold + TopAppBar
  - [x] FloatingActionButton（右下）
  - [x] Loading状態表示
  - [x] Error状態表示（リトライボタン付き）
  - [x] Empty状態表示
  - [x] Success: LazyVerticalGrid（2カラム）

- [x] `feature/meal/ui/components/MealGridItem.kt`
  - [x] Card + AsyncImage
  - [x] 料理名表示
  - [x] 調理日時表示（フォーマット関数付き）
  - [x] クリックハンドラー
  - [x] プレースホルダーアイコン

### 動作確認
- [x] ログイン後、一覧画面が表示されること
- [x] ローディング表示が機能すること
- [x] 空状態が正しく表示されること
- [x] エラー時にリトライボタンが表示されること
- [x] FABタップで作成画面へ遷移すること（ルート定義済み）
- [x] APIサーバー接続成功（Docker環境）

---

## 🔲 Phase 4: Meal詳細画面（未着手）

**見積時間**: 45分

### ViewModel実装

- [ ] `feature/meal/viewmodel/MealDetailViewModel.kt` (androidMain)
  - [ ] mealIdを受け取る
  - [ ] `loadMeal()` 実装
  - [ ] StateFlow<MealDetailState> 実装
  - [ ] エラーハンドリング

### UI実装

- [ ] `feature/meal/ui/MealDetailScreen.kt`
  - [ ] Scaffold + TopAppBar（戻るボタン）
  - [ ] Loading状態表示
  - [ ] Error状態表示
  - [ ] Success:
    - [ ] 画像表示（AsyncImage、横幅いっぱい）
    - [ ] 料理名表示
    - [ ] 調理日時表示
    - [ ] メモ表示

### 動作確認
- [ ] 一覧からMealタップで詳細画面が表示されること
- [ ] 画像が正しく表示されること
- [ ] 画像がない場合はプレースホルダー表示
- [ ] 戻るボタンで一覧に戻ること
- [ ] 作成後の遷移で詳細画面が表示されること

---

## 🔲 Phase 5: Meal作成画面（未着手）

**見積時間**: 120分

### Android設定

- [ ] AndroidManifest.xmlにFileProvider追加
  - [ ] `<provider>` 定義
  - [ ] authorities設定

- [ ] `res/xml/file_paths.xml` 作成
  - [ ] cache-path設定

- [ ] AndroidManifest.xmlに権限追加
  - [ ] `CAMERA` 権限
  - [ ] `READ_EXTERNAL_STORAGE` 権限（Android 12以下）

### ユーティリティ実装

- [ ] `feature/meal/util/ImagePickerUtil.kt` (androidMain)
  - [ ] `ImagePickerUtil` class
    - [ ] `createTempImageFile()`
    - [ ] `createImageUri()`
  - [ ] `rememberImagePicker()` Composable
    - [ ] カメラランチャー
    - [ ] ギャラリーランチャー
    - [ ] 権限リクエスト

- [ ] `feature/meal/util/S3Uploader.kt`
  - [ ] `uploadImage()`: 画像アップロードフロー全体
    - [ ] UriからByteArray変換
    - [ ] presignedURL取得
    - [ ] S3アップロード
    - [ ] imageId返却

### ViewModel実装

- [ ] `feature/meal/viewmodel/MealCreateViewModel.kt` (androidMain)
  - [ ] `uploadImage()` 実装
  - [ ] `createMeal()` 実装
  - [ ] StateFlow<MealCreateState> 実装
  - [ ] StateFlow<ImageUploadState> 実装
  - [ ] エラーハンドリング

### UI実装

- [ ] `feature/meal/ui/components/ImagePicker.kt`
  - [ ] 画像プレビューエリア
  - [ ] 「カメラで撮影」ボタン
  - [ ] 「ギャラリーから選択」ボタン
  - [ ] ローディング表示
  - [ ] エラー表示

- [ ] `feature/meal/ui/MealCreateScreen.kt`
  - [ ] Scaffold + TopAppBar（戻るボタン）
  - [ ] ImagePicker コンポーネント
  - [ ] 料理名入力（OutlinedTextField、100文字制限）
  - [ ] 調理日時選択（DateTimePicker、将来実装）
  - [ ] メモ入力（OutlinedTextField、1000文字制限）
  - [ ] 保存ボタン（バリデーション、ローディング）
  - [ ] エラー表示（Snackbar）

### 動作確認
- [ ] FABから作成画面へ遷移すること
- [ ] カメラで撮影できること
- [ ] ギャラリーから画像選択できること
- [ ] 画像アップロードが成功すること
- [ ] アップロード中のローディング表示
- [ ] フォームバリデーションが機能すること
- [ ] 保存が成功し、詳細画面へ遷移すること
- [ ] エラーメッセージが正しく表示されること

---

## ✅ Auth0ログイン統合（完了）

**見積時間**: - | **実績時間**: 120分

### Auth0設定

- [x] Auth0アプリケーション設定
  - [x] Callback URL設定
  - [x] Allowed Web Origins設定
  - [x] 環境変数管理（.env）

### 実装

- [x] `infrastructure/auth/AuthManager.kt`
  - [x] WebAuthProvider統合
  - [x] login() 実装
  - [x] logout() 実装
  - [x] refreshToken() 実装

- [x] `infrastructure/auth/TokenManager.kt`
  - [x] トークン保存・取得
  - [x] 有効期限チェック
  - [x] JWTデコード（ユーザーID取得）

- [x] `infrastructure/auth/SecureStorage.kt`
  - [x] Android EncryptedSharedPreferences

- [x] `feature/auth/ui/LoginViewModel.kt`
  - [x] checkAuthStatus() 実装
  - [x] login() 実装
  - [x] logout() 実装
  - [x] AuthState管理

- [x] `feature/auth/ui/LoginScreen.kt`
  - [x] ログインボタン
  - [x] ログアウトボタン
  - [x] 状態表示

### 解決した課題

- [x] DNS解決問題の回避（JWTローカルデコード）
- [x] getUserInfo()タイムアウト問題の解決
- [x] 自動ログイン機能実装

### 動作確認

- [x] Auth0ブラウザログイン成功
- [x] トークン保存・取得成功
- [x] 自動ログイン機能動作
- [x] ログイン後の画面遷移成功

---

## ✅ Docker環境構築（完了）

**見積時間**: - | **実績時間**: 60分

### Docker設定

- [x] `docker-compose.yml` 作成
  - [x] PostgreSQL設定
  - [x] LocalStack（S3）設定
  - [x] APIサーバー設定
  - [x] ネットワーク設定
  - [x] ヘルスチェック設定

- [x] `meal-manager-api/Dockerfile` 作成
  - [x] マルチステージビルド
  - [x] Gradle依存関係キャッシュ最適化

- [x] `.dockerignore` 作成

- [x] 環境変数管理
  - [x] `.env` ファイル作成
  - [x] .gitignore設定

### 起動確認

- [x] PostgreSQL起動成功（localhost:5433）
- [x] LocalStack起動成功（localhost:4566）
- [x] APIサーバー起動成功（localhost:8080）
- [x] Liquibaseマイグレーション成功
- [x] クライアントからAPI接続成功

### 解決した課題

- [x] ディスク容量不足（Gradleキャッシュクリア）
- [x] Auth0環境変数の引き渡し
- [x] コンテナ間通信設定

---

## 🔲 Phase 6: 統合テスト（未着手）

**見積時間**: 30分

### テストシナリオ

#### シナリオ1: 新規ユーザーの初回利用
- [ ] アプリ起動
- [ ] ログイン画面表示
- [ ] Auth0でログイン
- [ ] 一覧画面（空状態）表示
- [ ] FABタップ
- [ ] 作成画面表示
- [ ] カメラで撮影
- [ ] 画像アップロード成功
- [ ] フォーム入力
- [ ] 保存成功
- [ ] 詳細画面表示
- [ ] 戻るボタンで一覧へ
- [ ] 作成した食事が一覧に表示される

#### シナリオ2: 既存ユーザーの一覧閲覧
- [ ] ログイン
- [ ] 一覧画面に食事が表示される
- [ ] Mealタップ
- [ ] 詳細画面表示
- [ ] 戻るで一覧へ

#### シナリオ3: ギャラリーから画像選択
- [ ] 作成画面へ遷移
- [ ] ギャラリーボタンタップ
- [ ] 画像選択
- [ ] アップロード成功
- [ ] プレビュー表示

#### シナリオ4: エラーケース
- [ ] ネットワークエラー時の表示確認
- [ ] バリデーションエラーの表示
- [ ] 画像アップロード失敗時の表示
- [ ] リトライボタンの動作

### パフォーマンステスト
- [ ] 一覧画面のスクロールが滑らかか
- [ ] 画像読み込みが適切にキャッシュされているか
- [ ] メモリリークがないか

---

## 📝 次にやること

1. **Phase 4: Meal詳細画面の実装**
   - MealDetailViewModel の実装
   - MealDetailScreen UI実装
   - API関数 getMeal() 実装

2. **Phase 5: Meal作成画面の実装**
   - 画像選択機能（カメラ/ギャラリー）
   - S3アップロード機能
   - MealCreateViewModel 実装
   - MealCreateScreen UI実装

3. **Phase 6: 統合テスト**
   - エンドツーエンドテスト
   - エラーケーステスト

---

## 🚧 既知の課題・TODO

### 将来実装予定
- [ ] 日時選択UI（DateTimePicker）の実装
- [ ] Meal編集機能
- [ ] Meal削除機能
- [ ] オフライン対応
- [ ] 画像の圧縮処理
- [ ] Pull-to-Refresh実装
- [ ] ページネーション（一覧が多い場合）

### 改善案
- [ ] エラーメッセージの多言語対応
- [ ] ダークモード対応
- [ ] アクセシビリティ対応

---

## 💡 メモ

### 技術的な決定事項
- 日時は`kotlinx.datetime.Instant`を使用
- 画像読み込みは Coil を使用
- ナビゲーションは Compose Navigation を使用
- エラーハンドリングは try-catch を使わず、ViewModel で State に変換

### 参考リンク
- [Compose Navigation](https://developer.android.com/jetpack/compose/navigation)
- [Coil](https://coil-kt.github.io/coil/compose/)
- [kotlinx.datetime](https://github.com/Kotlin/kotlinx-datetime)
- [Ktor Client](https://ktor.io/docs/client.html)

---

**最終更新**: 2025-11-10 by Claude Code

## 📌 重要なリンク

### Docker操作
```bash
# すべて起動
docker-compose up -d

# ログ確認
docker-compose logs -f api

# 再起動
docker-compose restart api

# 停止
docker-compose down

# 完全削除（ボリュームも）
docker-compose down -v
```

### コンパイルチェック
```bash
# Androidアプリ
cd meal-manager-app
./gradlew :composeApp:compileDebugKotlinAndroid --no-daemon

# APIサーバー
cd meal-manager-api
./gradlew compileKotlin --no-daemon
```
