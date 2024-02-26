package ru.netology.mitune.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.mitune.R
import ru.netology.mitune.adapter.EventAdapter
import ru.netology.mitune.adapter.OnEventInteractionListener
import ru.netology.mitune.adapter.PagingLoadStateAdapter
import ru.netology.mitune.databinding.FragmentFeedEventsBinding
import ru.netology.mitune.dto.Event
import ru.netology.mitune.ui.EditEventFragment.Companion.EDITED_TEXT
import ru.netology.mitune.util.IntArg
import ru.netology.mitune.viewmodel.AuthViewModel
import ru.netology.mitune.viewmodel.EventViewModel

@AndroidEntryPoint
class FeedEventFragment : Fragment() {
    private val authViewModel: AuthViewModel by viewModels()
    private val viewModel: EventViewModel by activityViewModels()



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        val binding = FragmentFeedEventsBinding.inflate(inflater, container, false)

        viewLifecycleOwner.lifecycle.addObserver(
            object : LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    when (event) {
                        Lifecycle.Event.ON_RESUME -> binding.list.createPlayer()
                        Lifecycle.Event.ON_PAUSE,
                        Lifecycle.Event.ON_STOP -> binding.list.releasePlayer()
                        Lifecycle.Event.ON_DESTROY -> source.lifecycle.removeObserver(this)
                        else -> Unit
                    }
                }

            }
        )

        val adapter = EventAdapter(object : OnEventInteractionListener {
            override fun onLike(event: Event) {
                if (authViewModel.authenticated) {
                    if (!event.likedByMe) viewModel.likeEventById(event.id) else viewModel.dislikeEventById(
                        event.id
                    )
                } else {
                    Snackbar.make(binding.root, R.string.login_to_continue, Snackbar.LENGTH_SHORT)
                        .show()
                    findNavController().navigate(R.id.loginFragment)
                }
            }

            override fun onEdit(event: Event) {
                viewModel.editEvent(event)
                val text = event.content
                val bundle = Bundle()
                bundle.putString(EDITED_TEXT, text)
                findNavController().navigate(R.id.action_feedEventFragment_to_editEventFragment, bundle)
            }

            override fun onRemove(event: Event) {
                viewModel.removeEventById(event.id)
            }


        })

        binding.list.adapter = adapter.withLoadStateHeaderAndFooter(
            header = PagingLoadStateAdapter(object :
                PagingLoadStateAdapter.OnPagingInteractionListener {
                override fun onRetry() {
                    adapter.retry()
                }
            }),
            footer = PagingLoadStateAdapter(object :
                PagingLoadStateAdapter.OnPagingInteractionListener {
                override fun onRetry() {
                    adapter.retry()
                }
            }),
        )




        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            if (state.error) {
                Snackbar.make(binding.root, R.string.loading_error, Snackbar.LENGTH_SHORT)
                    .setAction(R.string.retry, { adapter.refresh() })
                    .show()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.data.collectLatest(adapter::submitData)
            }
        }

        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                if (positionStart == 0) {
                    binding.list.smoothScrollToPosition(0)
                }
            }
        })



        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                adapter.loadStateFlow.collectLatest {
                    binding.swipeRefresh.isRefreshing = it.refresh is LoadState.Loading
                }
            }
        }

        binding.swipeRefresh.setOnRefreshListener {
            adapter.refresh()
        }

        return binding.root
    }

    companion object {
        var Bundle.intArg: Int by IntArg
    }
}
