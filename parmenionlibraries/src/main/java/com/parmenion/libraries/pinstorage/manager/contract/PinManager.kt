package com.parmenion.libraries.pinstorage.manager.contract

interface PinManager {
    fun hasPinSet(): Boolean
    fun doesPinMatch(pin: String): Boolean
    fun setPin(pin: String)
    fun clearPin()
}