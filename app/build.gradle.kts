

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}






android {
    namespace = "com.example.blood"
    compileSdk = 35



    defaultConfig {
        applicationId = "com.example.blood"
        minSdk = 24
        targetSdk = 35
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
    }
}




dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("com.google.android.material:material:1.9.0")
    implementation("com.airbnb.android:lottie:6.3.0")
    implementation("androidx.core:core-ktx:1.10.0")
    implementation ("org.jetbrains.kotlin:kotlin-stdlib:1.9.0")

    implementation ("com.google.firebase:firebase-firestore:24.6.1")
    // Firebase BoM (manage all versions)
    implementation(platform("com.google.firebase:firebase-bom:32.0.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    //implementation ("com.google.android.gms:play-services-auth-api-phone:18.0.1")

    implementation("com.cuberto:liquid-swipe:1.0.0")
    implementation ("com.github.denzcoskun:ImageSlideshow:0.1.0")
    //implementation("com.github.Hitomis:CircleMenu:v1.1.0")
    //implementation("np.com.susanthapa:curved_bottom_navigation:0.6.5")

    //implementation ("com.truecaller.android.sdk:true caller-sdk:2.5.0")

    //qr-code
    implementation ("com.google.zxing:core:3.5.2")
    implementation ("com.journeyapps:zxing-android-embedded:4.3.0")

    //location
    implementation ("com.google.android.gms:play-services-location:21.0.1")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    //country code
    implementation ("com.hbb20:ccp:2.5.0")

    //qr-scanner/camera
    implementation ("com.google.mlkit:barcode-scanning:17.2.0")
    implementation ("androidx.camera:camera-camera2:1.2.3")
    implementation ("androidx.camera:camera-lifecycle:1.2.3")
    implementation ("androidx.camera:camera-view:1.2.3")



}