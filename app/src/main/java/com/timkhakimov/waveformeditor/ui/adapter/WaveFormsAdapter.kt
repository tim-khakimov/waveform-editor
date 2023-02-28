package com.timkhakimov.waveformeditor.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.timkhakimov.waveformeditor.model.AudioWaveForm

class WaveFormsAdapter(
    private val selectWaveFormListener: (AudioWaveForm) -> Unit
) : RecyclerView.Adapter<WaveFormViewHolder>() {

    private var items = listOf<AudioWaveForm>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        WaveFormViewHolder.create(parent)

    override fun onBindViewHolder(holder: WaveFormViewHolder, position: Int) {
        holder.bind(items[position], selectWaveFormListener)
    }

    override fun getItemCount(): Int = items.size

    fun setWaveForms(waveForms: List<AudioWaveForm>) {
        items = waveForms
        //TODO implement DiffUtil
        notifyDataSetChanged()
    }
}
