plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.nullpointer.nourseCompose"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.nullpointer.nourseCompose"
        minSdk = 21
        targetSdk = 34
        versionCode = 7
        versionName = "4.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf(
                    "room.schemaLocation" to "$projectDir/schemas",
                    "room.incremental" to "true"
                )
            }
        }

        multiDexEnabled = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17

        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + listOf(
                "-opt-in=androidx.compose.animation.ExperimentalAnimationApi",
                "-opt-in=androidx.compose.material.ExperimentalMaterialApi",
            )
        }
    }


    applicationVariants.all {
        addJavaSourceFoldersToModel(
            File(buildDir, "generated/ksp/$name/kotlin")
        )
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material:material:1.6.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")


    // * room
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")


    // * kotlin serialization

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")

    // * hilt
    val daggerHiltVersion = "2.50"
    implementation("com.google.dagger:hilt-android:$daggerHiltVersion")
    kapt("com.google.dagger:hilt-compiler:$daggerHiltVersion")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    // ? hilt test
    testImplementation("com.google.dagger:hilt-android-testing:$daggerHiltVersion")
    androidTestImplementation("com.google.dagger:hilt-android-testing:$daggerHiltVersion")
    kaptAndroidTest("com.google.dagger:hilt-android-compiler:$daggerHiltVersion")

    // * graph
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // * Desugaring
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    // * navigation
    val destinationsVersion = "1.10.0"
    implementation("io.github.raamcosta.compose-destinations:core:$destinationsVersion")
    ksp("io.github.raamcosta.compose-destinations:ksp:$destinationsVersion")

    // * pagination
    val pagingVersion = "3.2.1"
    implementation("androidx.paging:paging-runtime-ktx:$pagingVersion")
    implementation("androidx.paging:paging-compose:3.2.1")
    implementation("androidx.room:room-paging:$roomVersion")

    implementation("com.squareup.inject:assisted-inject-annotations-dagger2:0.5.2")
    kapt("com.squareup.inject:assisted-inject-processor-dagger2:0.5.2")

    // * csv
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:0.9.0")

    // * splash
    implementation("androidx.core:core-splashscreen:1.0.1")

    //* data store
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // * firebase
    implementation(platform("com.google.firebase:firebase-bom:32.1.0"))
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")

    // * timber
    implementation("com.orhanobut:logger:2.2.0")
    implementation("com.jakewharton.timber:timber:5.0.1")

}

kapt {
    correctErrorTypes = true
}
