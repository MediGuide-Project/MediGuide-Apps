package capstone.app.mediguide.fragment

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import capstone.app.mediguide.R
import capstone.app.mediguide.databinding.FragmentHistoryBinding
import capstone.app.mediguide.model.ChatHistory
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val db: FirebaseFirestore = Firebase.firestore
    private lateinit var historyAdapter: HistoryAdapter
    private val historyList = mutableListOf<ChatHistory>()
    private var currentUser: FirebaseUser? = null
    private lateinit var auth: FirebaseAuth
    private var isHistoryFragmentVisible: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        historyAdapter = HistoryAdapter(historyList) { chatHistory ->
            openChat(chatHistory)
        }
        binding.rvChat.apply {
            binding.rvChat.layoutManager = LinearLayoutManager(context)
            adapter = historyAdapter
        }

        fetchChatHistory()
    }

    private fun fetchChatHistory() {
        val currentUser = auth.currentUser
        val userId = currentUser?.uid
        if (userId != null) {
            db.collection("chats")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val chatTitle = document
                        val title = document["title"] as? String
                        val chatId = document.id
                        if (title != null) {
                            val chatHistory = ChatHistory(title, chatId)
                            updateChatHistory(chatHistory)
                        }
                    }
                    historyAdapter.notifyDataSetChanged()
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents: ", exception)
                }
        }
    }

    private fun openChat(chatHistory: ChatHistory) {
        val bundle = Bundle()
        bundle.putString("chatId", chatHistory.chatId)
        val chatFragment = ChatFragment()
        chatFragment.arguments = bundle
        parentFragmentManager.beginTransaction()
            .replace(R.id.frameFragment, chatFragment)
            .addToBackStack(null)
            .commit()

        updateChatHistory(chatHistory)
    }

    private fun updateChatHistory(chatHistory: ChatHistory) {
        if (!isHistoryFragmentVisible) {
            val existingIndex = historyList.indexOfFirst { it.chatId == chatHistory.chatId }
            if (existingIndex != -1) {
                historyList[existingIndex] = chatHistory
                historyAdapter.notifyItemChanged(existingIndex)
            } else {
                historyList.add(chatHistory)
                historyAdapter.notifyItemInserted(historyList.size - 1)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

