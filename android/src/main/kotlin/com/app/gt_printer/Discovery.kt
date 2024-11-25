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
    fun toMap(): Map<String, Any?> // Thêm phương thức này

}

class PrinterInfo(
    var ipAddress: String? = null,
    var bdAddress: String? = null,
    var macAddress: String? = null,
    var model: String? = null,
    var type: String? = null,
    var printType: String? = null,
    var target: String? =null
) : JSONConvertible {
    override fun toMap(): Map<String, Any?> {
        return mapOf(
            "ipAddress" to ipAddress,
            "bdAddress" to bdAddress,
            "macAddress" to macAddress,
            "model" to model,
            "type" to type,
            "printType" to printType,
            "target" to target
        )    }
}

data class PrinterResult(
    var type: String,
    var success: Boolean,
    var message: String? = null,
    var content: Any? = null
) : JSONConvertible {
    override fun toMap(): Map<String, Any?> {
        return mapOf(
            "type" to type,
            "success" to success,
            "message" to message,
            "content" to content
        )
    }
}