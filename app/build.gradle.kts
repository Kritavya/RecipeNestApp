plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.kritavya.recipenest"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.kritavya.recipenest"
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
    
    lint {
        abortOnError = false
        baseline = file("lint-baseline.xml")
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
    
    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:32.8.0"))
    
    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth")
    
    // Firebase Firestore (optional, for user data storage)
    implementation("com.google.firebase:firebase-firestore")
    
    // Firebase Realtime Database
    implementation("com.google.firebase:firebase-database")
    
    // Firebase Storage (for images and videos)
    implementation("com.google.firebase:firebase-storage")
    
    // Glide for image loading
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    
    // ViewPager2 for recipe creation flow
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    
    // FlexboxLayout for tags display
    implementation("com.google.android.flexbox:flexbox:3.0.0")
    
    // Cloudinary for image hosting
    implementation("com.cloudinary:cloudinary-android:2.2.0")
    
    // UCrop for image cropping (from JitPack)
    implementation("com.github.yalantis:ucrop:2.2.8-native")
}