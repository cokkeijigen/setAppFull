import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

plugins {
    alias(libs.plugins.android.application)
}
android {
    namespace = "ss.colytitse.setappfull"
    compileSdk = 35

    defaultConfig {
        applicationId = "ss.colytitse.setappfull"
        minSdk =  27
        //noinspection ExpiredTargetSdkVersion
        targetSdk = 27
        versionCode = 135
        versionName = "1.3.5"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    applicationVariants.all {
        outputs.all( {
            val outputs = this as BaseVariantOutputImpl
            val dateFormat = SimpleDateFormat("yyMMddHHmm")
            dateFormat.timeZone = TimeZone.getTimeZone("GMT+8")
            outputs.outputFileName = "setappfull_${versionName}_${dateFormat.format(Date())}.apk"
        })
    }
}

dependencies {
    compileOnly(libs.de.robv.android.xposed.api)
    implementation(libs.material)
    implementation(libs.swiperefreshlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}