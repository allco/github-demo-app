package se.allco.githubbrowser.app.user.di

import dagger.BindsInstance
import dagger.Subcomponent
import javax.inject.Scope
import se.allco.githubbrowser.app.main.di.MainComponent
import se.allco.githubbrowser.app.user.User

@Scope
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class UserScope

@UserScope
@Subcomponent
interface UserComponent {
    @Subcomponent.Factory
    interface Factory {
        fun create(@BindsInstance user: User): UserComponent
    }

    fun createMainComponent(): MainComponent
    fun getCurrentUser(): User
}
