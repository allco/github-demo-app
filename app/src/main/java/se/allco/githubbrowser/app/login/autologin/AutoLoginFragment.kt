package se.allco.githubbrowser.app.login.autologin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import javax.inject.Inject
import javax.inject.Provider
import se.allco.githubbrowser.app.user.User
import se.allco.githubbrowser.utils.getViewModel
import se.allco.githubbrowser.utils.observe
import se.allco.githubbrowser.utils.with
import se.allco.githubbrowser.databinding.LoginAutoFragmentBinding

class AutoLoginFragment @Inject constructor(
    private val viewModelProvider: Provider<AutoLoginViewModel>,
    private val listener: Listener,
) : Fragment() {

    interface Listener {
        fun onAutoLoginResult(user: User)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = LoginAutoFragmentBinding.inflate(inflater, container, false)
        val viewModel = getViewModel(viewModelProvider)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        observe(viewModel.result) with (::onLoginResult)
        return binding.root
    }

    private fun onLoginResult(user: User) {
        listener.onAutoLoginResult(user)
    }
}
