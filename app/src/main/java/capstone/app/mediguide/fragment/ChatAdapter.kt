package capstone.app.mediguide.fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import capstone.app.mediguide.databinding.ItemChatBinding
import capstone.app.mediguide.model.ChatMessage

class ChatAdapter(private val chatMessages: List<ChatMessage>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(val binding: ItemChatBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chatMessage = chatMessages[position]
        if (chatMessage.isUser) {
            holder.binding.userMessageTextView.visibility = View.VISIBLE
            holder.binding.botMessageTextView.visibility = View.GONE
            holder.binding.userMessageTextView.text = chatMessage.message
        } else {
            holder.binding.userMessageTextView.visibility = View.GONE
            holder.binding.botMessageTextView.visibility = View.VISIBLE
            holder.binding.botMessageTextView.text = chatMessage.message
        }
    }

    override fun getItemCount() = chatMessages.size
}
