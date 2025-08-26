// settings.gradle.kts

pluginManagement { // プラグイン自体を取得するリポジトリ
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement { // プロジェクトの依存関係（ライブラリ）を取得するリポジトリ
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS) // これでプロジェクトレベルのリポジトリ宣言をエラーにする
    repositories {
        google()
        mavenCentral()
        // 他に必要なリポジトリがあればここに追加
    }
}

rootProject.name = "MyApplication4" // プロジェクト名に合わせてください
include(":app") // アプリモジュール名に合わせてください
// 他のモジュールがあればここに追加
