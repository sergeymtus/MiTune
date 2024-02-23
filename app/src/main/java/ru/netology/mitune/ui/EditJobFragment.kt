package ru.netology.mitune.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.mitune.R
import ru.netology.mitune.databinding.FragmentEditJobBinding
import ru.netology.mitune.util.Utils
import ru.netology.mitune.viewmodel.UserProfileViewModel

@AndroidEntryPoint
class EditJobFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewModel: UserProfileViewModel by activityViewModels()

        val binding = FragmentEditJobBinding.inflate(inflater, container, false)

        val job = viewModel.editedJob

        job.value?.let{
            binding.company.setText(it.name)
            binding.position.setText(it.position)
            binding.addStartDate.setText(it.start)
            binding.addEndDate.setText(it.finish)
            binding.link.setText(it.link)
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


        binding.ok.setOnClickListener {
            if (binding.company.text.isNullOrBlank() || binding.position.text.isNullOrBlank() || binding.addStartDate.text.isNullOrBlank()) {
                Toast.makeText(
                    activity,
                    this.getString(R.string.field_cant_be_empty),
                    Toast.LENGTH_SHORT
                )
                    .show()
            } else {
                viewModel.changeJobCompany(binding.company.text.toString())
                viewModel.changeJobPosition(binding.position.text.toString())
                viewModel.changeJobLink(binding.link.text.toString())
                viewModel.updateStartDate(binding.addStartDate.text.toString())
                viewModel.updateEndDate(binding.addEndDate.text.toString())
                viewModel.saveJob()
                Utils.hideKeyboard(requireView())
                findNavController().navigateUp()
            }

        }

        binding.cancelButton.setOnClickListener {
            Utils.hideKeyboard(requireView())
            findNavController().navigateUp()
        }

        return binding.root
    }
}