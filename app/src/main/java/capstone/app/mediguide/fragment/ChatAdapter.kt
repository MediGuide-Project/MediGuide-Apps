package capstone.app.mediguide.fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import capstone.app.mediguide.R
import capstone.app.mediguide.databinding.ItemChatBinding
import capstone.app.mediguide.model.ChatMessage

class ChatAdapter(private val chatList: List<ChatMessage>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(val binding: ItemChatBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chatMessage = chatList[position]
        if (chatMessage.isUser) {
            holder.binding.userMessageTextView.visibility = View.VISIBLE
            holder.binding.botMessageTextView.visibility = View.GONE
            holder.binding.userMessageTextView.text = chatMessage.message
            holder.binding.userMessageTextView.setBackgroundResource(R.drawable.user_message_bg)
            holder.binding.logo.visibility = View.GONE
        } else {
            holder.binding.userMessageTextView.visibility = View.GONE
            holder.binding.botMessageTextView.visibility = View.VISIBLE
            holder.binding.botMessageTextView.text = chatMessage.message
            holder.binding.botMessageTextView.setBackgroundResource(R.drawable.bot_message_bg)
            holder.binding.logo.visibility = View.VISIBLE
        }
    }

    override fun getItemCount() = chatList.size
}
