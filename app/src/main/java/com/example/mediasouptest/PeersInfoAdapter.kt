package com.example.mediasouptest

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mediasouptest.media.ConsumerHolder
import com.example.mediasouptest.media.ConsumerScore
import com.example.mediasouptest.media.println
import com.example.mediasouptest.widget.VideoWallpaper

class PeersInfoAdapter(private val onClick: (PeerInfo) -> Unit) :
    ListAdapter<PeerInfo, PeersInfoAdapter.PeerInfoViewHolder>(PeerInfoDiffCallback) {

    class PeerInfoViewHolder(itemView: View, val onClick: (PeerInfo) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val flowerTextView: TextView = itemView.findViewById(R.id.flower_text)
        private val flowerImageView: TextView = itemView.findViewById(R.id.flower_image)
        private val videoWallpaper: VideoWallpaper = itemView.findViewById(R.id.video_wallpaper)
        private val textScore: TextView = itemView.findViewById(R.id.textScore)

        private var currentPeerInfo: PeerInfo? = null

        init {
            itemView.setOnClickListener {
                currentPeerInfo?.let {
                    onClick(it)
                }
            }
        }

        fun bind(peerInfo: PeerInfo) {
            Log.d("_____", "bind$peerInfo")
            currentPeerInfo = peerInfo

            flowerTextView.text = peerInfo.peer.id
            flowerImageView.text = getConsumerInfo(peerInfo.consumerHolder)

            loadVideo(peerInfo.consumerHolder)
        }

        fun bindScore(payloads: MutableList<Any>) {
            payloads.filterIsInstance<ConsumerScore>().firstOrNull()?.let {
                textScore.text = "Producer:${it.producerScore}, Consumer:${it.score}"
            }
        }

        private fun loadVideo(consumerHolder: ConsumerHolder?) {
            if (consumerHolder == null) {
                videoWallpaper.hideVideo()
            } else {
                videoWallpaper.showVideo(consumerHolder.consumer.track)
            }
        }

        private fun getConsumerInfo(consumerHolder: ConsumerHolder?): String {
            return consumerHolder?.println() ?: "null"
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeerInfoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.peer_info_item, parent, false)
        return PeerInfoViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: PeerInfoViewHolder, position: Int) {
        val peerInfo = getItem(position)
        holder.bind(peerInfo)
    }

    override fun onBindViewHolder(holder: PeerInfoViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            holder.bindScore(payloads)
        }
    }

    fun updateConsumerScore(consumerScore: ConsumerScore) {
        val position = currentList.indexOfFirst { it.consumerHolder?.id == consumerScore.consumerId }
        if (position != -1) {
            notifyItemChanged(position, consumerScore)
        }
    }
}

object PeerInfoDiffCallback : DiffUtil.ItemCallback<PeerInfo>() {
    override fun areItemsTheSame(oldItem: PeerInfo, newItem: PeerInfo): Boolean {
        return oldItem.peer.id == newItem.peer.id
    }

    override fun areContentsTheSame(oldItem: PeerInfo, newItem: PeerInfo): Boolean {
        val oldBean = oldItem.consumerHolder?.consumer?.id
        val newBean = newItem.consumerHolder?.consumer?.id
        return oldBean == newBean
    }
}