package com.seance.tv.ui.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

/**
 * Lecteur vidéo basé sur Media3 ExoPlayer.
 * Direct Play des codecs supportés par l'appareil (H.264 / HEVC / VP9 / AV1).
 */
class VideoPlayer(context: Context) {

    val playerView: PlayerView = PlayerView(context).apply {
        useController = false
    }

    private val player: ExoPlayer = ExoPlayer.Builder(context).build()

    init {
        playerView.player = player
    }

    fun play(url: String, startPositionMs: Long = 0L) {
        val mediaItem = MediaItem.fromUri(url)
        player.setMediaItem(mediaItem, startPositionMs.coerceAtLeast(0L))
        player.prepare()
        player.playWhenReady = true
    }

    fun togglePause() {
        player.playWhenReady = !player.playWhenReady
    }

    fun setPlaying(playing: Boolean) {
        player.playWhenReady = playing
    }

    fun seekRelative(offsetMs: Long) {
        val target = (player.currentPosition + offsetMs).coerceAtLeast(0L)
        player.seekTo(target)
    }

    fun seekAbsolute(positionMs: Long) {
        player.seekTo(positionMs.coerceAtLeast(0L))
    }

    fun getPositionMs(): Long = player.currentPosition

    fun getDurationMs(): Long = player.duration.coerceAtLeast(0L)

    fun release() {
        player.release()
    }
}
