package se.allco.githubbrowser.app.main.repos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import javax.inject.Inject
import javax.inject.Provider
import se.allco.githubbrowser.common.utils.getViewModel
import se.allco.githubbrowser.databinding.MainReposFragmentBinding

class ReposFragment @Inject constructor(
    private val viewModelProvider: Provider<ReposViewModel>,
) : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = MainReposFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = getViewModel(viewModelProvider)
        return binding.root
    }
}
