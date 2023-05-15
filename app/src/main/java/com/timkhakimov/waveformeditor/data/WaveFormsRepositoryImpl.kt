package com.timkhakimov.waveformeditor.data

import android.content.res.AssetManager
import com.timkhakimov.waveformeditor.model.AudioWaveForm
import com.timkhakimov.waveformeditor.model.WaveItem
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class WaveFormsRepositoryImpl(
    private val assets: AssetManager,
    private val downloadsDir: File,
) : WaveFormsRepository {

    private val itemsFromAssets = MutableStateFlow<List<AudioWaveForm>>(listOf())

    override suspend fun getWaveForms(): List<AudioWaveForm> {
        val assetsItems = if (itemsFromAssets.value.isEmpty()) {
            loadFromAssets()
        } else {
            itemsFromAssets.value
        }
        val downloadItems = loadFromDownloads()
        return assetsItems + downloadItems
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
        return assets.open(fullPath).reader().getWaveItems()
    }

    private suspend fun getWavesFromDownload(name: String): List<WaveItem> {
        val fullPath = downloadsDir.path + File.separator + WAVEFORMS_DIRECTORY + File.separator + name
        val file = File(fullPath)
        return file.inputStream().reader().getWaveItems()
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
        val fileName = SLICE_NAME_PREFIX + System.currentTimeMillis() + FILE_EXTENSION
        val fullPath = downloadsDir.path + File.separator + WAVEFORMS_DIRECTORY + File.separator + fileName
        val file = File(fullPath)
        if (file.exists()) {
            file.delete()
        }
        file.createNewFile()
        val builder = StringBuilder()
        waveItems.forEach {
            builder.append(it.bottom)
            builder.append(' ')
            builder.append(it.top)
            builder.append("\n")
        }
        val outputStreamWriter: OutputStreamWriter = file.outputStream().writer()
        outputStreamWriter.write(builder.toString().trim())
        outputStreamWriter.flush()
        outputStreamWriter.close()
    }

    private suspend fun loadFromAssets(): List<AudioWaveForm> {
        val items = assets.list(WAVEFORMS_DIRECTORY)?.map { name ->
            AudioWaveForm(
                name,
                isFromAssets = true
            )
        } ?: listOf()
        itemsFromAssets.emit(items)
        return items
    }

    private suspend fun loadFromDownloads(): List<AudioWaveForm> {
        val waveformsDir = File(downloadsDir.path + File.separator + WAVEFORMS_DIRECTORY)
        if (!waveformsDir.exists()) {
            waveformsDir.mkdir()
        }
        val items = waveformsDir.list()?.map { name ->
            AudioWaveForm(
                name,
                isFromAssets = false
            )
        } ?: listOf()
        return items
    }

    private companion object {
        const val WAVEFORMS_DIRECTORY = "waveforms"
        const val SLICE_NAME_PREFIX = "Slice_"
        const val FILE_EXTENSION = ".wf"
    }
}
