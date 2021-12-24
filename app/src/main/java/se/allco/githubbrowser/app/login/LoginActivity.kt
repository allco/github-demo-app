package se.allco.githubbrowser.app.login

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.TaskStackBuilder
import androidx.navigation.findNavController
import javax.inject.Inject
import se.allco.githubbrowser.R
import se.allco.githubbrowser.app.di.AppComponent
import se.allco.githubbrowser.app.login.di.LoginComponent
import se.allco.githubbrowser.app.main.MainActivity
import se.allco.githubbrowser.common.ui.FragmentFactory
import se.allco.githubbrowser.common.utils.getViewModel
import se.allco.githubbrowser.common.utils.observe
import se.allco.githubbrowser.common.utils.with
import timber.log.Timber

class LoginActivity : AppCompatActivity() {

    companion object {
        private const val ARG_CALLBACK = "ARG_CALLBACK"

        fun createIntent(currentActivity: Activity): Intent =
            Intent(currentActivity, LoginActivity::class.java)
                .apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra(ARG_CALLBACK, createCallbackPendingIntent(currentActivity))
                }

        private fun createCallbackPendingIntent(activity: Activity) =
            TaskStackBuilder
                .create(activity)
                .addNextIntentWithParentStack(activity.intent)
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

        private fun readCallbackIntent(intent: Intent): PendingIntent? =
            intent.getParcelableExtra(ARG_CALLBACK)
    }

    @Inject
    lateinit var fragmentFactory: FragmentFactory
    @Inject
    lateinit var mediator: LoginMediator

    private val component: LoginComponent
        get() = getViewModel {
            AppComponent
                .getInstance(this)
                .createLoginComponent()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        component.inject(this)
        supportFragmentManager.fragmentFactory = fragmentFactory
        super.onCreate(savedInstanceState)

        setContentView(R.layout.login_activity)
        initObservers()
    }

    private fun initObservers() {
        observe(mediator.finishFlow) with { onLoginFlowSucceeded() }
        observe(mediator.navigateToManualLogin) with { onNavigateToManualLogin() }
    }

    private fun onNavigateToManualLogin() {
        Timber.v("Navigate to Manual login")
        findNavController(R.id.nav_host_fragment).navigate(R.id.to_manual_login)
    }

    private fun onLoginFlowSucceeded() {
        Timber.v("Login flow succeeded")
        readCallbackIntent(intent)?.send()
            ?: startActivity(Intent(this, MainActivity::class.java))
        finishAfterTransition()
    }
}
