package com.example.mediasouptest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.mediasoup.droid.lib.model.Peer

class PeerAdapter(private val onClick: (Peer) -> Unit) :
    RecyclerView.Adapter<PeerAdapter.PlateViewHolder>() {
    private var data = mutableListOf<Peer>()

    class PlateViewHolder(itemView: View, val onClick: (Peer) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val textName: TextView = itemView.findViewById(R.id.text_name)
        var currentPlate: Peer? = null

        init {
            itemView.setOnClickListener {
                currentPlate?.let {
                    onClick(it)
                }
            }
        }

        fun bind(plate: Peer) {
            textName.text = plate.displayName + "," + plate.id
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlateViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.charging_station_plate, parent, false)
        return PlateViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: PlateViewHolder, position: Int) {
        holder.bind(data.get(position))
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun setPeers(peers: List<Peer>) {
        data.clear()
        data.addAll(ArrayList(peers))
        notifyDataSetChanged()
    }
}
