package se.allco.githubbrowser.app.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import se.allco.githubbrowser.R
import se.allco.githubbrowser.app.main.di.MainComponent
import se.allco.githubbrowser.app.user.UserComponentHolder
import se.allco.githubbrowser.common.ui.ensureUserLoggedIn
import se.allco.githubbrowser.common.utils.getViewModel
import se.allco.githubbrowser.databinding.MainActiviyBinding

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        inject()
        super.onCreate(savedInstanceState)
        ensureUserLoggedIn { initViews() }
    }

    private fun inject() {
        val component = getMainComponent()
        component.inject(this)
        supportFragmentManager.fragmentFactory = component.getFragmentFactory()
    }

    private fun initViews() {
        val binding =
            DataBindingUtil.setContentView<MainActiviyBinding>(this, R.layout.main_activiy)

        val navController = getNavController()
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.navigation_repos, R.id.navigation_account)
        )
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
        binding.bottomNavigation.setupWithNavController(navController)
    }

    private fun getNavController(): NavController {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        return navHostFragment.navController
    }

    private fun getMainComponent(): MainComponent = getViewModel {
        UserComponentHolder
            .getUserComponent(this)
            .createMainComponent()
    }
}
