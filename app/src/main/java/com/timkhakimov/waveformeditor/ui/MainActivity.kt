package com.timkhakimov.waveformeditor.ui

import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.timkhakimov.waveformeditor.R
import com.timkhakimov.waveformeditor.data.WaveFormsRepositoryImpl
import com.timkhakimov.waveformeditor.databinding.ActivityMainBinding
import com.timkhakimov.waveformeditor.model.AudioWaveForm
import com.timkhakimov.waveformeditor.model.WaveItem
import com.timkhakimov.waveformeditor.presentation.WaveFormsViewModel
import com.timkhakimov.waveformeditor.ui.adapter.WaveFormsAdapter
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val viewModel: WaveFormsViewModel by lazy {
        ViewModelProvider(this)[WaveFormsViewModel::class.java].apply {
                repository = WaveFormsRepositoryImpl(
                    assets,
                    getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!
                )
            }
    }

    private val waveFormsAdapter: WaveFormsAdapter by lazy {
        WaveFormsAdapter {
            viewModel.startAudioForm(it)
        }
    }

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initList()
        observeData()
        initListeners()
        viewModel.loadWaveForms()
    }

    private fun initListeners() = with(binding) {
        exportButtonTextView.setOnClickListener {
            viewModel.addWaveForm(waveFormEditorView.exportSlice())
        }
    }

    private fun initList() {
        with(binding.waveFormsRecyclerView) {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = waveFormsAdapter
        }
    }

    private fun observeData() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.waveFormsList.collect {
                    waveFormsAdapter.setWaveForms(it)
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectedWaveForm.collect {
                    setAudioForm(it)
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.waves.collect {
                    setWaves(it)
                }
            }
        }
    }

    private fun setAudioForm(audioWaveForm: AudioWaveForm?) {
        audioWaveForm?.let {
            binding.selectedWaveFormNameTextView.text = it.name
        } ?: run {
            binding.selectedWaveFormNameTextView.setText(R.string.select_file)
        }
    }

    private fun setWaves(waves: List<WaveItem>) = with(binding) {
        waveFormEditorView.setWaves(waves)
        exportButtonTextView.isVisible = waves.size > MIN_SIZE_FOR_EXPORT
    }

    private companion object {
        const val MIN_SIZE_FOR_EXPORT = 3
    }
}
