package capstone.app.mediguide.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import capstone.app.mediguide.databinding.FragmentChatBinding
import capstone.app.mediguide.view.HomeActivity

class ChatFragment : Fragment() {
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.floatingActionButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.question.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            val activity = activity as HomeActivity?
            if (hasFocus) {
                activity?.hideBottomNavView()
            } else {
                activity?.showBottomNavView()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}