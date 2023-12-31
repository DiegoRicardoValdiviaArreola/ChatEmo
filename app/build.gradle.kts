import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.chatemo"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.example.chatemo"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            val p = Properties()
            p.load(project.rootProject.file("apikey.properties").reader())
            val chatGPTApiKey: String = p.getProperty("CHAT_GPT_API_KEY")
            buildConfigField("String", "CHAT_GPT_API_KEY", "\"$chatGPTApiKey\"")
        }

        debug {
            val p = Properties()
            p.load(project.rootProject.file("apikey.properties").reader())
            val chatGPTApiKey: String = p.getProperty("CHAT_GPT_API_KEY")
            buildConfigField("String", "CHAT_GPT_API_KEY", "\"$chatGPTApiKey\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        buildConfig = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("com.android.volley:volley:1.2.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}