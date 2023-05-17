package com.timkhakimov.waveformeditor.presentation

import com.timkhakimov.waveformeditor.MainTestDispatcherRule
import com.timkhakimov.waveformeditor.data.WaveFormsRepository
import com.timkhakimov.waveformeditor.model.AudioWaveForm
import com.timkhakimov.waveformeditor.model.WaveItem
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*

@ExperimentalCoroutinesApi
class WaveFormsViewModelTest {

    @get:Rule
    val coroutineTestRule = MainTestDispatcherRule()

    private val waveFormsRepository: WaveFormsRepository = mock(WaveFormsRepository::class.java)

    private val viewModel: WaveFormsViewModel = WaveFormsViewModel().apply {
        repository = waveFormsRepository
    }

    @Test
    fun `addWaveForm should add waveForm to repository`() = runTest {
        val waveItems = WAVE_ITEMS_LIST

        viewModel.addWaveForm(waveItems)

        advanceUntilIdle()

        verify(waveFormsRepository).addWaveForm(waveItems)
    }

    @Test
    fun `startAudioForm should update selectedWaveForm and waves`() = runTest {
        val audioWaveForm: AudioWaveForm = AUDIO_WAVE_FORM_1
        val waveItems = WAVE_ITEMS_LIST
        `when`(waveFormsRepository.getWaveItemsFromForm(audioWaveForm)).thenReturn(waveItems)

        viewModel.startAudioForm(audioWaveForm)

        advanceUntilIdle()

        assertEquals(audioWaveForm, viewModel.selectedWaveForm.first())
        assertEquals(waveItems, viewModel.waves.first())
    }

    @Test
    fun `loadWaveForms should update waveFormsList`() = runTest {
        val waveForms = listOf(AUDIO_WAVE_FORM_1, AUDIO_WAVE_FORM_2)
        `when`(waveFormsRepository.getWaveForms()).thenReturn(waveForms)

        viewModel.loadWaveForms()

        advanceUntilIdle()

        assertEquals(waveForms, viewModel.waveFormsList.first())
    }

    private companion object {
        val AUDIO_WAVE_FORM_1 = AudioWaveForm("first", true)
        val AUDIO_WAVE_FORM_2 = AudioWaveForm("second", true)
        val WAVE_ITEMS_LIST = listOf<WaveItem>(
            WaveItem(0.1, 0.5),
            WaveItem(0.2, 0.6),
            WaveItem(0.15, 0.55)
        )
    }
}