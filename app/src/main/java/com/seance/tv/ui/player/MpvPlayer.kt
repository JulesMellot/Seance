package com.seance.tv.ui.player

import android.content.Context
import android.view.SurfaceView
import `is`.xyz.mpv.MPVLib

class MpvPlayer(private val context: Context) {

    val surfaceView: SurfaceView = SurfaceView(context)

    init {
        MPVLib.create(context)
        MPVLib.setOptionString("vo", "gpu")
        MPVLib.setOptionString("ao", "audiotrack")
        MPVLib.setOptionString("hwdec", "mediacodec-copy")
        MPVLib.setOptionString("hwdec-codecs", "h264,hevc,vp8,vp9,av1")
        MPVLib.setOptionString("network-timeout", "10")
        MPVLib.init()
        MPVLib.attachSurface(surfaceView.holder.surface)
    }

    fun play(url: String, startPositionMs: Long = 0L) {
        MPVLib.command(arrayOf("loadfile", url))
        if (startPositionMs > 0) {
            val seconds = startPositionMs / 1000.0
            MPVLib.setOptionString("start", seconds.toString())
        }
    }

    fun togglePause() {
        val paused = MPVLib.getPropertyBoolean("pause")
        MPVLib.setPropertyBoolean("pause", !paused)
    }

    fun seekRelative(offsetMs: Long) {
        val seconds = offsetMs / 1000.0
        MPVLib.command(arrayOf("seek", seconds.toString(), "relative"))
    }

    fun seekAbsolute(positionMs: Long) {
        val seconds = positionMs / 1000.0
        MPVLib.command(arrayOf("seek", seconds.toString(), "absolute"))
    }

    fun getPositionMs(): Long {
        return (MPVLib.getPropertyDouble("time-pos") * 1000).toLong()
    }

    fun getDurationMs(): Long {
        return (MPVLib.getPropertyDouble("duration") * 1000).toLong()
    }

    fun release() {
        MPVLib.destroy()
    }
}
