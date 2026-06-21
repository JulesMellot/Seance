package com.seance.tv.ui.player

import android.content.Context
import android.os.Build
import android.view.SurfaceView
import `is`.xyz.mpv.MPVLib

class MpvPlayer(private val context: Context) {

    val surfaceView: SurfaceView = SurfaceView(context)
    private val mpv: MPVLib = MPVLib.create(context)

    init {
        mpv.setOptionString("vo", "gpu")
        mpv.setOptionString("ao", "audiotrack")

        // Pas de décodage hardware sur émulateur
        val isEmulator = (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK"))
        if (isEmulator) {
            mpv.setOptionString("hwdec", "no")
        } else {
            mpv.setOptionString("hwdec", "mediacodec-copy")
            mpv.setOptionString("hwdec-codecs", "h264,hevc,vp8,vp9,av1")
        }

        mpv.setOptionString("network-timeout", "10")
        mpv.init()
        mpv.attachSurface(surfaceView.holder.surface)
    }

    fun play(url: String, startPositionMs: Long = 0L) {
        mpv.command(arrayOf("loadfile", url))
        if (startPositionMs > 0) {
            mpv.setOptionString("start", (startPositionMs / 1000.0).toString())
        }
    }

    fun togglePause() {
        val paused = mpv.getPropertyBoolean("pause")
        mpv.setPropertyBoolean("pause", !paused)
    }

    fun seekRelative(offsetMs: Long) {
        mpv.command(arrayOf("seek", (offsetMs / 1000.0).toString(), "relative"))
    }

    fun seekAbsolute(positionMs: Long) {
        mpv.command(arrayOf("seek", (positionMs / 1000.0).toString(), "absolute"))
    }

    fun getPositionMs(): Long =
        (mpv.getPropertyDouble("time-pos") * 1000).toLong()

    fun getDurationMs(): Long =
        (mpv.getPropertyDouble("duration") * 1000).toLong()

    fun release() {
        mpv.destroy()
    }
}
