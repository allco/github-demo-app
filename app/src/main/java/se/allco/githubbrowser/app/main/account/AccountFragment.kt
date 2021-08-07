package se.allco.githubbrowser.app.main.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import se.allco.githubbrowser.common.utils.getViewModel
import se.allco.githubbrowser.databinding.MainAccountFragmentBinding
import javax.inject.Inject
import javax.inject.Provider

class AccountFragment @Inject constructor(
    private val accountViewModelProvider: Provider<AccountViewModel>,
) : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = MainAccountFragmentBinding.inflate(inflater, container, false)
        binding.viewModel = getViewModel(accountViewModelProvider)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }
}
