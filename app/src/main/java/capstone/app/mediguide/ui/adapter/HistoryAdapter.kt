package capstone.app.mediguide.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import capstone.app.mediguide.R
import capstone.app.mediguide.data.model.ChatHistory

class HistoryAdapter(
    private val historyList: List<ChatHistory>,
    private val onItemClick: (ChatHistory) -> Unit,
    private val onDeleteClick: (ChatHistory) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_chat_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val chatHistory = historyList[position]
        holder.bind(chatHistory)
    }

    override fun getItemCount() = historyList.size

    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        private val deleteImageView: ImageView = itemView.findViewById(R.id.deleteIV)

        init {
            itemView.setOnClickListener {
                onItemClick(historyList[adapterPosition])
            }
            deleteImageView.setOnClickListener {
                onDeleteClick(historyList[adapterPosition])
            }
        }

        fun bind(chatHistory: ChatHistory) {
            titleTextView.text = chatHistory.title
        }
    }
}


