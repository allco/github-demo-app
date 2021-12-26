@file:Suppress("Filename", "MatchingDeclarationName")

package se.allco.githubbrowser.buildsrc

object Libs {
    const val androidGradlePlugin = "com.android.tools.build:gradle:7.0.4"

    object Kotlin {
        private const val version = "1.5.31"
        const val gradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$version"
        const val stdLibJdk7 = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$version"
        const val stdLibJdk8 = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$version"
        const val reflect = "org.jetbrains.kotlin:kotlin-reflect:$version"
    }

    object Detekt {
        private const val version = "1.19.0"
        const val gradlePlugin = "io.gitlab.arturbosch.detekt:detekt-gradle-plugin:$version"
        const val formatting = "io.gitlab.arturbosch.detekt:detekt-formatting:$version"
    }

    object Dagger {
        private const val version = "2.40.5"
        const val compiler = "com.google.dagger:dagger-compiler:$version"
        const val lib = "com.google.dagger:dagger:$version"
    }

    object AndroidX {
        object Lifecycle {
            const val extensions = "androidx.lifecycle:lifecycle-extensions:2.2.0"
            const val reactiveStreams = "androidx.lifecycle:lifecycle-reactivestreams-ktx:2.4.0"
            const val common = "androidx.lifecycle:lifecycle-common-java8:2.4.0"
        }

        object Navigation {
            private const val version = "2.3.5"
            const val fragmentKtx = "androidx.navigation:navigation-fragment-ktx:$version"
            const val uiKtx = "androidx.navigation:navigation-ui-ktx:$version"
        }

        const val constraintLayout = "androidx.constraintlayout:constraintlayout:2.1.2"
        const val material = "com.google.android.material:material:1.4.0"
    }

    object Square {
        object Retrofit {
            private const val version = "2.9.0"
            const val lib = "com.squareup.retrofit2:retrofit:$version"
            const val adapterRxJava3 = "com.squareup.retrofit2:adapter-rxjava3:$version"
            const val converterGson = "com.squareup.retrofit2:converter-gson:$version"
        }

        object OkHttp {
            private const val version = "4.9.3"
            const val lib = "com.squareup.okhttp3:okhttp:$version"
            const val loggingInterceptor = "com.squareup.okhttp3:logging-interceptor:$version"
        }
    }

    const val rxJava = "io.reactivex.rxjava3:rxjava:3.0.13"
    const val rxJavaAndroid = "io.reactivex.rxjava3:rxandroid:3.0.0"
    const val timber = "com.jakewharton.timber:timber:4.7.1"
    const val gson = "com.google.code.gson:gson:2.8.6"
    const val javaxInject = "javax.inject:javax.inject:1"
    const val glide = "com.github.bumptech.glide:glide:4.12.0"
}
