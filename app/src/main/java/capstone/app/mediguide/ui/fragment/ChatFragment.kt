package capstone.app.mediguide.ui.fragment

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import capstone.app.mediguide.R
import capstone.app.mediguide.data.api.ApiService
import capstone.app.mediguide.data.api.GenerateRequest
import capstone.app.mediguide.data.api.MyNetworkClient
import capstone.app.mediguide.databinding.FragmentChatBinding
import capstone.app.mediguide.data.model.ChatMessage
import capstone.app.mediguide.ui.activity.HomeActivity
import capstone.app.mediguide.ui.adapter.ChatAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class ChatFragment : Fragment() {
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private val chatList = mutableListOf<ChatMessage>()
    private lateinit var chatAdapter: ChatAdapter
    private val db: FirebaseFirestore = Firebase.firestore
    private var currentUser: FirebaseUser? = null
    private lateinit var auth: FirebaseAuth
    private var currentChatId: String? = null
    private lateinit var backgroundMessage: TextView
    private lateinit var logo1: ImageView

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

        backgroundMessage = view.findViewById(R.id.background_message)
        logo1 = view.findViewById(R.id.logo1)
        backgroundMessage.text = getString(R.string.background_text)

        currentChatId = arguments?.getString("chatId") ?: UUID.randomUUID().toString()
        fetchChatMessages(currentChatId!!)

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
        db.collection("chats").document(chatId).get().addOnSuccessListener { document ->
            if (document.exists()) {
                hideBackgroundMessage()
                val messages = document["messages"] as? List<String>
                messages?.let {
                    chatList.clear() // Clear the chat list to avoid duplication
                    for ((index, message) in it.withIndex()) {
                        val isUser = index % 2 == 0 // Even index means bot message, odd index means user message
                        addMessageToChat(message, isUser)
                    }
                }
            } else {
                Log.d(TAG, "No such document")
            }
        }.addOnFailureListener { exception ->
            Log.d(TAG, "get failed with ", exception)
        }
    }

    private fun sendMessage() {
        val messageText = binding.question.text.toString()
        if (messageText.isNotEmpty()) {
            addMessageToChat(messageText, true)
            hideBackgroundMessage()
            binding.question.text?.clear()
            hideKeyboard()
            getResponse(messageText) { response ->
                addMessageToChat(response, false)
                saveChatToHistory(messageText, response)
            }
        }
    }

    private fun getResponse(question: String, callback: (String) -> Unit) {
        val request = GenerateRequest(Patient = question)
        val retrofit = MyNetworkClient.retrofit
        val apiService = retrofit.create(ApiService::class.java)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("ChatFragment", "Sending request to server...")
                val response = apiService.generateResponse(request)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Log.d("ChatFragment", "Received response from server")
                        response.body()?.response?.let {
                            Log.d("ChatFragment", "Response from server: $it")
                            callback(it)
                        } ?: run {
                            Log.e("ChatFragment", "Response output is null")
                            callback("No response from server")
                        }
                    } else {
                        Log.e("ChatFragment", "Error response code: ${response.code()}")
                        response.errorBody()?.string()?.let { errorBody ->
                            Log.e("ChatFragment", "Error response body: $errorBody")
                        }
                        callback("Error response from server: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("ChatFragment", "Error: ${e.message}", e)
                    callback("Failed to get response from the server")
                }
            }
        }
    }

    private fun hideBackgroundMessage() {
        backgroundMessage.visibility = View.GONE
        logo1.visibility = View.GONE
    }

    private fun hideKeyboard() {
        val inputMethodManager = activity?.getSystemService(InputMethodManager::class.java)
        inputMethodManager?.hideSoftInputFromWindow(binding.question.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
