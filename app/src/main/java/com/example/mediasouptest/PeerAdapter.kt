package com.example.mediasouptest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mediasouptest.media.ConsumerHolder
import org.mediasoup.droid.lib.PeerConnectionUtils
import org.mediasoup.droid.lib.model.Peer
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoTrack

class PeerAdapter(private val onClick: (Peer) -> Unit) :
    RecyclerView.Adapter<PeerAdapter.PlateViewHolder>() {
    private var data = mutableListOf<Peer>()
    private var consumerList = mutableListOf<ConsumerHolder>()

    class PlateViewHolder(itemView: View, val onClick: (Peer) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val textName: TextView = itemView.findViewById(R.id.text_name)
        private val videoRenderer: SurfaceViewRenderer = itemView.findViewById(R.id.video_renderer)

        var currentPlate: Peer? = null

        init {
            videoRenderer.init(PeerConnectionUtils.getEglContext(), null)
            itemView.setOnClickListener {
                currentPlate?.let {
                    onClick(it)
                }
            }
        }

        fun bind(plate: Peer, consumerList: MutableList<ConsumerHolder>) {
            textName.text = plate.displayName + "," + plate.id
            render(videoRenderer, plate, consumerList)
        }

        fun render(renderer: SurfaceViewRenderer, peer: Peer, consumerList: MutableList<ConsumerHolder>) {
            consumerList.firstOrNull {
                it.peerId == peer.id
            }?.takeIf { consumerHolder ->
                consumerHolder.consumer.track is VideoTrack
            }?.let {
                it.consumer.track as VideoTrack
            }?.addSink(renderer)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlateViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.charging_station_plate, parent, false)
        return PlateViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: PlateViewHolder, position: Int) {
        holder.bind(data.get(position), consumerList)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun onNewConsumer(consumerHolder: ConsumerHolder){
        consumerList.add(consumerHolder)
        notifyDataSetChanged()
    }

    fun setPeers(peers: List<Peer>) {
        data.clear()
        data.addAll(ArrayList(peers))
        notifyDataSetChanged()
    }
}
