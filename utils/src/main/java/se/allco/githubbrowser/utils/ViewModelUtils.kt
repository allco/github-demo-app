package se.allco.githubbrowser.utils

import android.app.Application
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import javax.inject.Provider

/**
 *  SAM (single abstract method interface) conversion does not work well for
 *  ViewModelProvider.Factory because it is not declared as a generic interface
 *  but it has a generic method.
 *
 *  This method lets to pass a lambda as a factory to [androidx.lifecycle.ViewModelProviders.of]
 *
 *  Example:
 *  ```
 *     ViewModelProviders.of(this,
 *             createViewFactoryFactory {Application.appComponent(context).createLoginViewModel() })
 *             .get(LoginViewModel::class.java)
 *   ```
 *  instead of:
 *  ```
 *     ViewModelProviders.of(this,
 *             object : ViewModelProvider.Factory{
 *             override fun <T : ViewModel?> create(modelClass: Class<T>): T {
 *                   return  Application.appComponent(context).createLoginViewModel() as T
 *                 }
 *             })
 *             .get(LoginViewModel::class.java)
 * ```
 *
 *  @see <a href="https://kotlinlang.org/docs/reference/java-interop.html#sam-conversions">SAM conversion</a>
 */
fun <T : ViewModel> createViewFactoryFactory(factory: () -> T): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = factory() as T
    }
}

/**
 * Returns an instance of the view model, it either creates an instance or fetches one if it exists for this fragment in
 * the ViewModelStorage
 */
inline fun <reified T : ViewModel> Fragment.getViewModel(viewModelProvider: Provider<T>): T =
    ViewModelProvider(this, createViewFactoryFactory { viewModelProvider.get() }).get(T::class.java)

inline fun <reified T : ViewModel> FragmentActivity.getViewModel(viewModelProvider: Provider<T>): T =
    ViewModelProvider(this, createViewFactoryFactory { viewModelProvider.get() }).get(T::class.java)

inline fun <reified T : ViewModel> FragmentActivity.getViewModel(crossinline viewModelProvider: () -> T): T =
    ViewModelProvider(this, createViewFactoryFactory { viewModelProvider() }).get(T::class.java)

fun AndroidViewModel.getString(@StringRes res: Int): String =
    this.getApplication<Application>().getString(res)
