package capstone.app.mediguide.fragment

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
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
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.runBlocking
import java.util.UUID

class ChatFragment : Fragment() {
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private val chatList = mutableListOf<ChatMessage>()
    private lateinit var chatAdapter: ChatAdapter
    private val db: FirebaseFirestore = Firebase.firestore
    private var currentUser: FirebaseUser? = null
    private lateinit var auth: FirebaseAuth
    private var chatId: String? = null
    private var currentChatTitle: String? = null
    private var currentChatId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatId = arguments?.getString("chatId")
        if (chatId != null) {
            currentChatId = chatId
            fetchChatMessages(currentChatId!!)
        } else {
            currentChatId = UUID.randomUUID().toString()
        }
        if (chatId != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("chats").document(chatId!!)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val messages = document["messages"] as? List<String>
                        messages?.let {
                            chatList.clear()
                            for (message in it) {
                                addMessageToChat(message, isUser = false)
                            }
                        }
                    } else {
                        Log.d(TAG, "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "get failed with ", exception)
                }
        }

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
            val message = binding.question.text.toString().trim()
            if (message.isNotEmpty()) {
                addMessageToChat(message, true)
                getResponse(message) { response ->
                    addMessageToChat(response, false)
                    saveChatToHistory(message, response)
                }
                binding.question.text?.clear()
            }
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
        chatAdapter = ChatAdapter(chatList)
        binding.rvChatBot.layoutManager = LinearLayoutManager(requireContext())
        binding.rvChatBot.adapter = chatAdapter
    }

    private fun addMessageToChat(message: String, isUser: Boolean) {
        val chatMessage = ChatMessage(message, isUser)
        chatList.add(chatMessage)
        chatAdapter.notifyItemInserted(chatList.size - 1)
        binding.rvChatBot.scrollToPosition(chatList.size - 1)
    }

    private fun saveChatToHistory(userMessage: String, botResponse: String) {
        val currentUser = auth.currentUser
        val userId = currentUser?.uid
        if (userId != null && currentChatId != null) {
            val chatRef = db.collection("chats").document(currentChatId!!)
            chatRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val existingMessages = document["messages"] as? List<String>
                    existingMessages?.let {
                        val updatedMessages = it.toMutableList()
                        updatedMessages.addAll(listOf(userMessage, botResponse))
                        chatRef.update("messages", updatedMessages)
                    }
                } else {
                    val chat = hashMapOf(
                        "title" to userMessage.take(30),
                        "messages" to listOf(userMessage, botResponse),
                        "userId" to userId,
                        "timestamp" to System.currentTimeMillis()
                    )
                    chatRef.set(chat)
                }
            }
        } else {
            Log.w("ChatFragment", "No user is currently signed in or chat ID is null")
        }
    }

    private fun fetchChatMessages(chatId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("chats").document(chatId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val messages = document["messages"] as? List<String>
                    messages?.let {
                        for (message in it) {
                            addMessageToChat(message, isUser = false)
                        }
                    }
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
    }

    private fun sendMessage() {
        val messageText = binding.question.text.toString()
        if (messageText.isNotEmpty()) {
            val userMessage = ChatMessage(messageText, true)
            chatList.add(userMessage)
            chatAdapter.notifyItemInserted(chatList.size - 1)
            binding.question.text?.clear()
            binding.rvChatBot.smoothScrollToPosition(chatList.size - 1)
            getResponse(messageText) { response ->
                activity?.runOnUiThread {
                    val botMessage = ChatMessage(response, false)
                    chatList.add(botMessage)
                    chatAdapter.notifyItemInserted(chatList.size - 1)
                    binding.rvChatBot.smoothScrollToPosition(chatList.size - 1)
                    saveChatToHistory(messageText, response)
                }
            }
            currentChatTitle = null
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
