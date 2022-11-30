package example.uob.chatbox

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.gson.Gson
import example.uob.chatbox.ItemDetailFragment.ChatItemRecyclerViewAdapter.Const.INCOMING
import example.uob.chatbox.ItemDetailFragment.ChatItemRecyclerViewAdapter.Const.OUTGOING
import example.uob.chatbox.common.Common
import example.uob.chatbox.databinding.FragmentItemDetailBinding
import example.uob.chatbox.databinding.ListItemIncomingMessageBinding
import example.uob.chatbox.databinding.ListItemOutgoingMessageBinding
import example.uob.chatbox.model.InboxItem
import example.uob.chatbox.model.Message

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a [ItemListFragment]
 * in two-pane mode (on larger screen devices) or self-contained
 * on handsets.
 */
class ItemDetailFragment : Fragment() {

    private var _binding: FragmentItemDetailBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var userId: String? = null
    lateinit var messages: ArrayList<Message>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            if (it.containsKey(USER_ID)) {
                userId = it.getString(USER_ID).toString()
            }
            if (it.containsKey(INBOX_ITEM_DETAIL)) {
                val inboxDetailStr = it.getString(INBOX_ITEM_DETAIL)
                val inboxItem = Gson().fromJson(inboxDetailStr, InboxItem::class.java)
                messages = inboxItem.messages as ArrayList<Message>
                messages.reversed()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentItemDetailBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView: RecyclerView? = binding.itemDetailRecycleView

        // Leaving this not using view binding as it relies on if the view is visible the current
        // layout configuration (layout, layout-sw600dp)
        val itemDetailFragmentContainer: View? = view.findViewById(R.id.item_detail_nav_container)

        if (recyclerView != null) {
            userId?.let { setupRecyclerView(recyclerView, it) }
        }
    }

    private fun setupRecyclerView(
        recyclerView: RecyclerView,
        userId: String
    ) {
        recyclerView.adapter = ChatItemRecyclerViewAdapter(
            messages, userId
        )
    }

    class ChatItemRecyclerViewAdapter(
        private var messages: ArrayList<Message>,
        private var userId: String?
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private object Const{
            const val INCOMING = 0
            const val OUTGOING = 1
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : ViewHolder {
            return if (viewType == INCOMING) {
                val view = ListItemIncomingMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                IncomingViewHolder(view)
            } else {
                val view = ListItemOutgoingMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                OutgoingViewHolder(view)
            }
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if (getItemViewType(position) == INCOMING) {
                (holder as IncomingViewHolder).bind(messages[position])
            } else {
                (holder as OutgoingViewHolder).bind(messages[position])
            }
        }

        override fun getItemCount(): Int {
            return messages.size
        }

        override fun getItemViewType(position: Int): Int {
            return if (messages[position].senderId == userId) OUTGOING else INCOMING
        }

        inner class IncomingViewHolder(binding: ListItemIncomingMessageBinding) : RecyclerView.ViewHolder(binding.root) {
            private val textView: TextView = binding.textView
            private val timeTextView: TextView = binding.timeTextView

            fun bind(message: Message) {
                textView.text = message.message
                timeTextView.text = Common.convertGmtToLocalFullTimeString(message.time)
            }
        }

        inner class OutgoingViewHolder(binding: ListItemOutgoingMessageBinding) : RecyclerView.ViewHolder(binding.root) {
            private val textView: TextView = binding.textView
            private val timeTextView: TextView = binding.timeTextView

            fun bind(message: Message) {
                textView.text = message.message
                timeTextView.text = Common.convertGmtToLocalFullTimeString(message.time)
            }
        }
    }

    companion object {
        /**
         * The fragment argument representing the item ID that this fragment
         * represents.
         */
        const val USER_ID = "user_id"
        const val INBOX_ITEM_DETAIL = "inbox_item_detail"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}