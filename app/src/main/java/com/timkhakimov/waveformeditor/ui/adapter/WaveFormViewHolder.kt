package com.timkhakimov.waveformeditor.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.timkhakimov.waveformeditor.databinding.ItemWaveformBinding
import com.timkhakimov.waveformeditor.model.AudioWaveForm

class WaveFormViewHolder(
    private val binding: ItemWaveformBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(waveFormItem: AudioWaveForm, selectWaveFormListener: (AudioWaveForm) -> Unit) {
        binding.waveFormNameTextView.text = waveFormItem.name
        binding.root.setOnClickListener { selectWaveFormListener(waveFormItem) }
    }

    companion object {

        fun create(parent: ViewGroup): WaveFormViewHolder {
            return WaveFormViewHolder(
                ItemWaveformBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }
    }
}
