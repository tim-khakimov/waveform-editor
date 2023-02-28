package com.timkhakimov.waveformeditor.data

import com.timkhakimov.waveformeditor.model.AudioWaveForm
import com.timkhakimov.waveformeditor.model.WaveItem

interface WaveFormsRepository {

    suspend fun getWaveForms(): List<AudioWaveForm>

    suspend fun getWaveItemsFromForm(waveFormItem: AudioWaveForm): List<WaveItem>

    suspend fun addWaveForm(waveItems: List<WaveItem>)
}
