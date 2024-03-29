package se.allco.githubbrowser.app.main.di

import androidx.lifecycle.ViewModel
import dagger.Subcomponent
import javax.inject.Scope
import se.allco.githubbrowser.app.main.MainActivity
import se.allco.githubbrowser.common.ui.FragmentFactory

@Scope
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class MainScope

@MainScope
@Subcomponent(modules = [MainModule::class, MainFragmentsModule::class])
abstract class MainComponent : ViewModel() {
    abstract fun getFragmentFactory(): FragmentFactory
    abstract fun inject(mainActivity: MainActivity)
}
