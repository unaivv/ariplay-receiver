package com.example.airplayreceiverunai

object AirPlayBridge {
    init {
        System.loadLibrary("airplay_jni")
    }

    external fun nativeStart(port: Int, name: String, hwAddr: String, callback: AirPlayCallback): Boolean
    external fun nativeStop()
    external fun nativeGetPort(): Int
}
