package se.allco.githubbrowser.app.login.di

import androidx.lifecycle.ViewModel
import dagger.Subcomponent
import javax.inject.Scope
import se.allco.githubbrowser.app.login.LoginActivity

@Scope
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class LoginScope

@LoginScope
@Subcomponent(modules = [LoginModule::class, LoginFragmentsModule::class])
abstract class LoginComponent : ViewModel() {
    abstract fun inject(activity: LoginActivity)
}
