plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id ("kotlin-kapt")
    alias(libs.plugins.hilt.android.gradle.plugin)
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0"
}

android {
    namespace = "com.nithra.invoice_generator_tool"
    compileSdk = 35

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
        compose = true
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    //viewmodel and live data
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    //retrofit and converts gson and logging interceptor
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)

    //coroutine
    implementation(libs.kotlinx.coroutines.android)

    //hilt dagger
    implementation(libs.hilt.android)
  //  implementation(libs.itextg)
    kapt("com.google.dagger:hilt-compiler:2.55")


    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.ui.android)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
  //  implementation(libs.androidx.hilt.lifecycle.viewmodel)

    //apply viewmodel in fragment
    implementation(libs.androidx.fragment.ktx)


    implementation(libs.lottie)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}