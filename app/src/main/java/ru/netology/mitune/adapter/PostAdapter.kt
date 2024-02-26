package ru.netology.mitune.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.MediaItem
import ru.netology.mitune.R
import ru.netology.mitune.databinding.CardPostBinding
import ru.netology.mitune.dto.AttachmentType
import ru.netology.mitune.dto.Post
import ru.netology.mitune.util.Utils
import ru.netology.mitune.util.loadCircleCrop

interface OnPostInteractionListener {
    fun onAvatarClick(post: Post)
    fun onLinkClick(url: String)
    fun onLike(post: Post)
    fun onEdit(post: Post)
    fun onRemove(post: Post)
}

class PostAdapter(
    private val onPostInteractionListener: OnPostInteractionListener
) : PagingDataAdapter<Post, PostViewHolder>(PostDiffCallback()) {
    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val item = getItem(position) ?: return
        holder.bind(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = CardPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return PostViewHolder(binding, onPostInteractionListener)
    }


}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onPostInteractionListener: OnPostInteractionListener
) : RecyclerView.ViewHolder(binding.root) {

    private val parentView = binding.root
    val videoThumbnail = binding.videoThumbnail
    val videoContainer = binding.videoContainer
    val videoProgressBar = binding.videoProgressbar
    var videoPreview: MediaItem? = null
    val videoPlayIcon: ImageView = binding.videoPlayIcon

    fun bind(post: Post) {
        parentView.tag = this

        binding.apply {
            authorName.text = post.author
            published.text =
                Utils.formatMillisToDateTimeString(post.published.toEpochMilli())
            postText.text = post.content


            post.authorAvatar?.let {
                avatar.loadCircleCrop(it)
            } ?: avatar.setImageDrawable(

                AppCompatResources.getDrawable(
                    itemView.context,
                    R.drawable.avatar_placeholder
                )
            )

            avatar.setOnClickListener {
                onPostInteractionListener.onAvatarClick(post)
            }

            link.text = post.link

            link.setOnClickListener {
                var url = link.text.toString()
                if (!url.startsWith("http://") || !url.startsWith("https://"))
                    url = "http://" + url
                onPostInteractionListener.onLinkClick(url)
            }


            like.isChecked = post.likedByMe

            like.setOnClickListener {
                onPostInteractionListener.onLike(post)
            }


            if (post.attachment == null) {
                postImage.visibility = View.GONE
                videoContainer.visibility = View.GONE
                videoPreview = null
            } else {
                when (post.attachment.type) {
                    AttachmentType.IMAGE -> {
                        videoContainer.visibility = View.GONE
                        postImage.visibility = View.VISIBLE
                        videoPreview = null
                        Glide.with(binding.postImage)
                            .load(post.attachment.url)
                            .into(binding.postImage)
                    }
                    AttachmentType.VIDEO -> {
                        videoContainer.visibility = View.VISIBLE
                        postImage.visibility = View.GONE
                        videoPreview = MediaItem.fromUri(post.attachment.url)

                        Glide.with(parentView).load(post.attachment.url).into(videoThumbnail)

                    }
                    AttachmentType.AUDIO -> {
                        videoContainer.visibility = View.VISIBLE
                        postImage.visibility = View.GONE
                        videoPreview = MediaItem.fromUri(post.attachment.url)

                    }
                }
            }

            postMenu.isVisible = post.ownedByMe
            // postMenu.visibility = if (post.ownedByMe) View.VISIBLE else View.INVISIBLE

            postMenu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.list_item_menu)
                    menu.setGroupVisible(R.menu.list_item_menu, post.ownedByMe)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.edit -> {
                                onPostInteractionListener.onEdit(post)
                                true
                            }
                            R.id.remove -> {
                                onPostInteractionListener.onRemove(post)
                                true
                            }
                            else -> false
                        }
                    }
                }.show()

            }
        }

    }
}


class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        if (oldItem::class != newItem::class) {
            return false
        }

        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }

}

