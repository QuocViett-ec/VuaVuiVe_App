plugins {
    id("com.android.library")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "vn.vuavuive.shared"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    // ── Networking: Retrofit 2 + OkHttp 4 + Gson ──
    api("com.squareup.retrofit2:retrofit:2.11.0")
    api("com.squareup.retrofit2:converter-gson:2.11.0")
    api("com.squareup.okhttp3:okhttp:4.12.0")
    api("com.squareup.okhttp3:logging-interceptor:4.12.0")
    api("com.squareup.okhttp3:okhttp-sse:4.12.0")

    // ── DI: Hilt (Dagger) ──
    api("com.google.dagger:hilt-android:2.56.2")
    annotationProcessor("com.google.dagger:hilt-compiler:2.56.2")

    // ── Image Loading: Glide ──
    api("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // ── Architecture: MVVM ──
    api("androidx.lifecycle:lifecycle-viewmodel:2.8.7")
    api("androidx.lifecycle:lifecycle-livedata:2.8.7")
    api("androidx.lifecycle:lifecycle-runtime:2.8.7")

    // ── AndroidX UI ──
    api("androidx.appcompat:appcompat:1.7.0")
    api("com.google.android.material:material:1.12.0")
    api("androidx.constraintlayout:constraintlayout:2.2.1")
    api("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    api("androidx.navigation:navigation-fragment:2.8.9")
    api("androidx.navigation:navigation-ui:2.8.9")
    api("androidx.viewpager2:viewpager2:1.1.0")

    // Firebase Cloud Messaging
    api(platform("com.google.firebase:firebase-bom:33.1.1"))
    api("com.google.firebase:firebase-messaging")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
