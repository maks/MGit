plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "me.sheimi.sgit"
    compileSdk = 36

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    defaultConfig {
        applicationId = "com.manichord.mgit"
        minSdk = 21
        targetSdk = 36

        vectorDrawables.useSupportLibrary = true

        versionCode = 240
        versionName = "1.7.0"
    }

    buildFeatures {
        dataBinding = true
        buildConfig = true
    }

    lint {
        abortOnError = false
    }

    signingConfigs {
        create("release") {
            if (project.hasProperty("special")) {
                keyAlias = project.property("alias") as String
                keyPassword = project.property("password") as String
                storeFile = file(project.property("keystore") as String)
                storePassword = project.property("password") as String
            } else {
                keyAlias = ""
                keyPassword = ""
                storeFile = file("/empty")
                storePassword = ""
            }
        }
    }

    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "com.jcraft" && requested.name == "jsch") {
            useTarget("com.github.mwiede:jsch:0.2.0")
        }
    }
    exclude(group = "org.apache.httpcomponents", module = "httpclient")
}

dependencies {
    val acraVersion = "5.8.4"

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("androidx.fragment:fragment:1.4.0")
    implementation("androidx.annotation:annotation:1.3.0")
    implementation("androidx.appcompat:appcompat:1.4.0")
    implementation("com.google.android.material:material:1.4.0")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.vectordrawable:vectordrawable:1.1.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.2")

    // ViewModel and LiveData
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    kapt("androidx.lifecycle:lifecycle-compiler:2.4.0")

    implementation("com.jakewharton.timber:timber:4.5.1")
    implementation("com.github.mwiede:jsch:0.2.0")
    implementation("commons-io:commons-io:2.5")
    implementation("org.eclipse.jgit:org.eclipse.jgit:3.7.1.201504261725-r")
    implementation("com.nostra13.universalimageloader:universal-image-loader:1.9.5")
    implementation("com.scottyab:secure-preferences-lib:0.1.7")
    // 2.6.0+ ships 16 KB page-aligned native libs (built with NDK r27, minSdk 21).
    // Older versions (<= 2.5.x) have 4 KB LOAD alignment and fail Play's 16 KB check.
    implementation("org.conscrypt:conscrypt-android:2.7.0")
    implementation("org.bouncycastle:bcprov-jdk15on:1.70")

    implementation("ch.acra:acra-mail:$acraVersion")
    implementation("ch.acra:acra-dialog:$acraVersion")

    debugImplementation("com.facebook.stetho:stetho:1.5.0")
    debugImplementation("com.facebook.stetho:stetho-timber:1.5.0")
    testImplementation("junit:junit:4.12")
    testImplementation("org.robolectric:robolectric:3.5")
    testImplementation("org.robolectric:shadows-support-v4:3.4-rc2")
}
