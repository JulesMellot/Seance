package com.seance.tv.ui.detail

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.seance.tv.ui.player.PlayerActivity
import com.seance.tv.ui.theme.SeanceTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailActivity : ComponentActivity() {

    companion object {
        const val EXTRA_RATING_KEY = "rating_key"
        const val EXTRA_SERVER_URL = "server_url"
        const val EXTRA_TOKEN = "token"
    }

    private val viewModel: DetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ratingKey = intent.getStringExtra(EXTRA_RATING_KEY) ?: run { finish(); return }
        val serverUrl = intent.getStringExtra(EXTRA_SERVER_URL) ?: ""
        val token = intent.getStringExtra(EXTRA_TOKEN) ?: ""

        viewModel.load(ratingKey, serverUrl, token)

        setContent {
            SeanceTheme {
                val state by viewModel.uiState.collectAsState()
                DetailScreen(
                    state = state,
                    imageUrl = viewModel::imageUrl,
                    onPlay = { item ->
                        val partKey = item.media.firstOrNull()?.parts?.firstOrNull()?.key
                        startActivity(
                            Intent(this, PlayerActivity::class.java).apply {
                                putExtra(PlayerActivity.EXTRA_RATING_KEY, item.ratingKey)
                                putExtra(PlayerActivity.EXTRA_TITLE, item.title)
                                partKey?.let {
                                    putExtra(PlayerActivity.EXTRA_PART_KEY, it)
                                    putExtra(PlayerActivity.EXTRA_STREAM_URL,
                                        "$serverUrl${it}?X-Plex-Token=$token")
                                }
                                item.duration?.let { putExtra(PlayerActivity.EXTRA_DURATION, it) }
                                item.viewOffset?.let { putExtra(PlayerActivity.EXTRA_VIEW_OFFSET, it) }
                            }
                        )
                    },
                    onSeasonSelected = viewModel::selectSeason,
                    onBack = { finish() }
                )
            }
        }
    }
}
