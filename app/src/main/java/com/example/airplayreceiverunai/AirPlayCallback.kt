package com.example.airplayreceiverunai

interface AirPlayCallback {
    fun onVideoData(data: ByteArray, isH265: Boolean)
    fun onConnected()
    fun onDisconnected()
}
