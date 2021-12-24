package se.allco.githubbrowser.app.login._di

import dagger.Module
import dagger.Provides
import se.allco.githubbrowser.app.login.LoginRepository
import se.allco.githubbrowser.app.login.LoginRepositoryImpl
import se.allco.githubbrowser.app.login.LoginUseCase
import se.allco.githubbrowser.app.login.autologin.AutoLoginFragment
import se.allco.githubbrowser.app.login.autologin.AutoLoginRepository
import se.allco.githubbrowser.app.login.manuallogin.ManualLoginFragment
import se.allco.githubbrowser.app.login.manuallogin.ManualLoginRepository

@Module
class LoginModule {

    @Provides
    fun provideAutoLoginFragmentListener(impl: LoginUseCase): AutoLoginFragment.Listener =
        impl

    @Provides
    fun provideManualLoginFragmentListener(impl: LoginUseCase): ManualLoginFragment.Listener =
        impl

    @Provides
    fun provideAutoLoginRepository(impl: LoginRepositoryImpl): AutoLoginRepository = impl

    @Provides
    fun provideManualLoginRepository(impl: LoginRepositoryImpl): ManualLoginRepository = impl

    @Provides
    fun provideLoginRepository(impl: LoginRepositoryImpl): LoginRepository = impl
}
