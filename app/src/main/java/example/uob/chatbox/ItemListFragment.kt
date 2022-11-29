package example.uob.chatbox

import android.content.res.AssetManager
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import example.uob.chatbox.databinding.FragmentItemListBinding
import example.uob.chatbox.databinding.ItemListContentBinding
import example.uob.chatbox.model.ChatResponse
import example.uob.chatbox.model.GetChatsHistoryResponse
import example.uob.chatbox.model.InboxItem

/**
 * A Fragment representing a list of Pings. This fragment
 * has different presentations for handset and larger screen devices. On
 * handsets, the fragment presents a list of items, which when touched,
 * lead to a {@link ItemDetailFragment} representing
 * item details. On larger screens, the Navigation controller presents the list of items and
 * item details side-by-side using two vertical panes.
 */

class ItemListFragment : Fragment() {

    /**
     * Method to intercept global key events in the
     * item list fragment to trigger keyboard shortcuts
     * Currently provides a toast when Ctrl + Z and Ctrl + F
     * are triggered
     */
    private val unhandledKeyEventListenerCompat =
        ViewCompat.OnUnhandledKeyEventListenerCompat { v, event ->
            if (event.keyCode == KeyEvent.KEYCODE_Z && event.isCtrlPressed) {
                Toast.makeText(
                    v.context,
                    "Undo (Ctrl + Z) shortcut triggered",
                    Toast.LENGTH_LONG
                ).show()
                true
            } else if (event.keyCode == KeyEvent.KEYCODE_F && event.isCtrlPressed) {
                Toast.makeText(
                    v.context,
                    "Find (Ctrl + F) shortcut triggered",
                    Toast.LENGTH_LONG
                ).show()
                true
            }
            false
        }

    private var _binding: FragmentItemListBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentItemListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.addOnUnhandledKeyEventListener(view, unhandledKeyEventListenerCompat)

        val recyclerView: RecyclerView = binding.itemList

        // Leaving this not using view binding as it relies on if the view is visible the current
        // layout configuration (layout, layout-sw600dp)
        val itemDetailFragmentContainer: View? = view.findViewById(R.id.item_detail_nav_container)

        setupRecyclerView(recyclerView, itemDetailFragmentContainer)
    }

    private fun setupRecyclerView(
        recyclerView: RecyclerView,
        itemDetailFragmentContainer: View?
    ) {

        val gson = Gson()
        val chatResponse: GetChatsHistoryResponse = gson.fromJson(activity?.assets?.readAssetsFile("chat.json"), GetChatsHistoryResponse::class.java)

        recyclerView.adapter = chatResponse.response?.inbox?.let {
            chatResponse.response!!.userDetail?.userId?.let { it1 ->
                ChatsRecyclerViewAdapter(
                    it, itemDetailFragmentContainer, it1
                )
            }
        }
    }

    private fun AssetManager.readAssetsFile(fileName: String): String = open(fileName).bufferedReader().use { it.readText() }

    class ChatsRecyclerViewAdapter(
        private val values: List<InboxItem>,
        private val itemDetailFragmentContainer: View?,
        private val userId: String
    ) :
        RecyclerView.Adapter<ChatsRecyclerViewAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemListContentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = values[position]
            holder.nameTextView.text = item.senderName
            holder.lastMessage.text = item.messages?.get(0)?.message ?: ""
            holder.lastMessageTime.text = item.messages?.get(0)?.time

            with(holder.itemView) {
                tag = item
                setOnClickListener { itemView ->
                    val item = itemView.tag as InboxItem
                    val bundle = Bundle()
                    bundle.putString(
                        ItemDetailFragment.USER_ID,
                        userId
                    )
                    bundle.putString(
                        ItemDetailFragment.INBOX_ITEM_DETAIL,
                        Gson().toJson(item, InboxItem::class.java)
                    )
                    if (itemDetailFragmentContainer != null) {
                        itemDetailFragmentContainer.findNavController()
                            .navigate(R.id.fragment_item_detail, bundle)
                    } else {
                        itemView.findNavController().navigate(R.id.show_item_detail, bundle)
                    }
                }
            }
        }

        override fun getItemCount() = values.size

        inner class ViewHolder(binding: ItemListContentBinding) :
            RecyclerView.ViewHolder(binding.root) {
            val nameTextView: TextView = binding.nameTextView
            val lastMessage: TextView = binding.lastMessage
            val lastMessageTime: TextView = binding.lastMessageTime
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}