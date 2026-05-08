# Implementation Plan: Tentuin Agent Android App
**Target AI**: Gemini in Android Studio  
**Language**: Kotlin + Jetpack Compose  
**Architecture**: MVVM + Hilt DI + Retrofit (identical to `apps/student-android`)

---

## 1. PROJECT SETUP

### App Identity
```
applicationId  : id.tentuin.agent
namespace      : id.tentuin.agent
Package prefix : id.tentuin.agent
App name       : Tentuin Agen
minSdk         : 24
targetSdk      : 34
compileSdk     : 34
```

### Supabase Credentials (copy ke `gradle.properties`)
```properties
SUPABASE_URL=https://dmupwcjrsnydkunwuaex.supabase.co
SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImRtdXB3Y2pyc255ZGt1bnd1YWV4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzY0OTc2MDksImV4cCI6MjA5MjA3MzYwOX0.1Msoj9JWpg-u2ljy4tLR_Xzm-vyw0ic0VJMs557N9WU
```

### `settings.gradle.kts`
```kotlin
rootProject.name = "agent-android"
include(":app")
```

### Root `build.gradle.kts`
```kotlin
plugins {
    id("com.android.application") version "8.4.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
    id("com.google.dagger.hilt.android") version "2.51.1" apply false
}
```

### App-level `app/build.gradle.kts`
```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "id.tentuin.agent"
    compileSdk = 34

    defaultConfig {
        applicationId = "id.tentuin.agent"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        val supabaseUrl = (project.findProperty("SUPABASE_URL") as String?) ?: ""
        val supabaseAnonKey = (project.findProperty("SUPABASE_ANON_KEY") as String?) ?: ""
        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabaseAnonKey\"")
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
        )
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")

    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.ui:ui-text-google-fonts")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Retrofit + OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Coil (image)
    implementation("io.coil-kt:coil-compose:2.6.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
```

---

## 2. FULL DIRECTORY STRUCTURE

```
app/src/main/
├── AndroidManifest.xml
└── java/id/tentuin/agent/
    ├── AgentApp.kt                          (Hilt Application)
    ├── core/
    │   ├── datastore/
    │   │   └── SessionDataStore.kt
    │   ├── di/
    │   │   ├── DataStoreModule.kt
    │   │   └── NetworkModule.kt
    │   └── network/
    │       ├── AgentApi.kt                  (Retrofit interface)
    │       └── AuthInterceptor.kt
    ├── data/
    │   ├── model/
    │   │   ├── AgentModels.kt
    │   │   ├── AuthModels.kt
    │   │   ├── ClaimModels.kt
    │   │   ├── CommissionModels.kt
    │   │   └── SchoolModels.kt
    │   └── repository/
    │       ├── AgentRepository.kt
    │       ├── AuthRepository.kt
    │       ├── ClaimRepository.kt
    │       ├── CommissionRepository.kt
    │       └── SchoolRepository.kt
    └── ui/
        ├── MainActivity.kt
        ├── navigation/
        │   ├── AgentNavHost.kt
        │   ├── BottomNavBar.kt
        │   └── Route.kt
        ├── screen/
        │   ├── auth/
        │   │   ├── LoginScreen.kt
        │   │   ├── RegisterScreen.kt
        │   │   └── AuthViewModel.kt
        │   ├── dashboard/
        │   │   ├── DashboardScreen.kt
        │   │   └── DashboardViewModel.kt
        │   ├── claim/
        │   │   ├── ClaimSchoolScreen.kt
        │   │   ├── ClaimUniversityScreen.kt
        │   │   └── ClaimViewModel.kt
        │   ├── portfolio/
        │   │   ├── PortfolioSchoolScreen.kt
        │   │   ├── PortfolioUniversityScreen.kt
        │   │   └── PortfolioViewModel.kt
        │   ├── commission/
        │   │   ├── CommissionScreen.kt
        │   │   └── CommissionViewModel.kt
        │   └── withdrawal/
        │       ├── WithdrawalScreen.kt
        │       └── WithdrawalViewModel.kt
        ├── component/
        │   ├── AgentCard.kt
        │   ├── CommissionCard.kt
        │   ├── SchoolCard.kt
        │   ├── UniversityClaimCard.kt
        │   ├── ProgressBar.kt
        │   ├── StatCard.kt
        │   ├── SkeletonBox.kt
        │   ├── EmptyState.kt
        │   └── TentuinButton.kt
        └── theme/
            ├── Color.kt
            ├── Theme.kt
            └── Type.kt
```

---

## 3. ANDROIDMANIFEST.XML

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />

    <application
        android:name=".AgentApp"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:label="Tentuin Agen"
        android:supportsRtl="true"
        android:theme="@style/Theme.TentuinAgent">
        <activity
            android:name=".ui.MainActivity"
            android:exported="true"
            android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```

---

## 4. ENTRYPOINT FILES

### `AgentApp.kt`
```kotlin
package id.tentuin.agent

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AgentApp : Application()
```

### `ui/MainActivity.kt`
```kotlin
package id.tentuin.agent.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import id.tentuin.agent.ui.navigation.AgentNavHost
import id.tentuin.agent.ui.navigation.BottomNavBar
import id.tentuin.agent.ui.navigation.bottomNavRoutes
import id.tentuin.agent.ui.theme.TentuinAgentTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TentuinAgentTheme {
                val navController = rememberNavController()
                val backStack by navController.currentBackStackEntryAsState()
                val currentRoute = backStack?.destination?.route

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (bottomNavRoutes.any { currentRoute?.startsWith(it) == true }) {
                            BottomNavBar(navController = navController)
                        }
                    }
                ) { innerPadding ->
                    AgentNavHost(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
```

---

## 5. NAVIGATION

### `navigation/Route.kt`
```kotlin
package id.tentuin.agent.ui.navigation

sealed class Route(val route: String) {
    object Login              : Route("auth/login")
    object Register           : Route("auth/register")

    // Bottom nav tabs
    object Dashboard          : Route("main/dashboard")
    object ClaimSchool        : Route("main/claim/school")
    object ClaimUniversity    : Route("main/claim/university")
    object Portfolio          : Route("main/portfolio")
    object Commission         : Route("main/commission")

    // Portfolio sub-screens
    object PortfolioSchool    : Route("main/portfolio/school/{schoolId}") {
        fun createRoute(schoolId: String) = "main/portfolio/school/$schoolId"
    }
    object PortfolioUniversity : Route("main/portfolio/university/{universityId}") {
        fun createRoute(universityId: String) = "main/portfolio/university/$universityId"
    }

    // Withdrawal
    object Withdrawal         : Route("main/withdrawal")
}

val bottomNavRoutes = listOf(
    Route.Dashboard.route,
    Route.ClaimSchool.route,
    Route.ClaimUniversity.route,
    Route.Portfolio.route,
    Route.Commission.route,
)
```

### `navigation/AgentNavHost.kt`
```kotlin
package id.tentuin.agent.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import id.tentuin.agent.ui.screen.auth.LoginScreen
import id.tentuin.agent.ui.screen.auth.RegisterScreen
import id.tentuin.agent.ui.screen.claim.ClaimSchoolScreen
import id.tentuin.agent.ui.screen.claim.ClaimUniversityScreen
import id.tentuin.agent.ui.screen.commission.CommissionScreen
import id.tentuin.agent.ui.screen.dashboard.DashboardScreen
import id.tentuin.agent.ui.screen.portfolio.PortfolioSchoolScreen
import id.tentuin.agent.ui.screen.portfolio.PortfolioUniversityScreen
import id.tentuin.agent.ui.screen.withdrawal.WithdrawalScreen

@Composable
fun AgentNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Route.Login.route,
        modifier = modifier,
    ) {
        composable(Route.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(Route.Register.route) {
            RegisterScreen(navController = navController)
        }
        composable(Route.Dashboard.route) {
            DashboardScreen(navController = navController)
        }
        composable(Route.ClaimSchool.route) {
            ClaimSchoolScreen(navController = navController)
        }
        composable(Route.ClaimUniversity.route) {
            ClaimUniversityScreen(navController = navController)
        }
        composable(Route.Portfolio.route) {
            // Default: tampilkan list sekolah diklaim (tab pertama)
            PortfolioSchoolScreen(navController = navController)
        }
        composable(
            route = Route.PortfolioSchool.route,
            arguments = listOf(navArgument("schoolId") { type = NavType.StringType })
        ) { back ->
            val schoolId = back.arguments?.getString("schoolId") ?: ""
            PortfolioSchoolScreen(navController = navController, schoolId = schoolId)
        }
        composable(
            route = Route.PortfolioUniversity.route,
            arguments = listOf(navArgument("universityId") { type = NavType.StringType })
        ) { back ->
            val universityId = back.arguments?.getString("universityId") ?: ""
            PortfolioUniversityScreen(navController = navController, universityId = universityId)
        }
        composable(Route.Commission.route) {
            CommissionScreen(navController = navController)
        }
        composable(Route.Withdrawal.route) {
            WithdrawalScreen(navController = navController)
        }
    }
}
```

### `navigation/BottomNavBar.kt`
```kotlin
package id.tentuin.agent.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import id.tentuin.agent.ui.theme.Primary
import id.tentuin.agent.ui.theme.TextMuted
import id.tentuin.agent.ui.theme.Surface

private data class NavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val items = listOf(
    NavItem(Route.Dashboard.route,       "Dashboard",   Icons.Filled.Dashboard,  Icons.Outlined.Dashboard),
    NavItem(Route.ClaimSchool.route,     "Sekolah",     Icons.Filled.School,     Icons.Outlined.School),
    NavItem(Route.ClaimUniversity.route, "Kampus",      Icons.Filled.AccountBalance, Icons.Outlined.AccountBalance),
    NavItem(Route.Portfolio.route,       "Portfolio",   Icons.Filled.Folder,     Icons.Outlined.FolderOpen),
    NavItem(Route.Commission.route,      "Komisi",      Icons.Filled.Payments,   Icons.Outlined.Payments),
)

@Composable
fun BottomNavBar(navController: NavController) {
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    NavigationBar(containerColor = Surface, tonalElevation = 0.dp) {
        items.forEach { item ->
            val selected = currentRoute?.startsWith(item.route.substringBefore("/")) == true ||
                           currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(Route.Dashboard.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = (item.route != Route.Dashboard.route)
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label, style = TentuinAgentTypography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Primary,
                    selectedTextColor = Primary,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted,
                    indicatorColor = Primary.copy(alpha = 0.12f),
                )
            )
        }
    }
}
```

---

## 6. THEME

### `theme/Color.kt`
```kotlin
package id.tentuin.agent.ui.theme

import androidx.compose.ui.graphics.Color

val Primary       = Color(0xFF6C63FF)
val PrimaryLight  = Color(0xFFEEEDFF)
val PrimaryDark   = Color(0xFF4F46E5)
val Background    = Color(0xFFF7F8FA)
val Surface       = Color(0xFFFFFFFF)
val SurfaceVar    = Color(0xFFF3F4F6)
val TextPrimary   = Color(0xFF111827)
val TextSub       = Color(0xFF374151)
val TextMuted     = Color(0xFF9CA3AF)
val Border        = Color(0xFFE5E7EB)
val Success       = Color(0xFF10B981)
val SuccessLight  = Color(0xFFD1FAE5)
val Error         = Color(0xFFEF4444)
val ErrorLight    = Color(0xFFFEE2E2)
val Warning       = Color(0xFFF59E0B)
val WarningLight  = Color(0xFFFEF3C7)
```

### `theme/Theme.kt`
```kotlin
package id.tentuin.agent.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val AgentColorScheme = lightColorScheme(
    primary            = Primary,
    onPrimary          = androidx.compose.ui.graphics.Color.White,
    primaryContainer   = PrimaryLight,
    onPrimaryContainer = PrimaryDark,
    background         = Background,
    onBackground       = TextPrimary,
    surface            = Surface,
    onSurface          = TextPrimary,
    surfaceVariant     = SurfaceVar,
    onSurfaceVariant   = TextSub,
    error              = Error,
    outline            = Border,
)

@Composable
fun TentuinAgentTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AgentColorScheme,
        typography  = TentuinAgentTypography,
        content     = content,
    )
}
```

### `theme/Type.kt`
```kotlin
package id.tentuin.agent.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import id.tentuin.agent.R
import androidx.compose.ui.text.font.FontFamily

val googleFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage   = "com.google.android.gms",
    certificates      = R.array.com_google_android_gms_fonts_certs,
)

private val plusJakartaSans = GoogleFont("Plus Jakarta Sans")

val PlusJakartaSansFamily = FontFamily(
    Font(googleFont = plusJakartaSans, fontProvider = googleFontProvider, weight = FontWeight.Normal),
    Font(googleFont = plusJakartaSans, fontProvider = googleFontProvider, weight = FontWeight.Medium),
    Font(googleFont = plusJakartaSans, fontProvider = googleFontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = plusJakartaSans, fontProvider = googleFontProvider, weight = FontWeight.Bold),
)

val TentuinAgentTypography = Typography(
    headlineMedium = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 28.sp),
    titleLarge     = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Bold,     fontSize = 16.sp, lineHeight = 24.sp),
    titleMedium    = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    bodyMedium     = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Normal,   fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall      = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Normal,   fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge     = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium    = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Medium,   fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall     = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Medium,   fontSize = 11.sp, lineHeight = 14.sp),
)
```

---

## 7. DATA MODELS

### `data/model/AuthModels.kt`
```kotlin
package id.tentuin.agent.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("email")    val email:    String,
    @SerializedName("password") val password: String,
)

data class RegisterRequest(
    @SerializedName("email")    val email:    String,
    @SerializedName("password") val password: String,
    @SerializedName("data")     val data:     AgentMeta,
)

data class AgentMeta(
    @SerializedName("full_name") val fullName: String,
    @SerializedName("phone")     val phone:    String? = null,
)

data class RefreshRequest(
    @SerializedName("refresh_token") val refreshToken: String,
)

data class TokenResponse(
    @SerializedName("access_token")  val accessToken:  String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("user")          val user:         AuthUser?,
    @SerializedName("expires_in")    val expiresIn:    Long = 3600,
)

data class AuthUser(
    @SerializedName("id")    val id:    String,
    @SerializedName("email") val email: String,
)
```

### `data/model/AgentModels.kt`
```kotlin
package id.tentuin.agent.data.model

import com.google.gson.annotations.SerializedName

data class Agent(
    @SerializedName("id")                   val id:                 String,
    @SerializedName("full_name")            val fullName:           String,
    @SerializedName("email")                val email:              String,
    @SerializedName("phone")                val phone:              String?,
    @SerializedName("referral_code")        val referralCode:       String,
    @SerializedName("status")               val status:             String,  // active | suspended | inactive
    @SerializedName("is_owner")             val isOwner:            Boolean = false,
    @SerializedName("last_active_at")       val lastActiveAt:       String?,
    @SerializedName("bank_name")            val bankName:           String?,
    @SerializedName("bank_account_number")  val bankAccountNumber:  String?,
    @SerializedName("bank_account_name")    val bankAccountName:    String?,
    @SerializedName("created_at")           val createdAt:          String?,
)

data class CreateAgentRequest(
    @SerializedName("id")             val id:           String,
    @SerializedName("full_name")      val fullName:     String,
    @SerializedName("email")          val email:        String,
    @SerializedName("phone")          val phone:        String?,
    @SerializedName("referral_code")  val referralCode: String,
    @SerializedName("last_active_at") val lastActiveAt: String,
)

data class UpdateBankRequest(
    @SerializedName("bank_name")            val bankName:          String,
    @SerializedName("bank_account_number")  val bankAccountNumber: String,
    @SerializedName("bank_account_name")    val bankAccountName:   String,
    @SerializedName("updated_at")           val updatedAt:         String = java.time.Instant.now().toString(),
)
```

### `data/model/SchoolModels.kt`
```kotlin
package id.tentuin.agent.data.model

import com.google.gson.annotations.SerializedName

data class School(
    @SerializedName("id")              val id:            String,
    @SerializedName("name")            val name:          String,
    @SerializedName("npsn")            val npsn:          String?,
    @SerializedName("city")            val city:          String,
    @SerializedName("province")        val province:      String,
    @SerializedName("address")         val address:       String?,
    @SerializedName("email")           val email:         String?,
    @SerializedName("phone")           val phone:         String?,
    @SerializedName("logo_url")        val logoUrl:       String?,
    @SerializedName("total_students")  val totalStudents: Int = 0,
    @SerializedName("is_active")       val isActive:      Boolean = true,
)

data class SchoolTarget(
    @SerializedName("id")               val id:             String,
    @SerializedName("school_id")        val schoolId:       String,
    @SerializedName("year")             val year:           Int,
    @SerializedName("annual_target")    val annualTarget:   Int,
    @SerializedName("monthly_targets")  val monthlyTargets: List<Int>?,
)
```

### `data/model/ClaimModels.kt`
```kotlin
package id.tentuin.agent.data.model

import com.google.gson.annotations.SerializedName

data class SchoolClaim(
    @SerializedName("id")          val id:        String,
    @SerializedName("agent_id")    val agentId:   String,
    @SerializedName("school_id")   val schoolId:  String,
    @SerializedName("is_active")   val isActive:  Boolean,
    @SerializedName("claimed_at")  val claimedAt: String,
    @SerializedName("school")      val school:    School?,
)

data class UniversityClaim(
    @SerializedName("id")              val id:           String,
    @SerializedName("agent_id")        val agentId:      String,
    @SerializedName("university_id")   val universityId: String,
    @SerializedName("is_active")       val isActive:     Boolean,
    @SerializedName("claimed_at")      val claimedAt:    String,
    @SerializedName("university")      val university:   UniversityBrief?,
)

data class UniversityBrief(
    @SerializedName("id")              val id:            String,
    @SerializedName("name")            val name:          String,
    @SerializedName("short_name")      val shortName:     String,
    @SerializedName("city")            val city:          String,
    @SerializedName("logo_url")        val logoUrl:       String?,
    @SerializedName("quota_balance")   val quotaBalance:  Int = 0,
    @SerializedName("is_partner")      val isPartner:     Boolean = false,
    @SerializedName("partner_tier")    val partnerTier:   String?,
)

data class CreateSchoolClaimRequest(
    @SerializedName("agent_id")  val agentId:  String,
    @SerializedName("school_id") val schoolId: String,
)

data class CreateUniversityClaimRequest(
    @SerializedName("agent_id")      val agentId:      String,
    @SerializedName("university_id") val universityId: String,
)
```

### `data/model/CommissionModels.kt`
```kotlin
package id.tentuin.agent.data.model

import com.google.gson.annotations.SerializedName

data class AgentCommission(
    @SerializedName("id")               val id:            String,
    @SerializedName("agent_id")         val agentId:       String,
    @SerializedName("month")            val month:         Int,
    @SerializedName("year")             val year:          Int,
    @SerializedName("stream_a_amount")  val streamAAmount: Int,
    @SerializedName("stream_b_amount")  val streamBAmount: Int,
    @SerializedName("total_amount")     val totalAmount:   Int,
    @SerializedName("status")           val status:        String,  // pending | paid | cancelled
    @SerializedName("notes")            val notes:         String?,
    @SerializedName("created_at")       val createdAt:     String?,
)

data class AgentWithdrawal(
    @SerializedName("id")            val id:           String,
    @SerializedName("agent_id")      val agentId:      String,
    @SerializedName("amount")        val amount:       Int,
    @SerializedName("status")        val status:       String,  // requested | approved | rejected | transferred
    @SerializedName("requested_at")  val requestedAt:  String,
    @SerializedName("processed_at")  val processedAt:  String?,
    @SerializedName("admin_notes")   val adminNotes:   String?,
)

data class CreateWithdrawalRequest(
    @SerializedName("agent_id") val agentId: String,
    @SerializedName("amount")   val amount:  Int,
)

data class UniversitySubscribeLog(
    @SerializedName("id")               val id:              String,
    @SerializedName("university_id")    val universityId:    String,
    @SerializedName("amount")           val amount:          Int,
    @SerializedName("quota_purchased")  val quotaPurchased:  Int,
    @SerializedName("commission_agent") val commissionAgent: Int,
    @SerializedName("subscribed_at")    val subscribedAt:    String,
)
```

---

## 8. RETROFIT API INTERFACE

### `core/network/AgentApi.kt`
```kotlin
package id.tentuin.agent.core.network

import id.tentuin.agent.data.model.*
import okhttp3.ResponseBody
import retrofit2.http.*

interface AgentApi {

    // ── Auth ──────────────────────────────────────────────────────────────
    @POST("auth/v1/token")
    suspend fun login(
        @Query("grant_type") grantType: String = "password",
        @Body body: LoginRequest,
    ): TokenResponse

    @POST("auth/v1/signup")
    suspend fun register(@Body body: RegisterRequest): TokenResponse

    @POST("auth/v1/token")
    suspend fun refreshToken(
        @Query("grant_type") grantType: String = "refresh_token",
        @Body body: RefreshRequest,
    ): TokenResponse

    @POST("auth/v1/logout")
    suspend fun logout(@Header("Authorization") token: String): ResponseBody

    // ── Agents ────────────────────────────────────────────────────────────
    @GET("rest/v1/agents")
    suspend fun getAgent(
        @Query("id")     id:     String,    // format: "eq.{uuid}"
        @Query("select") select: String = "*",
    ): List<Agent>

    @POST("rest/v1/agents")
    @Headers("Prefer: return=representation")
    suspend fun createAgent(@Body body: CreateAgentRequest): List<Agent>

    @PATCH("rest/v1/agents")
    @Headers("Prefer: return=minimal")
    suspend fun updateAgentLastActive(
        @Query("id")  id:   String,
        @Body         body: Map<String, String>,
    ): ResponseBody

    @PATCH("rest/v1/agents")
    @Headers("Prefer: return=minimal")
    suspend fun updateAgentBank(
        @Query("id")  id:   String,
        @Body         body: UpdateBankRequest,
    ): ResponseBody

    // ── Schools ───────────────────────────────────────────────────────────
    @GET("rest/v1/schools")
    suspend fun getSchools(
        @Query("is_active") isActive: String = "eq.true",
        @Query("order")     order:    String = "name.asc",
        @Query("select")    select:   String = "*",
        @Query("limit")     limit:    Int = 100,
        @Query("offset")    offset:   Int = 0,
    ): List<School>

    @GET("rest/v1/schools")
    suspend fun searchSchools(
        @Query("name")      nameFilter: String,   // format: "ilike.*query*"
        @Query("is_active") isActive:   String = "eq.true",
        @Query("order")     order:      String = "name.asc",
        @Query("select")    select:     String = "*",
    ): List<School>

    @GET("rest/v1/school_targets")
    suspend fun getSchoolTarget(
        @Query("school_id") schoolId: String,
        @Query("year")      year:     String,
        @Query("select")    select:   String = "*",
    ): List<SchoolTarget>

    // ── Agent School Claims ───────────────────────────────────────────────
    @GET("rest/v1/agent_school_claims")
    suspend fun getSchoolClaims(
        @Query("agent_id")  agentId:  String,
        @Query("is_active") isActive: String = "eq.true",
        @Query("select")    select:   String = "*,school:schools(id,name,city,province,total_students,logo_url)",
        @Query("order")     order:    String = "claimed_at.desc",
    ): List<SchoolClaim>

    @GET("rest/v1/agent_school_claims")
    suspend fun checkSchoolClaim(
        @Query("school_id") schoolId: String,
        @Query("is_active") isActive: String = "eq.true",
        @Query("select")    select:   String = "id",
    ): List<SchoolClaim>

    @POST("rest/v1/agent_school_claims")
    @Headers("Prefer: return=representation")
    suspend fun createSchoolClaim(@Body body: CreateSchoolClaimRequest): List<SchoolClaim>

    // ── Agent University Claims ───────────────────────────────────────────
    @GET("rest/v1/universities")
    suspend fun getUniversities(
        @Query("is_active") isActive: String = "eq.true",
        @Query("order")     order:    String = "is_partner.desc,name.asc",
        @Query("select")    select:   String = "id,name,short_name,city,logo_url,is_partner,partner_tier,quota_balance",
        @Query("limit")     limit:    Int = 100,
    ): List<UniversityBrief>

    @GET("rest/v1/agent_university_claims")
    suspend fun getUniversityClaims(
        @Query("agent_id")  agentId:  String,
        @Query("is_active") isActive: String = "eq.true",
        @Query("select")    select:   String = "*,university:universities(id,name,short_name,city,logo_url,quota_balance,is_partner,partner_tier)",
        @Query("order")     order:    String = "claimed_at.desc",
    ): List<UniversityClaim>

    @GET("rest/v1/agent_university_claims")
    suspend fun checkUniversityClaim(
        @Query("university_id") universityId: String,
        @Query("is_active")     isActive:     String = "eq.true",
        @Query("select")        select:       String = "id",
    ): List<UniversityClaim>

    @POST("rest/v1/agent_university_claims")
    @Headers("Prefer: return=representation")
    suspend fun createUniversityClaim(@Body body: CreateUniversityClaimRequest): List<UniversityClaim>

    // ── Commissions ───────────────────────────────────────────────────────
    @GET("rest/v1/agent_commissions")
    suspend fun getCommissions(
        @Query("agent_id") agentId: String,
        @Query("year")     year:    String,
        @Query("order")    order:   String = "month.asc",
        @Query("select")   select:  String = "*",
    ): List<AgentCommission>

    @GET("rest/v1/university_subscribe_logs")
    suspend fun getSubscribeLogs(
        @Query("agent_id") agentId: String,
        @Query("order")    order:   String = "subscribed_at.desc",
        @Query("select")   select:  String = "*",
        @Query("limit")    limit:   Int = 50,
    ): List<UniversitySubscribeLog>

    // ── Withdrawals ───────────────────────────────────────────────────────
    @GET("rest/v1/agent_withdrawals")
    suspend fun getWithdrawals(
        @Query("agent_id") agentId: String,
        @Query("order")    order:   String = "requested_at.desc",
        @Query("select")   select:  String = "*",
    ): List<AgentWithdrawal>

    @POST("rest/v1/agent_withdrawals")
    @Headers("Prefer: return=representation")
    suspend fun createWithdrawal(@Body body: CreateWithdrawalRequest): List<AgentWithdrawal>
}
```

---

## 9. SESSION & DI SETUP

### `core/datastore/SessionDataStore.kt`
```kotlin
package id.tentuin.agent.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore by preferencesDataStore(name = "agent_session")

class SessionDataStore(private val context: Context) {
    private val store = context.sessionDataStore

    companion object {
        private val KEY_ACCESS_TOKEN  = stringPreferencesKey("access_token")
        private val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val KEY_USER_ID       = stringPreferencesKey("user_id")
    }

    val accessToken:  Flow<String?> = store.data.map { it[KEY_ACCESS_TOKEN] }
    val refreshToken: Flow<String?> = store.data.map { it[KEY_REFRESH_TOKEN] }
    val userId:       Flow<String?> = store.data.map { it[KEY_USER_ID] }

    suspend fun saveSession(accessToken: String, refreshToken: String, userId: String) {
        store.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN]  = accessToken
            prefs[KEY_REFRESH_TOKEN] = refreshToken
            prefs[KEY_USER_ID]       = userId
        }
    }

    suspend fun updateTokens(accessToken: String, refreshToken: String) {
        store.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN]  = accessToken
            prefs[KEY_REFRESH_TOKEN] = refreshToken
        }
    }

    suspend fun clearSession() { store.edit { it.clear() } }
}
```

### `core/network/AuthInterceptor.kt`
```kotlin
package id.tentuin.agent.core.network

import id.tentuin.agent.BuildConfig
import id.tentuin.agent.core.datastore.SessionDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val sessionDataStore: SessionDataStore,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { sessionDataStore.accessToken.first() }
        val request = chain.request().newBuilder()
            .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer ${token ?: BuildConfig.SUPABASE_ANON_KEY}")
            .build()
        return chain.proceed(request)
    }
}
```

### `core/di/DataStoreModule.kt`
```kotlin
package id.tentuin.agent.core.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import id.tentuin.agent.core.datastore.SessionDataStore
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    @Provides @Singleton
    fun provideSessionDataStore(@ApplicationContext ctx: Context): SessionDataStore =
        SessionDataStore(ctx)
}
```

### `core/di/NetworkModule.kt`
```kotlin
package id.tentuin.agent.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import id.tentuin.agent.BuildConfig
import id.tentuin.agent.core.network.AgentApi
import id.tentuin.agent.core.network.AuthInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                        else HttpLoggingInterceptor.Level.NONE
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

    @Provides @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.SUPABASE_URL.trimEnd('/') + "/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides @Singleton
    fun provideAgentApi(retrofit: Retrofit): AgentApi =
        retrofit.create(AgentApi::class.java)
}
```

---

## 10. REPOSITORIES

### `data/repository/AuthRepository.kt`
```kotlin
package id.tentuin.agent.data.repository

import id.tentuin.agent.core.datastore.SessionDataStore
import id.tentuin.agent.core.network.AgentApi
import id.tentuin.agent.data.model.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: AgentApi,
    private val session: SessionDataStore,
) {
    val accessToken = session.accessToken
    val userId = session.userId

    suspend fun login(email: String, password: String): Result<TokenResponse> = runCatching {
        val res = api.login(body = LoginRequest(email, password))
        val uid = res.user?.id ?: error("No user in response")
        session.saveSession(res.accessToken, res.refreshToken, uid)
        res
    }

    suspend fun register(email: String, password: String, fullName: String, phone: String?): Result<TokenResponse> = runCatching {
        val res = api.register(RegisterRequest(email, password, AgentMeta(fullName, phone)))
        val uid = res.user?.id ?: error("No user in response")
        session.saveSession(res.accessToken, res.refreshToken, uid)
        res
    }

    suspend fun logout() {
        val token = session.accessToken.first()
        if (token != null) runCatching { api.logout("Bearer $token") }
        session.clearSession()
    }
}
```

### `data/repository/AgentRepository.kt`
```kotlin
package id.tentuin.agent.data.repository

import id.tentuin.agent.core.datastore.SessionDataStore
import id.tentuin.agent.core.network.AgentApi
import id.tentuin.agent.data.model.Agent
import id.tentuin.agent.data.model.CreateAgentRequest
import id.tentuin.agent.data.model.UpdateBankRequest
import kotlinx.coroutines.flow.first
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgentRepository @Inject constructor(
    private val api: AgentApi,
    private val session: SessionDataStore,
) {
    suspend fun getOrCreateAgent(fullName: String, email: String, phone: String?): Result<Agent> = runCatching {
        val userId = session.userId.first() ?: error("Not logged in")
        val existing = api.getAgent(id = "eq.$userId")
        if (existing.isNotEmpty()) return@runCatching existing.first()

        // Generate referral code
        val code = generateReferralCode()
        val created = api.createAgent(
            CreateAgentRequest(
                id = userId,
                fullName = fullName,
                email = email,
                phone = phone,
                referralCode = code,
                lastActiveAt = Instant.now().toString(),
            )
        )
        created.first()
    }

    suspend fun getCurrentAgent(): Result<Agent?> = runCatching {
        val userId = session.userId.first() ?: return@runCatching null
        api.getAgent(id = "eq.$userId").firstOrNull()
    }

    suspend fun updateLastActive(): Result<Unit> = runCatching {
        val userId = session.userId.first() ?: return@runCatching
        api.updateAgentLastActive(
            id = "eq.$userId",
            body = mapOf("last_active_at" to Instant.now().toString())
        )
        Unit
    }

    suspend fun updateBank(bankName: String, accountNumber: String, accountName: String): Result<Unit> = runCatching {
        val userId = session.userId.first() ?: error("Not logged in")
        api.updateAgentBank(
            id = "eq.$userId",
            body = UpdateBankRequest(bankName, accountNumber, accountName)
        )
        Unit
    }

    private fun generateReferralCode(): String {
        val letters = "ABCDEFGHJKLMNPQRSTUVWXYZ"
        val prefix = (1..3).map { letters.random() }.joinToString("")
        val suffix = (1000..9999).random()
        return "$prefix$suffix"
    }
}
```

### `data/repository/ClaimRepository.kt`
```kotlin
package id.tentuin.agent.data.repository

import id.tentuin.agent.core.datastore.SessionDataStore
import id.tentuin.agent.core.network.AgentApi
import id.tentuin.agent.data.model.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClaimRepository @Inject constructor(
    private val api: AgentApi,
    private val session: SessionDataStore,
) {
    suspend fun getSchoolClaims(): Result<List<SchoolClaim>> = runCatching {
        val userId = session.userId.first() ?: error("Not logged in")
        api.getSchoolClaims(agentId = "eq.$userId")
    }

    suspend fun claimSchool(schoolId: String): Result<SchoolClaim> = runCatching {
        val userId = session.userId.first() ?: error("Not logged in")
        // Cek apakah sudah ada klaim aktif
        val existing = api.checkSchoolClaim(schoolId = "eq.$schoolId")
        if (existing.isNotEmpty()) error("Sekolah ini sudah diklaim oleh agen lain.")
        val result = api.createSchoolClaim(CreateSchoolClaimRequest(userId, schoolId))
        result.first()
    }

    suspend fun getUniversityClaims(): Result<List<UniversityClaim>> = runCatching {
        val userId = session.userId.first() ?: error("Not logged in")
        api.getUniversityClaims(agentId = "eq.$userId")
    }

    suspend fun claimUniversity(universityId: String): Result<UniversityClaim> = runCatching {
        val userId = session.userId.first() ?: error("Not logged in")
        val existing = api.checkUniversityClaim(universityId = "eq.$universityId")
        if (existing.isNotEmpty()) error("Universitas ini sudah diklaim oleh agen lain.")
        val result = api.createUniversityClaim(CreateUniversityClaimRequest(userId, universityId))
        result.first()
    }

    suspend fun getAvailableSchools(search: String? = null): Result<List<School>> = runCatching {
        if (!search.isNullOrBlank()) {
            api.searchSchools(nameFilter = "ilike.*$search*")
        } else {
            api.getSchools()
        }
    }

    suspend fun getAvailableUniversities(): Result<List<UniversityBrief>> = runCatching {
        api.getUniversities()
    }
}
```

### `data/repository/CommissionRepository.kt`
```kotlin
package id.tentuin.agent.data.repository

import id.tentuin.agent.core.datastore.SessionDataStore
import id.tentuin.agent.core.network.AgentApi
import id.tentuin.agent.data.model.*
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommissionRepository @Inject constructor(
    private val api: AgentApi,
    private val session: SessionDataStore,
) {
    suspend fun getCommissions(year: Int = LocalDate.now().year): Result<List<AgentCommission>> = runCatching {
        val userId = session.userId.first() ?: error("Not logged in")
        api.getCommissions(agentId = "eq.$userId", year = "eq.$year")
    }

    suspend fun getSubscribeLogs(): Result<List<UniversitySubscribeLog>> = runCatching {
        val userId = session.userId.first() ?: error("Not logged in")
        api.getSubscribeLogs(agentId = "eq.$userId")
    }

    suspend fun getWithdrawals(): Result<List<AgentWithdrawal>> = runCatching {
        val userId = session.userId.first() ?: error("Not logged in")
        api.getWithdrawals(agentId = "eq.$userId")
    }

    suspend fun requestWithdrawal(amount: Int): Result<AgentWithdrawal> = runCatching {
        val userId = session.userId.first() ?: error("Not logged in")
        val result = api.createWithdrawal(CreateWithdrawalRequest(userId, amount))
        result.first()
    }
}
```

---

## 11. SCREENS & VIEWMODELS

### Screen 1: Auth — `screen/auth/AuthViewModel.kt`
```kotlin
package id.tentuin.agent.ui.screen.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.tentuin.agent.data.repository.AgentRepository
import id.tentuin.agent.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading:    Boolean = false,
    val error:        String? = null,
    val isSuccess:    Boolean = false,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val agentRepository: AgentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.login(email, password)
                .onSuccess {
                    agentRepository.updateLastActive()
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Login gagal") }
                }
        }
    }

    fun register(email: String, password: String, fullName: String, phone: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.register(email, password, fullName, phone)
                .onSuccess {
                    agentRepository.getOrCreateAgent(fullName, email, phone)
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Registrasi gagal") }
                }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
```

#### `screen/auth/LoginScreen.kt`
```kotlin
// Composable dengan:
// - Logo Tentuin Agen di atas
// - Field email + password (TentuinTextField pattern)
// - Button "Masuk" → viewModel.login()
// - Link "Belum punya akun? Daftar" → navController.navigate(Route.Register.route)
// - Saat isSuccess → navController.navigate(Route.Dashboard.route) { popUpTo(Route.Login.route) { inclusive = true } }
// - Tampilkan error message dengan warna merah jika error != null
```

#### `screen/auth/RegisterScreen.kt`
```kotlin
// Composable dengan:
// - Field: Nama Lengkap, Email, Nomor HP (opsional), Password, Konfirmasi Password
// - Validasi: email valid, password min 8 karakter, password == konfirmasi
// - Button "Daftar" → viewModel.register()
// - Link "Sudah punya akun? Masuk" → navController.popBackStack()
// - Saat isSuccess → navigate ke Dashboard (replace stack)
```

---

### Screen 2: Dashboard — `screen/dashboard/DashboardViewModel.kt`
```kotlin
package id.tentuin.agent.ui.screen.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.tentuin.agent.data.model.Agent
import id.tentuin.agent.data.model.AgentCommission
import id.tentuin.agent.data.repository.AgentRepository
import id.tentuin.agent.data.repository.ClaimRepository
import id.tentuin.agent.data.repository.CommissionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class DashboardUiState(
    val agent:              Agent? = null,
    val currentMonthComm:  AgentCommission? = null,
    val totalPending:       Int = 0,
    val totalPaid:          Int = 0,
    val schoolClaimCount:   Int = 0,
    val uniClaimCount:      Int = 0,
    val isLoading:          Boolean = true,
    val error:              String? = null,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val agentRepository: AgentRepository,
    private val claimRepository: ClaimRepository,
    private val commissionRepository: CommissionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init { loadDashboard() }

    fun refresh() = loadDashboard()

    private fun loadDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val now = LocalDate.now()
            val agent = agentRepository.getCurrentAgent().getOrNull()
            val commissions = commissionRepository.getCommissions(now.year).getOrDefault(emptyList())
            val schoolClaims = claimRepository.getSchoolClaims().getOrDefault(emptyList())
            val uniClaims = claimRepository.getUniversityClaims().getOrDefault(emptyList())

            val currentMonthComm = commissions.firstOrNull { it.month == now.monthValue }
            val totalPending = commissions.filter { it.status == "pending" }.sumOf { it.totalAmount }
            val totalPaid    = commissions.filter { it.status == "paid" }.sumOf { it.totalAmount }

            _uiState.update {
                it.copy(
                    agent            = agent,
                    currentMonthComm = currentMonthComm,
                    totalPending     = totalPending,
                    totalPaid        = totalPaid,
                    schoolClaimCount = schoolClaims.size,
                    uniClaimCount    = uniClaims.size,
                    isLoading        = false,
                )
            }
        }
    }
}
```

#### `screen/dashboard/DashboardScreen.kt`
```
UI Layout (LazyColumn):
┌─────────────────────────────────────────┐
│  Header: "Selamat datang, {nama}!"      │
│  Sub: "Kode Referral: {kode}"           │
│  [Salin Kode] button                    │
├─────────────────────────────────────────┤
│  KOMISI BULAN INI                       │
│  ┌─────────────┐  ┌─────────────┐      │
│  │ Stream A    │  │ Stream B    │      │
│  │ Rp 500.000  │  │ Rp 200.000  │      │
│  │ Dari Prospek│  │ Dari Kampus │      │
│  └─────────────┘  └─────────────┘      │
├─────────────────────────────────────────┤
│  STATISTIK                              │
│  ┌──────────┐ ┌──────────┐ ┌────────┐  │
│  │ Pending  │ │ Dibayar  │ │Sekolah │  │
│  │ Rp 1.2jt │ │ Rp 800rb │ │   5   │  │
│  └──────────┘ └──────────┘ └────────┘  │
├─────────────────────────────────────────┤
│  AKSI CEPAT                             │
│  [Klaim Sekolah]  [Klaim Kampus]        │
│  [Riwayat Komisi] [Tarik Komisi]        │
└─────────────────────────────────────────┘
```

---

### Screen 3: Klaim Sekolah — `screen/claim/ClaimViewModel.kt`
```kotlin
package id.tentuin.agent.ui.screen.claim

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.tentuin.agent.data.model.School
import id.tentuin.agent.data.model.SchoolClaim
import id.tentuin.agent.data.model.UniversityBrief
import id.tentuin.agent.data.model.UniversityClaim
import id.tentuin.agent.data.repository.ClaimRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ClaimUiState(
    // School
    val schools:         List<School> = emptyList(),
    val schoolClaims:    List<SchoolClaim> = emptyList(),
    val schoolSearch:    String = "",
    val loadingSchools:  Boolean = true,

    // University
    val universities:    List<UniversityBrief> = emptyList(),
    val uniClaims:       List<UniversityClaim> = emptyList(),
    val loadingUnis:     Boolean = true,

    // Action feedback
    val claimLoading:    Boolean = false,
    val claimSuccess:    String? = null,  // nama sekolah/univ yang baru diklaim
    val claimError:      String? = null,
)

@HiltViewModel
class ClaimViewModel @Inject constructor(
    private val claimRepository: ClaimRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClaimUiState())
    val uiState: StateFlow<ClaimUiState> = _uiState.asStateFlow()
    private var searchJob: Job? = null

    init {
        loadSchools()
        loadSchoolClaims()
        loadUniversities()
        loadUniClaims()
    }

    fun onSchoolSearchChanged(query: String) {
        _uiState.update { it.copy(schoolSearch = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)
            claimRepository.getAvailableSchools(query.ifBlank { null })
                .onSuccess { schools -> _uiState.update { it.copy(schools = schools) } }
        }
    }

    fun claimSchool(schoolId: String, schoolName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(claimLoading = true, claimError = null) }
            claimRepository.claimSchool(schoolId)
                .onSuccess {
                    loadSchoolClaims()
                    _uiState.update { it.copy(claimLoading = false, claimSuccess = schoolName) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(claimLoading = false, claimError = e.message) }
                }
        }
    }

    fun claimUniversity(universityId: String, universityName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(claimLoading = true, claimError = null) }
            claimRepository.claimUniversity(universityId)
                .onSuccess {
                    loadUniClaims()
                    _uiState.update { it.copy(claimLoading = false, claimSuccess = universityName) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(claimLoading = false, claimError = e.message) }
                }
        }
    }

    fun clearFeedback() = _uiState.update { it.copy(claimSuccess = null, claimError = null) }

    private fun loadSchools() = viewModelScope.launch {
        claimRepository.getAvailableSchools()
            .onSuccess { schools -> _uiState.update { it.copy(schools = schools, loadingSchools = false) } }
            .onFailure { _uiState.update { it.copy(loadingSchools = false) } }
    }

    private fun loadSchoolClaims() = viewModelScope.launch {
        claimRepository.getSchoolClaims()
            .onSuccess { claims -> _uiState.update { it.copy(schoolClaims = claims) } }
    }

    private fun loadUniversities() = viewModelScope.launch {
        claimRepository.getAvailableUniversities()
            .onSuccess { unis -> _uiState.update { it.copy(universities = unis, loadingUnis = false) } }
            .onFailure { _uiState.update { it.copy(loadingUnis = false) } }
    }

    private fun loadUniClaims() = viewModelScope.launch {
        claimRepository.getUniversityClaims()
            .onSuccess { claims -> _uiState.update { it.copy(uniClaims = claims) } }
    }
}
```

#### `screen/claim/ClaimSchoolScreen.kt`
```
UI Layout:
┌──────────────────────────────────────────┐
│ AppBar: "Klaim Sekolah"                  │
├──────────────────────────────────────────┤
│ [🔍 Cari nama sekolah...]  ← TextField  │
├──────────────────────────────────────────┤
│ SEKOLAH SUDAH DIKLAIM ({count})          │
│ ┌──────────────────────────────────┐     │
│ │ 🏫 SMA Negeri 1 Bandung     ✅   │     │
│ │    Bandung, Jawa Barat            │     │
│ │    Diklaim: 12 Jan 2025           │     │
│ └──────────────────────────────────┘     │
├──────────────────────────────────────────┤
│ TERSEDIA UNTUK DIKLAIM                   │
│ ┌──────────────────────────────────┐     │
│ │ 🏫 SMA Negeri 2 Jakarta          │     │
│ │    Jakarta, DKI Jakarta           │     │
│ │    1.200 siswa  [KLAIM]           │     │  ← Button outline
│ └──────────────────────────────────┘     │
└──────────────────────────────────────────┘

Aturan UI:
- Sekolah yang sudah diklaim agen ini: tampilkan badge hijau ✅, tidak ada tombol Klaim
- Sekolah yang sudah diklaim agen lain: tampilkan badge abu "Diklaim", tombol KLAIM disabled
- Sekolah belum diklaim: tombol [KLAIM] berwarna Primary aktif
- Saat klaim berhasil: tampilkan Snackbar "Sekolah {nama} berhasil diklaim!"
- Saat klaim gagal: tampilkan Snackbar error merah
- Loading: tampilkan SkeletonBox untuk setiap item
```

#### `screen/claim/ClaimUniversityScreen.kt`
```
UI Layout: sama dengan ClaimSchoolScreen tapi untuk universitas
Tambahan:
- Tampilkan badge partner tier (Basic/Premium) di setiap card
- Tampilkan quota_balance sisa kuota universitas
- Filter chip: Semua | Negeri | Swasta
```

---

### Screen 4: Portfolio — `screen/portfolio/PortfolioViewModel.kt`
```kotlin
package id.tentuin.agent.ui.screen.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.tentuin.agent.data.model.SchoolClaim
import id.tentuin.agent.data.model.UniversityClaim
import id.tentuin.agent.data.model.UniversitySubscribeLog
import id.tentuin.agent.data.repository.ClaimRepository
import id.tentuin.agent.data.repository.CommissionRepository
import id.tentuin.agent.data.repository.SchoolRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class PortfolioUiState(
    val schoolClaims:   List<SchoolClaim> = emptyList(),
    val uniClaims:      List<UniversityClaim> = emptyList(),
    val subscribeLogs:  List<UniversitySubscribeLog> = emptyList(),
    val activeTab:      PortfolioTab = PortfolioTab.SCHOOLS,
    val isLoading:      Boolean = true,
)

enum class PortfolioTab { SCHOOLS, UNIVERSITIES }

@HiltViewModel
class PortfolioViewModel @Inject constructor(
    private val claimRepository: ClaimRepository,
    private val commissionRepository: CommissionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PortfolioUiState())
    val uiState: StateFlow<PortfolioUiState> = _uiState.asStateFlow()

    init { loadPortfolio() }

    fun setTab(tab: PortfolioTab) = _uiState.update { it.copy(activeTab = tab) }

    private fun loadPortfolio() {
        viewModelScope.launch {
            val schoolClaims  = claimRepository.getSchoolClaims().getOrDefault(emptyList())
            val uniClaims     = claimRepository.getUniversityClaims().getOrDefault(emptyList())
            val subscribeLogs = commissionRepository.getSubscribeLogs().getOrDefault(emptyList())
            _uiState.update {
                it.copy(
                    schoolClaims  = schoolClaims,
                    uniClaims     = uniClaims,
                    subscribeLogs = subscribeLogs,
                    isLoading     = false,
                )
            }
        }
    }
}
```

#### `screen/portfolio/PortfolioSchoolScreen.kt`
```
UI Layout:
┌──────────────────────────────────────────┐
│ Tab: [Sekolah (5)] | [Universitas (3)]   │
├──────────────────────────────────────────┤
│ (Tab Sekolah aktif)                      │
│ ┌────────────────────────────────────┐   │
│ │ 🏫 SMA Negeri 1 Bandung            │   │
│ │    Bandung • 1.200 siswa            │   │
│ │    ────────────────────────────    │   │
│ │    Target 2025: 120 siswa          │   │
│ │    Progress: ████████░░ 87/120     │   │  ← ProgressBar
│ │    Status: ✅ On Track             │   │
│ │    Komisi Jan: Rp 340.000          │   │
│ └────────────────────────────────────┘   │
└──────────────────────────────────────────┘

Untuk setiap sekolah, tampilkan:
- Nama, kota, total_students
- Progress bar kumulatif (current/target * 100%)
- Label: "On Track ✅" jika komisi cair, "Belum Tercapai ❌" jika tidak
- Komisi bulan ini (ambil dari agent_commissions stream_a_amount terkait sekolah ini)
```

#### `screen/portfolio/PortfolioUniversityScreen.kt`
```
(Tab Universitas aktif)
┌────────────────────────────────────┐
│ 🏛 Universitas Bina Nusantara       │
│    Jakarta • Premium Partner        │
│    Sisa Kuota: 1.240 prospek        │
│    ─────────────────────────────   │
│    Subscribe Terakhir:             │
│    12 Feb 2025 - Rp 5.000.000      │
│    Komisi: Rp 500.000              │
│    8 Feb 2025 - Rp 3.000.000       │
│    Komisi: Rp 300.000              │
└────────────────────────────────────┘
```

---

### Screen 5: Komisi — `screen/commission/CommissionViewModel.kt`
```kotlin
package id.tentuin.agent.ui.screen.commission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.tentuin.agent.data.model.AgentCommission
import id.tentuin.agent.data.repository.CommissionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class CommissionUiState(
    val commissions:    List<AgentCommission> = emptyList(),
    val selectedYear:   Int = LocalDate.now().year,
    val totalPending:   Int = 0,
    val totalPaid:      Int = 0,
    val totalAll:       Int = 0,
    val isLoading:      Boolean = true,
)

@HiltViewModel
class CommissionViewModel @Inject constructor(
    private val commissionRepository: CommissionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommissionUiState())
    val uiState: StateFlow<CommissionUiState> = _uiState.asStateFlow()

    init { loadCommissions() }

    fun setYear(year: Int) {
        _uiState.update { it.copy(selectedYear = year) }
        loadCommissions()
    }

    private fun loadCommissions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val year = _uiState.value.selectedYear
            commissionRepository.getCommissions(year)
                .onSuccess { list ->
                    _uiState.update {
                        it.copy(
                            commissions  = list,
                            totalPending = list.filter { c -> c.status == "pending" }.sumOf { c -> c.totalAmount },
                            totalPaid    = list.filter { c -> c.status == "paid" }.sumOf { c -> c.totalAmount },
                            totalAll     = list.sumOf { c -> c.totalAmount },
                            isLoading    = false,
                        )
                    }
                }
                .onFailure { _uiState.update { it.copy(isLoading = false) } }
        }
    }
}
```

#### `screen/commission/CommissionScreen.kt`
```
UI Layout:
┌──────────────────────────────────────────┐
│ AppBar: "Riwayat Komisi" + Year Picker   │
├──────────────────────────────────────────┤
│ RINGKASAN 2025                           │
│ ┌──────────────┐ ┌───────────────────┐  │
│ │ Total Semua  │ │ Sudah Dibayar     │  │
│ │ Rp 4.500.000 │ │ Rp 2.100.000      │  │
│ └──────────────┘ └───────────────────┘  │
│ Menunggu Pembayaran: Rp 2.400.000        │
├──────────────────────────────────────────┤
│ DETAIL PER BULAN                         │
│ ┌─────────────────────────────────────┐  │
│ │ Januari 2025              ✅ Dibayar │  │
│ │ Stream A (Prospek): Rp 340.000      │  │
│ │ Stream B (Kampus) : Rp 500.000      │  │
│ │ Total             : Rp 840.000      │  │
│ └─────────────────────────────────────┘  │
│ ┌─────────────────────────────────────┐  │
│ │ Februari 2025         ⏳ Menunggu   │  │
│ │ ...                                 │  │
│ └─────────────────────────────────────┘  │
├──────────────────────────────────────────┤
│ [TARIK KOMISI]  ← FloatingActionButton   │
└──────────────────────────────────────────┘

Status badge warna:
- pending  → Warning (amber)
- paid     → Success (green)
- cancelled → Error (red)
```

---

### Screen 6: Withdrawal — `screen/withdrawal/WithdrawalViewModel.kt`
```kotlin
package id.tentuin.agent.ui.screen.withdrawal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.tentuin.agent.data.model.Agent
import id.tentuin.agent.data.model.AgentWithdrawal
import id.tentuin.agent.data.repository.AgentRepository
import id.tentuin.agent.data.repository.CommissionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WithdrawalUiState(
    val agent:           Agent? = null,
    val withdrawals:     List<AgentWithdrawal> = emptyList(),
    val availableAmount: Int = 0,    // total komisi pending
    val isLoading:       Boolean = true,
    val isSubmitting:    Boolean = false,
    val success:         Boolean = false,
    val error:           String? = null,
)

@HiltViewModel
class WithdrawalViewModel @Inject constructor(
    private val agentRepository: AgentRepository,
    private val commissionRepository: CommissionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WithdrawalUiState())
    val uiState: StateFlow<WithdrawalUiState> = _uiState.asStateFlow()

    init { loadData() }

    fun requestWithdrawal(amount: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            commissionRepository.requestWithdrawal(amount)
                .onSuccess {
                    loadData()
                    _uiState.update { it.copy(isSubmitting = false, success = true) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isSubmitting = false, error = e.message) }
                }
        }
    }

    fun clearFeedback() = _uiState.update { it.copy(success = false, error = null) }

    private fun loadData() {
        viewModelScope.launch {
            val agent = agentRepository.getCurrentAgent().getOrNull()
            val commissions = commissionRepository.getCommissions().getOrDefault(emptyList())
            val withdrawals = commissionRepository.getWithdrawals().getOrDefault(emptyList())
            val available = commissions.filter { it.status == "pending" }.sumOf { it.totalAmount }
            _uiState.update {
                it.copy(
                    agent           = agent,
                    withdrawals     = withdrawals,
                    availableAmount = available,
                    isLoading       = false,
                )
            }
        }
    }
}
```

#### `screen/withdrawal/WithdrawalScreen.kt`
```
UI Layout:
┌──────────────────────────────────────────┐
│ AppBar: "Tarik Komisi"                   │
├──────────────────────────────────────────┤
│ REKENING TERDAFTAR                       │
│ Bank: BCA                                │
│ No: 1234567890  Nama: Budi Santoso       │
│ [Ubah Rekening]                          │
├──────────────────────────────────────────┤
│ SALDO TERSEDIA                           │
│ Rp 2.400.000                             │
├──────────────────────────────────────────┤
│ NOMINAL PENARIKAN                        │
│ [Rp ________________] ← TextField angka  │
│ Min. penarikan: Rp 100.000               │
│                                          │
│ [AJUKAN PENARIKAN]                       │
├──────────────────────────────────────────┤
│ RIWAYAT PENARIKAN                        │
│ ┌───────────────────────────────────┐    │
│ │ 10 Feb 2025   Rp 500.000          │    │
│ │ Status: ✅ Ditransfer             │    │
│ └───────────────────────────────────┘    │
│ ┌───────────────────────────────────┐    │
│ │ 5 Jan 2025    Rp 800.000          │    │
│ │ Status: ⏳ Menunggu Approval      │    │
│ └───────────────────────────────────┘    │
└──────────────────────────────────────────┘

Validasi:
- Nominal harus ≥ Rp 100.000
- Nominal tidak boleh > availableAmount
- Rekening bank wajib diisi sebelum bisa withdrawal
```

---

## 12. REUSABLE COMPONENTS

### `component/StatCard.kt`
```kotlin
// Card kecil dengan label + nilai angka (untuk dashboard stats)
// Parameter: label: String, value: String, color: Color = Primary, modifier: Modifier
// Tampilan: Card putih, shadow tipis, label kecil di atas, nilai bold di bawah
```

### `component/ProgressBar.kt`
```kotlin
// Progress bar horizontal dengan persentase
// Parameter: current: Int, target: Int, color: Color, modifier: Modifier
// Tampilan: LinearProgressIndicator Material3 + label "87/120 (72%)" di bawah
```

### `component/SchoolCard.kt`
```kotlin
// Card sekolah untuk list klaim/browse
// Parameter: school: School, claimStatus: ClaimStatus (MINE, CLAIMED, AVAILABLE), onClaim: () -> Unit
// ClaimStatus.MINE      → badge hijau "Milikmu"
// ClaimStatus.CLAIMED   → badge abu "Diklaim", button disabled
// ClaimStatus.AVAILABLE → button Primary "KLAIM"
enum class ClaimStatus { MINE, CLAIMED, AVAILABLE }
```

### `component/CommissionCard.kt`
```kotlin
// Card komisi per bulan
// Parameter: commission: AgentCommission
// Tampilan: baris stream A, baris stream B, total, badge status
```

### `component/EmptyState.kt`
```kotlin
// Identik dengan student-android
// Parameter: icon: ImageVector, title: String, subtitle: String, actionLabel: String?, onAction: () -> Unit
```

### `component/SkeletonBox.kt`
```kotlin
// Identik dengan student-android (shimmer loading placeholder)
// Parameter: modifier: Modifier
```

---

## 13. FORMAT ANGKA (UTILITY)

Buat file `core/util/FormatUtils.kt`:
```kotlin
package id.tentuin.agent.core.util

import java.text.NumberFormat
import java.util.Locale

fun Int.toRupiah(): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return format.format(this).replace("Rp", "Rp ").replace(",00", "")
}
// Contoh: 500000.toRupiah() → "Rp 500.000"

fun String.toMonthName(): String = when (this.toIntOrNull()) {
    1  -> "Januari"
    2  -> "Februari"
    3  -> "Maret"
    4  -> "April"
    5  -> "Mei"
    6  -> "Juni"
    7  -> "Juli"
    8  -> "Agustus"
    9  -> "September"
    10 -> "Oktober"
    11 -> "November"
    12 -> "Desember"
    else -> this
}
```

---

## 14. RESOURCES

### `res/values/themes.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.TentuinAgent" parent="Theme.MaterialComponents.DayNight.NoActionBar">
        <item name="colorPrimary">#6C63FF</item>
        <item name="colorOnPrimary">#FFFFFF</item>
        <item name="android:statusBarColor">#6C63FF</item>
    </style>
</resources>
```

### `res/values/strings.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">Tentuin Agen</string>
</resources>
```

---

## 15. URUTAN IMPLEMENTASI YANG DISARANKAN

Implementasikan file-file ini **secara berurutan** agar build selalu berhasil:

1. `gradle.properties` (tambah Supabase credentials)
2. `build.gradle.kts` (root + app)
3. `AgentApp.kt`
4. `theme/` (Color, Type, Theme)
5. `core/datastore/SessionDataStore.kt`
6. `core/network/AuthInterceptor.kt`
7. `data/model/` (semua model)
8. `core/network/AgentApi.kt`
9. `core/di/` (DataStoreModule, NetworkModule)
10. `data/repository/` (semua repository)
11. `ui/navigation/` (Route, AgentNavHost, BottomNavBar)
12. `ui/component/` (SkeletonBox, EmptyState, StatCard, ProgressBar, SchoolCard, CommissionCard)
13. `ui/screen/auth/` (AuthViewModel, LoginScreen, RegisterScreen)
14. `ui/screen/dashboard/` (DashboardViewModel, DashboardScreen)
15. `ui/screen/claim/` (ClaimViewModel, ClaimSchoolScreen, ClaimUniversityScreen)
16. `ui/screen/portfolio/` (PortfolioViewModel, PortfolioSchoolScreen, PortfolioUniversityScreen)
17. `ui/screen/commission/` (CommissionViewModel, CommissionScreen)
18. `ui/screen/withdrawal/` (WithdrawalViewModel, WithdrawalScreen)
19. `MainActivity.kt`
20. `AndroidManifest.xml`

---

## 16. CATATAN PENTING UNTUK IMPLEMENTASI

1. **Semua ViewModel menggunakan `MutableStateFlow` + `collectAsState()`** — jangan pakai LiveData
2. **Semua Repository menggunakan `Result<T>` dengan `runCatching {}`** — tangani error dengan `.onFailure {}`
3. **Hilt** digunakan untuk semua DI — gunakan `@HiltViewModel`, `@Inject constructor`, `@Singleton`
4. **Retrofit query format Supabase**: filter menggunakan `"eq.{value}"`, sort dengan `"field.asc"`, foreign key embed dengan `"*,table(field1,field2)"`
5. **Semua screen** harus punya state loading (SkeletonBox) dan state empty (EmptyState)
6. **Format Rupiah** selalu gunakan `Int.toRupiah()` dari FormatUtils
7. **Font**: Plus Jakarta Sans dari Google Fonts (sudah ada di student-android, gunakan pola yang sama)
8. **Warna Primary**: `#6C63FF` — konsisten dengan student app
