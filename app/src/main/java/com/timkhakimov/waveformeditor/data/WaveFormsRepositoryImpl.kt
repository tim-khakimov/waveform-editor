package com.timkhakimov.waveformeditor.data

import android.content.Context
import com.timkhakimov.waveformeditor.model.AudioWaveForm
import com.timkhakimov.waveformeditor.model.WaveItem
import java.io.File
import java.io.InputStreamReader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class WaveFormsRepositoryImpl(
    private val context: Context,
) : WaveFormsRepository {

    private val itemsFromAssets = MutableStateFlow<List<AudioWaveForm>>(listOf())

    override suspend fun getWaveForms(): List<AudioWaveForm> {
        if (itemsFromAssets.value.isEmpty()) {
            loadFromAssets()
        }
        return suspendCoroutine {
            it.resume(itemsFromAssets.value)
        }
    }

    override suspend fun getWaveItemsFromForm(waveFormItem: AudioWaveForm): List<WaveItem> {
        return if (waveFormItem.isFromAssets) {
            getWavesFromAssets(waveFormItem.name)
        } else {
            getWavesFromDownload(waveFormItem.name)
        }
    }

    private suspend fun getWavesFromAssets(name: String): List<WaveItem> {
        val fullPath = WAVEFORMS_DIRECTORY + File.separator + name
        val streamReader: InputStreamReader = context.assets.open(fullPath).reader()
        return streamReader.getWaveItems()
    }

    private suspend fun getWavesFromDownload(name: String): List<WaveItem> {
        TODO("Not yet implemented")
    }

    private fun InputStreamReader.getWaveItems(): List<WaveItem> {
        return readLines().map {
            val parts = it.split(" ")
            WaveItem(
                bottom = parts[0].toDouble(),
                top = parts[1].toDouble(),
            )
        }
    }

    override suspend fun addWaveForm(waveItems: List<WaveItem>) {
        TODO("Not yet implemented")
    }

    private suspend fun loadFromAssets() {
        val items = context.assets.list(WAVEFORMS_DIRECTORY)?.map { name ->
            AudioWaveForm(
                name,
                isFromAssets = true
            )
        } ?: listOf()
        itemsFromAssets.emit(items)
        return suspendCoroutine {
            it.resume(Unit)
        }
    }

    private companion object {
        const val WAVEFORMS_DIRECTORY = "waveforms"
    }
}
