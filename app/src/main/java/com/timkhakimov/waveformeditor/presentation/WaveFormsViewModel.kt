package com.timkhakimov.waveformeditor.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timkhakimov.waveformeditor.data.WaveFormsRepository
import com.timkhakimov.waveformeditor.model.AudioWaveForm
import com.timkhakimov.waveformeditor.model.WaveItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WaveFormsViewModel : ViewModel() {

    lateinit var repository: WaveFormsRepository

    private val _waveFormsList = MutableStateFlow<List<AudioWaveForm>>(listOf())
    val waveFormsList = _waveFormsList.asStateFlow()

    private val _selectedWaveForm = MutableStateFlow<AudioWaveForm?>(null)
    val selectedWaveForm = _selectedWaveForm.asStateFlow()

    private val _waves = MutableStateFlow<List<WaveItem>>(listOf())
    val waves = _waves.asStateFlow()

    fun addWaveForm(waveItems: List<WaveItem>) {
        viewModelScope.launch {
            runCatching {
                repository.addWaveForm(waveItems)
            }.onSuccess {
                loadWaveForms()
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    fun loadWaveForms() {
        viewModelScope.launch {
            runCatching {
                repository.getWaveForms()
            }.onSuccess {
                _waveFormsList.emit(it)
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    fun startAudioForm(audioWaveForm: AudioWaveForm) {
        _selectedWaveForm.update { audioWaveForm }
        viewModelScope.launch {
            runCatching {
                repository.getWaveItemsFromForm(audioWaveForm)
            }.onSuccess {
                _waves.emit(it)
            }.onFailure {
               it.printStackTrace()
            }
        }
    }
}
