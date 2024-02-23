package ru.netology.mitune.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.mitune.R
import ru.netology.mitune.databinding.FragmentNewJobBinding
import ru.netology.mitune.util.Utils
import ru.netology.mitune.viewmodel.UserProfileViewModel

@AndroidEntryPoint
class NewJobFragment : Fragment() {
    private val viewModel: UserProfileViewModel by activityViewModels()
    private lateinit var navController : NavController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentNewJobBinding.inflate(inflater, container, false)
        navController = findNavController()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            Snackbar.make(binding.root, R.string.skip_edit_question, Snackbar.LENGTH_SHORT)
                .setAction(R.string.exit) {
                    viewModel.deleteEditJob()
                    navController.navigateUp()
                }.show()
        }

        binding.addStartDate.setOnClickListener {
            Utils.selectDateDialog(binding.addStartDate, requireContext())
            val startDate = binding.addStartDate.text.toString()
            viewModel.updateStartDate(startDate)
        }

        binding.addEndDate.setOnClickListener {
            Utils.selectDateDialog(binding.addEndDate, requireContext())
            val endDate = binding.addEndDate.text.toString()
            viewModel.updateEndDate(endDate)
        }

        binding.save.setOnClickListener {
            Utils.hideKeyboard(requireView())
            if (binding.company.text.toString().isBlank() || binding.position.text.toString().isBlank() || binding.addStartDate.text.toString().isBlank()) {
                Snackbar.make(
                    binding.root,
                    R.string.field_cant_be_empty,
                    Snackbar.LENGTH_SHORT
                ).show()
            } else {
                if (requireNotNull(viewModel.editedJob.value).id == 0) {
                    0
                } else {
                    requireNotNull(viewModel.editedJob.value).id
                }
                val name = binding.company.text.trim().toString()
                val position = binding.position.text.trim().toString()
                val start = binding.addStartDate.text.trim().toString()
                val finish = if (binding.addEndDate.text.toString().isBlank()) {
                    null
                } else {
                    binding.addEndDate.text.trim().toString()
                }
                val link = if (binding.link.text.toString().isBlank()) {
                    null
                } else {
                    binding.link.text.trim().toString()
                }
                viewModel.changeJobCompany(name)
                viewModel.changeJobPosition(position)
                viewModel.updateStartDate(start)
                viewModel.changeJobLink(link)
                viewModel.updateEndDate(finish)
                viewModel.saveJob()
                navController.navigateUp()
            }
        }

        return binding.root

    }

}