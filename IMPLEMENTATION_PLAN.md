# Meal Manager アプリ 実装計画書

## 📋 プロジェクト概要

### プロジェクト名
Meal Manager - 献立管理アプリ

### 目的
食事の記録と管理を行うモバイルアプリケーション。ユーザーは食事の写真、料理名、調理日時、メモを記録できる。

### 技術スタック
- **言語**: Kotlin
- **UI**: Compose Multiplatform
- **プラットフォーム**: Android / iOS
- **認証**: Auth0 (JWT認証)
- **HTTP Client**: Ktor Client 3.0.3
- **状態管理**: ViewModel + StateFlow
- **ナビゲーション**: Jetpack Compose Navigation
- **画像処理**: Android Activity Result API
- **シリアライゼーション**: kotlinx.serialization

### バックエンドAPI
- **URL**: `https://api.meal-manager.com` (環境変数で設定)
- **認証**: Bearer Token (Auth0 JWT)
- **エンドポイント**:
  - `GET /meal-manager-api/meals`: 食事一覧取得
  - `GET /meal-manager-api/meals/{mealId}`: 食事詳細取得
  - `POST /meal-manager-api/meals`: 食事作成
  - `GET /meal-manager-api/images/upload-url`: 画像アップロード用URL取得
  - `POST /meal-manager-api/images/{imageId}/upload/complete`: 画像アップロード完了通知（サーバー側で自動実行）

---

## 🏗️ アーキテクチャ方針

詳細は `/Users/yasuikendo/dev/meal-manager/CLAUDE.md` の「クライアントアプリ（meal-manager-app）の設計方針」セクションを参照。

### 基本原則
1. **機能ベースのディレクトリ構成**: 機能（feature）ごとにコンテキストを分離
2. **シンプルなレイヤー構成**: Model + ViewModel + API呼び出し関数
3. **レイヤー分けしない**: Repository層、UseCase層などは作らない
4. **try-catchを使わない**: エラーハンドリングは上位層（ViewModel）に任せる

### ディレクトリ構成
```
composeApp/src/
├── commonMain/kotlin/com/kenstack/mealmanager/
│   ├── feature/
│   │   ├── auth/           # 認証機能（既存）
│   │   └── meal/           # 食事管理機能（新規）
│   │       ├── model/      # ドメインモデル、State定義
│   │       ├── api/        # API呼び出し関数
│   │       ├── ui/         # Compose UI画面
│   │       │   └── components/  # 再利用可能なUIコンポーネント
│   │       └── util/       # 機能内ユーティリティ
│   ├── infrastructure/
│   │   ├── http/           # Ktor Client設定（既存）
│   │   ├── auth/           # TokenManager（既存）
│   │   └── util/           # 共通ユーティリティ
│   └── navigation/         # ナビゲーション定義（新規）
└── androidMain/kotlin/com/kenstack/mealmanager/
    └── feature/meal/
        ├── viewmodel/      # ViewModel
        └── util/           # プラットフォーム固有ユーティリティ
```

---

## 📅 実装フェーズ

### ✅ Phase 0: API通信基盤（完了）
**所要時間**: 1時間

**実装内容**:
- [x] Ktor Client依存関係追加
- [x] HttpClientFactory実装
- [x] AuthInterceptor実装（Auth0トークン自動付与）

**実装ファイル**:
- `infrastructure/http/HttpClientFactory.kt`
- `infrastructure/http/AuthInterceptor.kt`

---

### 🔲 Phase 1: 基盤とモデル層
**所要時間**: 30分

**実装内容**:
1. Mealドメインモデル
2. 各画面のState定義
3. API呼び出し関数

#### 1.1 Mealドメインモデル

**ファイル**: `feature/meal/model/Meal.kt`

```kotlin
package com.kenstack.mealmanager.feature.meal.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Meal(
    val mealId: String,
    val dishName: String,
    val cookedAt: Instant,
    val memo: String,
    val imageId: String? = null,
    val imageUrl: String? = null,
    val recipeId: String? = null
)

@Serializable
data class MealListResponse(
    val meals: List<Meal>
)

@Serializable
data class CreateMealRequest(
    val dishName: String,
    val cookedAt: Instant,
    val memo: String,
    val imageId: String? = null,
    val recipeId: String? = null
)
```

**注意点**:
- `kotlinx.datetime.Instant`を使用してISO8601形式の日時を扱う
- `@Serializable`アノテーションでJSON自動変換

#### 1.2 画像アップロード関連モデル

**ファイル**: `feature/meal/model/ImageUpload.kt`

```kotlin
package com.kenstack.mealmanager.feature.meal.model

import kotlinx.serialization.Serializable

@Serializable
data class UploadUrlResponse(
    val imageId: String,
    val presignedUrl: String
)
```

#### 1.3 State定義

**ファイル**: `feature/meal/model/MealListState.kt`

```kotlin
package com.kenstack.mealmanager.feature.meal.model

sealed class MealListState {
    data object Loading : MealListState()
    data class Success(val meals: List<Meal>) : MealListState()
    data class Error(val message: String) : MealListState()
}
```

**ファイル**: `feature/meal/model/MealDetailState.kt`

```kotlin
package com.kenstack.mealmanager.feature.meal.model

sealed class MealDetailState {
    data object Loading : MealDetailState()
    data class Success(val meal: Meal) : MealDetailState()
    data class Error(val message: String) : MealDetailState()
}
```

**ファイル**: `feature/meal/model/MealCreateState.kt`

```kotlin
package com.kenstack.mealmanager.feature.meal.model

sealed class MealCreateState {
    data object Idle : MealCreateState()
    data object Saving : MealCreateState()
    data class Success(val mealId: String) : MealCreateState()
    data class Error(val message: String) : MealCreateState()
}

sealed class ImageUploadState {
    data object Idle : ImageUploadState()
    data object Uploading : ImageUploadState()
    data class Success(val imageId: String, val localUri: String) : ImageUploadState()
    data class Error(val message: String) : ImageUploadState()
}
```

#### 1.4 API呼び出し関数

**ファイル**: `feature/meal/api/MealApi.kt`

```kotlin
package com.kenstack.mealmanager.feature.meal.api

import com.kenstack.mealmanager.feature.meal.model.CreateMealRequest
import com.kenstack.mealmanager.feature.meal.model.Meal
import com.kenstack.mealmanager.feature.meal.model.MealListResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

suspend fun getMeals(client: HttpClient): List<Meal> {
    val response = client.get("/meal-manager-api/meals").body<MealListResponse>()
    return response.meals
}

suspend fun getMeal(client: HttpClient, mealId: String): Meal {
    return client.get("/meal-manager-api/meals/$mealId").body()
}

suspend fun createMeal(client: HttpClient, request: CreateMealRequest): Meal {
    return client.post("/meal-manager-api/meals") {
        setBody(request)
    }.body()
}
```

**ファイル**: `feature/meal/api/ImageApi.kt`

```kotlin
package com.kenstack.mealmanager.feature.meal.api

import com.kenstack.mealmanager.feature.meal.model.UploadUrlResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

suspend fun getUploadUrl(client: HttpClient): UploadUrlResponse {
    return client.get("/meal-manager-api/images/upload-url").body()
}

suspend fun uploadImageToS3(client: HttpClient, presignedUrl: String, imageData: ByteArray) {
    client.put(presignedUrl) {
        contentType(ContentType.Image.JPEG)
        setBody(imageData)
    }
}
```

---

### 🔲 Phase 2: ナビゲーション設定
**所要時間**: 30分

**実装内容**:
1. Navigation依存関係追加
2. ナビゲーショングラフ定義
3. ログイン成功後の画面遷移設定

#### 2.1 依存関係追加

**ファイル**: `gradle/libs.versions.toml`

```toml
[versions]
navigation-compose = "2.8.0"

[libraries]
navigation-compose = { module = "androidx.navigation:navigation-compose", version.ref = "navigation-compose" }
```

**ファイル**: `composeApp/build.gradle.kts`

```kotlin
commonMain.dependencies {
    // 既存の依存関係...
    implementation(libs.navigation.compose)
}
```

#### 2.2 ナビゲーショングラフ

**ファイル**: `navigation/MealNavigation.kt`

```kotlin
package com.kenstack.mealmanager.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kenstack.mealmanager.feature.auth.ui.LoginScreen
import com.kenstack.mealmanager.feature.meal.ui.MealListScreen
import com.kenstack.mealmanager.feature.meal.ui.MealDetailScreen
import com.kenstack.mealmanager.feature.meal.ui.MealCreateScreen

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object MealList : Screen("meal_list")
    data object MealDetail : Screen("meal_detail/{mealId}") {
        fun createRoute(mealId: String) = "meal_detail/$mealId"
    }
    data object MealCreate : Screen("meal_create")
}

@Composable
fun MealManagerNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Login.route
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.MealList.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.MealList.route) {
            MealListScreen(
                onMealClick = { mealId ->
                    navController.navigate(Screen.MealDetail.createRoute(mealId))
                },
                onCreateMealClick = {
                    navController.navigate(Screen.MealCreate.route)
                }
            )
        }

        composable(
            route = Screen.MealDetail.route,
            arguments = listOf(navArgument("mealId") { type = NavType.StringType })
        ) { backStackEntry ->
            val mealId = backStackEntry.arguments?.getString("mealId") ?: return@composable
            MealDetailScreen(
                mealId = mealId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.MealCreate.route) {
            MealCreateScreen(
                onSaveSuccess = { mealId ->
                    navController.navigate(Screen.MealDetail.createRoute(mealId)) {
                        popUpTo(Screen.MealList.route)
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
```

---

### 🔲 Phase 3: Meal一覧画面
**所要時間**: 60分

**実装内容**:
1. MealListViewModel
2. MealListScreen（グリッド表示）
3. MealGridItemコンポーネント

#### 3.1 ViewModel

**ファイル**: `feature/meal/viewmodel/MealListViewModel.kt` (androidMain)

```kotlin
package com.kenstack.mealmanager.feature.meal.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kenstack.mealmanager.feature.meal.api.getMeals
import com.kenstack.mealmanager.feature.meal.model.MealListState
import io.ktor.client.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MealListViewModel(
    private val httpClient: HttpClient
) : ViewModel() {

    private val _state = MutableStateFlow<MealListState>(MealListState.Loading)
    val state: StateFlow<MealListState> = _state.asStateFlow()

    init {
        loadMeals()
    }

    fun loadMeals() {
        viewModelScope.launch {
            _state.value = MealListState.Loading
            _state.value = try {
                val meals = getMeals(httpClient)
                MealListState.Success(meals)
            } catch (e: Exception) {
                MealListState.Error(e.message ?: "食事の取得に失敗しました")
            }
        }
    }
}
```

#### 3.2 UI画面

**ファイル**: `feature/meal/ui/MealListScreen.kt`

```kotlin
package com.kenstack.mealmanager.feature.meal.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kenstack.mealmanager.feature.meal.model.MealListState
import com.kenstack.mealmanager.feature.meal.ui.components.MealGridItem

@Composable
fun MealListScreen(
    viewModel: MealListViewModel,
    onMealClick: (String) -> Unit,
    onCreateMealClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Meal Manager") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateMealClick) {
                Icon(Icons.Default.Add, contentDescription = "食事を追加")
            }
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val currentState = state) {
                is MealListState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is MealListState.Success -> {
                    if (currentState.meals.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("まだ食事が登録されていません")
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = onCreateMealClick) {
                                Text("最初の食事を追加")
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(currentState.meals) { meal ->
                                MealGridItem(
                                    meal = meal,
                                    onClick = { onMealClick(meal.mealId) }
                                )
                            }
                        }
                    }
                }
                is MealListState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = currentState.message,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadMeals() }) {
                            Text("再試行")
                        }
                    }
                }
            }
        }
    }
}
```

#### 3.3 グリッドアイテムコンポーネント

**ファイル**: `feature/meal/ui/components/MealGridItem.kt`

```kotlin
package com.kenstack.mealmanager.feature.meal.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.kenstack.mealmanager.feature.meal.model.Meal
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.TimeZone

@Composable
fun MealGridItem(
    meal: Meal,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column {
            // 画像
            AsyncImage(
                model = meal.imageUrl,
                contentDescription = meal.dishName,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentScale = ContentScale.Crop
            )

            // 情報
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = meal.dishName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                val localDateTime = meal.cookedAt.toLocalDateTime(TimeZone.currentSystemDefault())
                Text(
                    text = "${localDateTime.year}/${localDateTime.monthNumber}/${localDateTime.dayOfMonth} ${localDateTime.hour}:${localDateTime.minute}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
```

**注意**:
- Coil（画像読み込みライブラリ）の依存関係追加が必要

---

### 🔲 Phase 4: Meal詳細画面
**所要時間**: 45分

**実装内容**:
1. MealDetailViewModel
2. MealDetailScreen

#### 4.1 ViewModel

**ファイル**: `feature/meal/viewmodel/MealDetailViewModel.kt` (androidMain)

```kotlin
package com.kenstack.mealmanager.feature.meal.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kenstack.mealmanager.feature.meal.api.getMeal
import com.kenstack.mealmanager.feature.meal.model.MealDetailState
import io.ktor.client.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MealDetailViewModel(
    private val httpClient: HttpClient,
    private val mealId: String
) : ViewModel() {

    private val _state = MutableStateFlow<MealDetailState>(MealDetailState.Loading)
    val state: StateFlow<MealDetailState> = _state.asStateFlow()

    init {
        loadMeal()
    }

    private fun loadMeal() {
        viewModelScope.launch {
            _state.value = MealDetailState.Loading
            _state.value = try {
                val meal = getMeal(httpClient, mealId)
                MealDetailState.Success(meal)
            } catch (e: Exception) {
                MealDetailState.Error(e.message ?: "食事の取得に失敗しました")
            }
        }
    }
}
```

#### 4.2 UI画面

**ファイル**: `feature/meal/ui/MealDetailScreen.kt`

```kotlin
package com.kenstack.mealmanager.feature.meal.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.kenstack.mealmanager.feature.meal.model.MealDetailState
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.TimeZone

@Composable
fun MealDetailScreen(
    viewModel: MealDetailViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("食事詳細") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "戻る")
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val currentState = state) {
                is MealDetailState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is MealDetailState.Success -> {
                    val meal = currentState.meal
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // 画像
                        meal.imageUrl?.let { imageUrl ->
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = meal.dishName,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 400.dp),
                                contentScale = ContentScale.Fit
                            )
                        }

                        // 情報
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = meal.dishName,
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            val localDateTime = meal.cookedAt.toLocalDateTime(TimeZone.currentSystemDefault())
                            Text(
                                text = "${localDateTime.year}年${localDateTime.monthNumber}月${localDateTime.dayOfMonth}日 ${localDateTime.hour}:${String.format("%02d", localDateTime.minute)}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = meal.memo,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
                is MealDetailState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = currentState.message,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onBackClick) {
                            Text("戻る")
                        }
                    }
                }
            }
        }
    }
}
```

---

### 🔲 Phase 5: Meal作成画面 + 画像アップロード
**所要時間**: 120分

**実装内容**:
1. 画像選択ユーティリティ（カメラ/ギャラリー）
2. S3アップロードユーティリティ
3. MealCreateViewModel
4. MealCreateScreen
5. ImagePickerコンポーネント

#### 5.1 画像選択ユーティリティ（Android）

**ファイル**: `feature/meal/util/ImagePickerUtil.kt` (androidMain)

```kotlin
package com.kenstack.mealmanager.feature.meal.util

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File

class ImagePickerUtil(private val context: Context) {

    fun createTempImageFile(): File {
        return File.createTempFile(
            "meal_image_${System.currentTimeMillis()}",
            ".jpg",
            context.cacheDir
        )
    }

    fun createImageUri(file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }
}

@Composable
fun rememberImagePicker(
    onImageSelected: (Uri) -> Unit
): ImagePicker {
    val context = LocalContext.current
    val util = remember { ImagePickerUtil(context) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // カメラで撮影成功
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { onImageSelected(it) }
    }

    return remember {
        ImagePicker(
            onLaunchCamera = {
                val file = util.createTempImageFile()
                val uri = util.createImageUri(file)
                cameraLauncher.launch(uri)
                uri
            },
            onLaunchGallery = {
                galleryLauncher.launch("image/*")
            }
        )
    }
}

data class ImagePicker(
    val onLaunchCamera: () -> Uri,
    val onLaunchGallery: () -> Unit
)
```

**AndroidManifest.xml に追加**:
```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

**res/xml/file_paths.xml**:
```xml
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <cache-path name="meal_images" path="." />
</paths>
```

#### 5.2 S3アップロードユーティリティ

**ファイル**: `feature/meal/util/S3Uploader.kt`

```kotlin
package com.kenstack.mealmanager.feature.meal.util

import android.content.Context
import android.net.Uri
import com.kenstack.mealmanager.feature.meal.api.getUploadUrl
import com.kenstack.mealmanager.feature.meal.api.uploadImageToS3
import io.ktor.client.*
import java.io.ByteArrayOutputStream

suspend fun uploadImage(
    context: Context,
    httpClient: HttpClient,
    imageUri: Uri
): String {
    // 1. 画像をByteArrayに変換
    val imageData = context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
        ByteArrayOutputStream().use { outputStream ->
            inputStream.copyTo(outputStream)
            outputStream.toByteArray()
        }
    } ?: throw IllegalArgumentException("画像の読み込みに失敗しました")

    // 2. presignedURL取得
    val uploadUrlResponse = getUploadUrl(httpClient)

    // 3. S3にアップロード
    uploadImageToS3(httpClient, uploadUrlResponse.presignedUrl, imageData)

    // 4. imageIdを返す
    return uploadUrlResponse.imageId
}
```

#### 5.3 ViewModel

**ファイル**: `feature/meal/viewmodel/MealCreateViewModel.kt` (androidMain)

```kotlin
package com.kenstack.mealmanager.feature.meal.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kenstack.mealmanager.feature.meal.api.createMeal
import com.kenstack.mealmanager.feature.meal.model.*
import com.kenstack.mealmanager.feature.meal.util.uploadImage
import io.ktor.client.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant

class MealCreateViewModel(
    private val context: Context,
    private val httpClient: HttpClient
) : ViewModel() {

    private val _createState = MutableStateFlow<MealCreateState>(MealCreateState.Idle)
    val createState: StateFlow<MealCreateState> = _createState.asStateFlow()

    private val _imageUploadState = MutableStateFlow<ImageUploadState>(ImageUploadState.Idle)
    val imageUploadState: StateFlow<ImageUploadState> = _imageUploadState.asStateFlow()

    fun uploadImage(imageUri: Uri) {
        viewModelScope.launch {
            _imageUploadState.value = ImageUploadState.Uploading
            _imageUploadState.value = try {
                val imageId = uploadImage(context, httpClient, imageUri)
                ImageUploadState.Success(imageId, imageUri.toString())
            } catch (e: Exception) {
                ImageUploadState.Error(e.message ?: "画像のアップロードに失敗しました")
            }
        }
    }

    fun createMeal(
        dishName: String,
        cookedAt: Instant,
        memo: String,
        imageId: String?
    ) {
        viewModelScope.launch {
            _createState.value = MealCreateState.Saving
            _createState.value = try {
                val request = CreateMealRequest(
                    dishName = dishName,
                    cookedAt = cookedAt,
                    memo = memo,
                    imageId = imageId
                )
                val meal = createMeal(httpClient, request)
                MealCreateState.Success(meal.mealId)
            } catch (e: Exception) {
                MealCreateState.Error(e.message ?: "食事の保存に失敗しました")
            }
        }
    }
}
```

#### 5.4 UI画面

**ファイル**: `feature/meal/ui/MealCreateScreen.kt`

```kotlin
package com.kenstack.mealmanager.feature.meal.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kenstack.mealmanager.feature.meal.model.ImageUploadState
import com.kenstack.mealmanager.feature.meal.model.MealCreateState
import com.kenstack.mealmanager.feature.meal.ui.components.ImagePicker
import com.kenstack.mealmanager.feature.meal.util.rememberImagePicker
import kotlinx.datetime.Clock

@Composable
fun MealCreateScreen(
    viewModel: MealCreateViewModel,
    onSaveSuccess: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var dishName by remember { mutableStateOf("") }
    var memo by remember { mutableStateOf("") }
    var cookedAt by remember { mutableStateOf(Clock.System.now()) }

    val createState by viewModel.createState.collectAsState()
    val imageUploadState by viewModel.imageUploadState.collectAsState()

    val imagePicker = rememberImagePicker { uri ->
        viewModel.uploadImage(uri)
    }

    LaunchedEffect(createState) {
        if (createState is MealCreateState.Success) {
            val mealId = (createState as MealCreateState.Success).mealId
            onSaveSuccess(mealId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("食事を追加") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "戻る")
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 画像選択
            ImagePicker(
                imageUploadState = imageUploadState,
                onCameraClick = { imagePicker.onLaunchCamera() },
                onGalleryClick = { imagePicker.onLaunchGallery() }
            )

            // 料理名
            OutlinedTextField(
                value = dishName,
                onValueChange = { if (it.length <= 100) dishName = it },
                label = { Text("料理名") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                supportingText = { Text("${dishName.length}/100") }
            )

            // メモ
            OutlinedTextField(
                value = memo,
                onValueChange = { if (it.length <= 1000) memo = it },
                label = { Text("メモ（任意）") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                supportingText = { Text("${memo.length}/1000") }
            )

            Spacer(modifier = Modifier.weight(1f))

            // 保存ボタン
            Button(
                onClick = {
                    val imageId = (imageUploadState as? ImageUploadState.Success)?.imageId
                    viewModel.createMeal(dishName, cookedAt, memo, imageId)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = dishName.isNotBlank() && createState !is MealCreateState.Saving
            ) {
                if (createState is MealCreateState.Saving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text("保存")
                }
            }
        }
    }
}
```

---

## 📦 必要な依存関係

### 追加が必要な依存関係

#### 1. Navigation
```toml
# libs.versions.toml
navigation-compose = "2.8.0"
navigation-compose = { module = "androidx.navigation:navigation-compose", version.ref = "navigation-compose" }
```

#### 2. Coil（画像読み込み）
```toml
# libs.versions.toml
coil = "2.5.0"
coil-compose = { module = "io.coil-kt:coil-compose", version.ref = "coil" }
```

#### 3. kotlinx-datetime（既に追加済み）
```toml
kotlinx-datetime = "0.4.1"
kotlinx-datetime = { module = "org.jetbrains.kotlinx:kotlinx-datetime", version.ref = "kotlinx-datetime" }
```

---

## ⚠️ 実装時の注意点

1. **try-catchを使わない**
   - API呼び出し関数では例外をそのまま投げる
   - ViewModelでcatchしてStateに変換

2. **日時の扱い**
   - `kotlinx.datetime.Instant`を使用
   - APIとの送受信はISO8601形式で自動変換
   - 表示時は`toLocalDateTime(TimeZone.currentSystemDefault())`で変換

3. **画像アップロード**
   - presignedURLは署名付きなので認証不要
   - S3へのPUTは`Content-Type: image/jpeg`を設定
   - アップロード成功後、imageIdを保持してPOST /mealsで送信

4. **ナビゲーション**
   - ログイン成功後はLoginScreenをback stackから削除
   - Meal作成成功後は詳細画面へ遷移し、作成画面をback stackから削除

5. **エラーハンドリング**
   - 各Stateでエラーメッセージを保持
   - UI側でエラーメッセージを表示 + リトライボタン

6. **Android権限**
   - カメラ: `CAMERA`権限
   - ギャラリー（Android 12以下）: `READ_EXTERNAL_STORAGE`権限
   - Activity Result APIで権限リクエスト

---

## 🧪 テスト戦略

### 手動テスト
1. ログイン → 一覧画面表示
2. 一覧が空の場合の表示確認
3. FABタップ → 作成画面表示
4. カメラで撮影 → アップロード成功
5. ギャラリーから選択 → アップロード成功
6. フォーム入力 → 保存 → 詳細画面表示
7. 一覧に追加された食事が表示されることを確認
8. 食事タップ → 詳細画面表示

### エラーケース
- ネットワークエラー時の挙動
- バリデーションエラーの表示
- 権限拒否時の動作

---

## 📅 実装スケジュール（推奨）

| フェーズ | 所要時間 | 累計 |
|---------|---------|------|
| Phase 1: 基盤とモデル | 30分 | 30分 |
| Phase 2: ナビゲーション | 30分 | 1時間 |
| Phase 3: 一覧画面 | 60分 | 2時間 |
| Phase 4: 詳細画面 | 45分 | 2時間45分 |
| Phase 5: 作成画面 | 120分 | 4時間45分 |
| Phase 6: 統合テスト | 30分 | 5時間15分 |

**合計: 約5-6時間**

---

## 🔗 関連ドキュメント

- [画面仕様書](docs/app-screens.md)
- [アーキテクチャ方針](CLAUDE.md)
- [TODOリスト](TODO.md)
- [API仕様](meal-manager-api/docs/openapi.yaml)

---

## 📞 サポート

実装中に不明な点があれば、以下を参照：
- CLAUDE.md: アーキテクチャ方針と設計ルール
- docs/app-screens.md: 画面仕様とUI/UXガイドライン
- TODO.md: 現在の進捗と次のタスク
