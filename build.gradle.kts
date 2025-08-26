// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.12.0" apply false
    id("com.google.gms.google-services") version "4.4.3" apply false
    // Kotlin Multiplatform/Native などのために id("org.jetbrains.kotlin.android") version "X.Y.Z" apply false が必要になる場合もある
}

// buildscript ブロックは、pluginsブロックで解決できない古いプラグインや、
// 特殊な設定が必要な場合にのみ使用が推奨されます。
// 今回のケースでは、Android Gradle Pluginのclasspath指定のために残すか、
// よりモダンな手法に移行することも検討できますが、まずは動作する形にします。
buildscript {
    repositories {
        google()
        mavenCentral()
}

    dependencies {
        // Android Gradle Plugin の classpath はここに記述するのが一般的
        classpath("com.android.tools.build:gradle:8.12.0")
        // Google Services Plugin は plugins ブロックで宣言済みのため、ここでは不要
        classpath("com.google.gms:google-services:4.4.3") // 最新バージョンを確認
    }
}

//allprojects {
//    repositories {
//        google()
//        mavenCentral()
//    }
//}