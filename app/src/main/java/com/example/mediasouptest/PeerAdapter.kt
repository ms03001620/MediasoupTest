package com.example.mediasouptest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mediasouptest.media.ConsumerHolder
import com.example.mediasouptest.widget.VideoWallpaper
import org.mediasoup.droid.lib.model.Peer
import org.webrtc.VideoTrack

class PeerAdapter(private val onClick: (Peer) -> Unit) :
    RecyclerView.Adapter<PeerAdapter.PlateViewHolder>() {
    private var data = mutableListOf<Peer>()
    private var consumerList = mutableListOf<ConsumerHolder>()

    class PlateViewHolder(itemView: View, val onClick: (Peer) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val textName: TextView = itemView.findViewById(R.id.text_name)
        private val videoWallpaper: VideoWallpaper = itemView.findViewById(R.id.video_wallpaper)

        var currentPlate: Peer? = null

        init {
            itemView.setOnClickListener {
                currentPlate?.let {
                    onClick(it)
                }
            }
        }

        fun bind(plate: Peer, consumerList: MutableList<ConsumerHolder>) {
            textName.text = plate.displayName + "," + plate.id
            render(videoWallpaper, plate, consumerList)
        }

        private fun render(videoWallpaper: VideoWallpaper, peer: Peer, consumerList: MutableList<ConsumerHolder>) {
            consumerList.firstOrNull {
                it.peerId == peer.id
            }?.let {
                it.consumer.track as VideoTrack
            }?.addSink(videoWallpaper.getRenderer())
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlateViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_peer_list, parent, false)
        return PlateViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: PlateViewHolder, position: Int) {
        holder.bind(data.get(position), consumerList)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun onNewConsumer(consumers: List<ConsumerHolder>) {
        consumerList.clear()
        consumerList.addAll(consumers)
        notifyDataSetChanged()
    }

    fun setPeers(peers: List<Peer>) {
        data.clear()
        data.addAll(ArrayList(peers))
        notifyDataSetChanged()
    }
}
