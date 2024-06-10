package capstone.app.mediguide.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import capstone.app.mediguide.databinding.FragmentChatBinding
import capstone.app.mediguide.model.ChatMessage
import capstone.app.mediguide.view.HomeActivity
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.runBlocking

class ChatFragment : Fragment() {
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private val chatMessages = mutableListOf<ChatMessage>()
    private lateinit var chatAdapter: ChatAdapter

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

        setupRecyclerView()
        binding.sendButton.setOnClickListener {
            sendMessage()
        }

        binding.question.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else {
                false
            }
        }
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(chatMessages)
        binding.rvChatBot.layoutManager = LinearLayoutManager(requireContext())
        binding.rvChatBot.adapter = chatAdapter
    }

    private fun sendMessage() {
        val messageText = binding.question.text.toString()
        if (messageText.isNotEmpty()) {
            val userMessage = ChatMessage(messageText, true)
            chatMessages.add(userMessage)
            chatAdapter.notifyItemInserted(chatMessages.size - 1)
            binding.question.text?.clear()
            getResponse(messageText) { response ->
                activity?.runOnUiThread {
                    val botMessage = ChatMessage(response, false)
                    chatMessages.add(botMessage)
                    chatAdapter.notifyItemInserted(chatMessages.size - 1)

                }
            }
        }
    }

    private fun getResponse(question: String, callback: (String) -> Unit) {
        val generativeModel = GenerativeModel(
            modelName = "gemini-pro",
            apiKey = "AIzaSyBd9xYiHlUJOJWE9vpll2HmYccLvb_Hyy8"
        )

        runBlocking {
            try {
                val response = generativeModel.generateContent(question)
                response.text?.let { callback(it) }
            } catch (e: Exception) {
                callback("Failed to get response from the server")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
