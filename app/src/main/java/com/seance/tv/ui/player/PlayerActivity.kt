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
    }

    private val viewModel: PlayerViewModel by viewModels()
    private lateinit var mpvPlayer: MpvPlayer

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

        mpvPlayer = MpvPlayer(this)

        viewModel.initialize(ratingKey, title, streamUrl, partKey, durationMs, viewOffsetMs)

        setContent {
            SeanceTheme {
                val state by viewModel.uiState.collectAsState()
                PlayerScreen(
                    state = state,
                    mpvPlayer = mpvPlayer,
                    onPlayPause = viewModel::togglePlayPause,
                    onSeek = viewModel::seek,
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
                viewModel.seek(10_000)
                true
            }
            KeyEvent.KEYCODE_MEDIA_REWIND,
            KeyEvent.KEYCODE_DPAD_LEFT -> {
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
        mpvPlayer.release()
        super.onDestroy()
    }
}
