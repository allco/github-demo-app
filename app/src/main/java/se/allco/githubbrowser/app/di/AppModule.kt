package se.allco.githubbrowser.app.di

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import se.allco.githubbrowser.BuildConfig
import se.allco.githubbrowser.common.networkreporter.ConnectivityStateReporter
import se.allco.githubbrowser.common.networkreporter.NetworkConnectivityReporterImpl

@Module
class AppModule {

    companion object {
        const val PROCESS_LIFECYCLE_OWNER = "PROCESS_LIFECYCLE_OWNER"
    }

    @Provides
    fun providesApplicationContext(application: Application): Context =
        application.applicationContext

    @Provides
    @Singleton
    fun providesNetworkReporter(impl: NetworkConnectivityReporterImpl): ConnectivityStateReporter =
        impl

    @Provides
    fun provideGsonBuilder(): GsonBuilder = GsonBuilder()

    @Provides
    fun provideSharedPreferences(context: Context): SharedPreferences =
        context.getSharedPreferences("settings", Activity.MODE_PRIVATE)!!

    @Provides
    @Named(PROCESS_LIFECYCLE_OWNER)
    fun getProcessLifeCycle(): LifecycleOwner = ProcessLifecycleOwner.get()

    @Provides
    fun provideOkHttpClientBuilder(): OkHttpClient.Builder =
        OkHttpClient
            .Builder()
            .apply {
                if (BuildConfig.DEBUG) {
                    val interceptor = HttpLoggingInterceptor()
                    interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
                    addInterceptor(interceptor)
                }
            }

    @Provides
    fun provideRetrofitBuilder(gsonBuilder: GsonBuilder): Retrofit.Builder =
        Retrofit
            .Builder()
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .addConverterFactory(
                GsonConverterFactory.create(gsonBuilder.setLenient().create())
            )
}
