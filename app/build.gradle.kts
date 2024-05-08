plugins {
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "com.example.smarthome"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.smarthome"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    buildFeatures {
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")
//    implementation("org.eclipse.paho:org.eclipse.paho.android.service:1.1.1")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("com.github.hannesa2:paho.mqtt.android:3.6.4")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
}