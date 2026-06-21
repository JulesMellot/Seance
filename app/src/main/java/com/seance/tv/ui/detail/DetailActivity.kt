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
    }

    private val viewModel: DetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ratingKey = intent.getStringExtra(EXTRA_RATING_KEY) ?: run {
            finish()
            return
        }
        viewModel.load(ratingKey)

        setContent {
            SeanceTheme {
                val state by viewModel.uiState.collectAsState()
                DetailScreen(
                    state = state,
                    onPlay = { item ->
                        startActivity(
                            Intent(this, PlayerActivity::class.java).apply {
                                putExtra(PlayerActivity.EXTRA_RATING_KEY, item.ratingKey)
                                putExtra(PlayerActivity.EXTRA_TITLE, item.title)
                                item.media.firstOrNull()?.parts?.firstOrNull()?.let { part ->
                                    putExtra(PlayerActivity.EXTRA_PART_KEY, part.key)
                                    part.duration?.let { d -> putExtra(PlayerActivity.EXTRA_DURATION, d) }
                                }
                                item.viewOffset?.let { offset -> putExtra(PlayerActivity.EXTRA_VIEW_OFFSET, offset) }
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
