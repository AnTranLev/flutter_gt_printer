package com.app.gt_printer

import com.google.gson.Gson

class Discovery {
    companion object {
        const val PORTTYPE_TCP = "TCP"
        const val PORTTYPE_BLUETOOTH = "BT"
        const val PORTTYPE_USB = "USB"
    }
}

interface JSONConvertible {
    fun toJSON(): String = Gson().toJson(this)
}

class PrinterInfo(
    var ipAddress: String? = null,
    var bdAddress: String? = null,
    var macAddress: String? = null,
    var model: String? = null,
    var type: String? = null,
    var printType: String? = null,
    var target: String? =null
) : JSONConvertible

data class PrinterResult(
    var type: String,
    var success: Boolean,
    var message: String? = null,
    var content: Any? = null
) : JSONConvertible