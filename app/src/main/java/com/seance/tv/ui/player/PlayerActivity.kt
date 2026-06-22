package com.seance.tv.ui.player

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.seance.tv.ui.theme.SeanceTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlayerActivity : ComponentActivity() {

    companion object {
        const val EXTRA_RATING_KEY = "rating_key"
        const val EXTRA_TITLE = "title"
        const val EXTRA_PART_KEY = "part_key"
        const val EXTRA_STREAM_URL = "stream_url"
        const val EXTRA_DURATION = "duration_ms"
        const val EXTRA_VIEW_OFFSET = "view_offset_ms"
        const val EXTRA_AUDIO_LANG = "audio_lang"
        const val EXTRA_SUBTITLE_LANG = "subtitle_lang"
        const val EXTRA_SUBS_OFF = "subs_off"
    }

    private val viewModel: PlayerViewModel by viewModels()
    private lateinit var videoPlayer: VideoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            )

        val ratingKey = intent.getStringExtra(EXTRA_RATING_KEY) ?: run { finish(); return }
        val title = intent.getStringExtra(EXTRA_TITLE) ?: ""
        val streamUrl = intent.getStringExtra(EXTRA_STREAM_URL) ?: ""
        val partKey = intent.getStringExtra(EXTRA_PART_KEY)
        val durationMs = intent.getLongExtra(EXTRA_DURATION, 0L)
        val viewOffsetMs = intent.getLongExtra(EXTRA_VIEW_OFFSET, 0L)
        val audioLang = intent.getStringExtra(EXTRA_AUDIO_LANG)
        val subtitleLang = intent.getStringExtra(EXTRA_SUBTITLE_LANG)
        val subsOff = intent.getBooleanExtra(EXTRA_SUBS_OFF, false)

        videoPlayer = VideoPlayer(this)

        viewModel.initialize(
            ratingKey, title, streamUrl, partKey, durationMs, viewOffsetMs,
            audioLanguage = audioLang,
            subtitleLanguage = subtitleLang,
            subtitlesDisabled = subsOff
        )

        setContent {
            SeanceTheme {
                val state by viewModel.uiState.collectAsState()
                PlayerScreen(
                    state = state,
                    videoPlayer = videoPlayer,
                    onPlayPause = viewModel::togglePlayPause,
                    onSeek = viewModel::seek,
                    onProgress = viewModel::onProgress,
                    onBack = { finish() }
                )
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
            KeyEvent.KEYCODE_DPAD_CENTER -> {
                viewModel.togglePlayPause()
                true
            }
            KeyEvent.KEYCODE_MEDIA_FAST_FORWARD,
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                videoPlayer.seekRelative(10_000)
                viewModel.seek(10_000)
                true
            }
            KeyEvent.KEYCODE_MEDIA_REWIND,
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                videoPlayer.seekRelative(-10_000)
                viewModel.seek(-10_000)
                true
            }
            KeyEvent.KEYCODE_BACK -> {
                finish()
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.saveProgress()
    }

    override fun onDestroy() {
        videoPlayer.release()
        super.onDestroy()
    }
}
