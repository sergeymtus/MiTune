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
import ru.netology.mitune.databinding.FragmentEditEventBinding
import ru.netology.mitune.util.StringArg
import ru.netology.mitune.util.Utils
import ru.netology.mitune.viewmodel.EventViewModel

@AndroidEntryPoint
class EditEventFragment : Fragment() {
    private val viewModel: EventViewModel by activityViewModels()
    companion object {
        var Bundle.edit: String? by StringArg

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentEditEventBinding.inflate(inflater, container,false)

        arguments?.edit?.let(binding.edit::setText)
        binding.edit.setText(arguments?.getString("editedText"))


        binding.ok.setOnClickListener {
            if (binding.edit.text.isNullOrBlank()) {
                Toast.makeText(
                    activity,
                    this.getString(R.string.field_cant_be_empty),
                    Toast.LENGTH_SHORT
                )
                    .show()
            } else {
                viewModel.changeContent(binding.edit.text.toString())
                viewModel.saveEvent()
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