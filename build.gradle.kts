buildscript {
    rootProject.extra["kotlin_version"] = "1.3.61"
    rootProject.extra["android_plugin_version"] = "3.5.3"

    val kotlin_version: String by rootProject.extra
    val android_plugin_version: String by rootProject.extra

    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:${android_plugin_version}")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${kotlin_version}")

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        google()
        jcenter()
    }
}


task<Delete>("clean") {
    delete(rootProject.buildDir)
}
