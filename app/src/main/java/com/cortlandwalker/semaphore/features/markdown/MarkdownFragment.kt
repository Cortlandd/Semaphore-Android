package com.cortlandwalker.semaphore.features.markdown

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MarkdownViewerFragment : Fragment() {

    private val args: MarkdownViewerFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MarkdownScreen(
                    title = args.title,
                    filename = args.filename,
                    onBack = { findNavController().popBackStack() }
                )
            }
        }
    }
}