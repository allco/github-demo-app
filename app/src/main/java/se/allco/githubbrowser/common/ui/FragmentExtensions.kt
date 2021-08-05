package se.allco.githubbrowser.common.ui

import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment

fun Fragment.overrideOnBackPress(onBack: OnBackPressedCallback.() -> Unit): OnBackPressedCallback =
    object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            with(this, onBack)
        }
    }.also {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, it)
    }
