package com.example.mediasouptest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mediasouptest.media.ConsumerHolder
import com.example.mediasouptest.widget.VideoWallpaper
import org.mediasoup.droid.lib.model.Peer

data class PeerInfo(val peer: Peer, var consumerHolder: ConsumerHolder?)

class PeerAdapter(private val onClick: (Peer) -> Unit) :


    RecyclerView.Adapter<PeerAdapter.PlateViewHolder>() {
    private var data = mutableListOf<PeerInfo>()

    class PlateViewHolder(itemView: View, val onClick: (Peer) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val textName: TextView = itemView.findViewById(R.id.text_name)
        private val videoWallpaper: VideoWallpaper = itemView.findViewById(R.id.video_wallpaper)

        fun bind(plate: PeerInfo) {
            textName.text = plate.peer.displayName + "," + plate.peer.id
            render(videoWallpaper, plate)
        }

        private fun render(videoWallpaper: VideoWallpaper, peer: PeerInfo) {
            val consumerHolder = peer.consumerHolder
            if (consumerHolder == null) {
                videoWallpaper.hideVideo()
            } else {
                videoWallpaper.showVideo(consumerHolder.consumer.track)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlateViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_peer_list, parent, false)
        return PlateViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: PlateViewHolder, position: Int) {
        holder.bind(data.get(position))
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun onVideoConsumer(consumers: List<ConsumerHolder>) {
        for (info in data) {
            val id = info.peer.id
            val c = consumers.firstOrNull { it.peerId == id }
            info.consumerHolder = c
        }
        notifyDataSetChanged()
    }

    fun setPeers(peers: List<Peer>) {
        data.clear()
        data.addAll(peers.map {
            PeerInfo(it, null)
        })
        notifyDataSetChanged()
    }

    fun removeAll() {
        data.clear()
        notifyDataSetChanged()
    }
}
