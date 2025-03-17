plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "de.securepoint.janho.goapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "de.securepoint.janho.goapp"
        minSdk = 33
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
        compose = true
    }
}

fun findExecutable(executable: String): String = System.getenv("PATH")
    .split(File.pathSeparatorChar)
    .map { File(it, executable) }
    .find { it.exists() && it.canExecute() }
    ?.absolutePath
    ?: throw GradleException("$executable not found in PATH")


val goSrcDir = "src/go"
val goBuildDir = "build/go"
val compileGoTaskName = "compileGo"

tasks.register(compileGoTaskName) {
    doFirst { File("$projectDir/$goBuildDir").mkdirs() }
    doLast {
        exec {
            setWorkingDir(goSrcDir)
            commandLine(
                // if gradle can't find gomobile, run `go get golang.org/x/mobile/cmd/gomobile`
                // it should be in $HOME/go/bin, but `findExecutable` finds it anywhere in $PATH
                findExecutable("gomobile"),
                "bind",
                "-target=android",
                "-o=$projectDir/$goBuildDir/logging.aar",
            )
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(
        fileTree(goBuildDir) {
            include("*.aar")
            builtBy(compileGoTaskName)
        },
    )
}
