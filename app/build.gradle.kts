apply(plugin = "com.android.application")

apply(plugin = "kotlin-android")

apply(plugin = "kotlin-android-extensions")

android {
    compileSdkVersion(28)
    defaultConfig {
        applicationId = "com.manichord.mgit"
        minSdkVersion(27)
        targetSdkVersion(28)
        versionCode = 2001
        versionName = "2.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk7"))
    implementation("androidx.appcompat:appcompat:1.0.0-alpha1")
    implementation("androidx.constraintlayout:constraintlayout:1.1.2")
    implementation("com.google.android.material:material:1.0.0-alpha1")
    testImplementation("junit:junit:4.12")
    androidTestImplementation("androidx.test:runner:1.1.0-alpha3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.1.0-alpha3")
}
