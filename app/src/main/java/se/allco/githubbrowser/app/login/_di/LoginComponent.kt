package se.allco.githubbrowser.app.login._di

import androidx.lifecycle.ViewModel
import dagger.Subcomponent
import se.allco.githubbrowser.app.login.LoginActivity
import javax.inject.Scope

@Scope
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class LoginScope

@LoginScope
@Subcomponent(modules = [LoginModule::class, LoginFragmentsModule::class])
abstract class LoginComponent : ViewModel() {
    abstract fun inject(activity: LoginActivity)
}
