package com.app.gt_printer

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.caysn.autoreplyprint.AutoReplyPrint
import com.sun.jna.Pointer
import com.sun.jna.ptr.LongByReference
import io.flutter.plugin.common.MethodChannel.Result
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException


class GTPrinterBridgeModule(private val context: Context) {
    private val TAG = "GTPrinterBridgeModule"

    private fun getAppContext(): Context? {
        return context?.applicationContext
    }

    fun getName(): String {
        return "GTPrinterBridgeModule"
    }

    fun portDiscovery(
        type: String, promise: Result
    ) {
        try {
            var h: Pointer = Pointer.NULL
            var port: String? = null

            val listUsbPort =
                AutoReplyPrint.CP_Port_EnumUsb_Helper.EnumUsb()
            listUsbPort?.let {
                for (usbPort in it) {
                    h = AutoReplyPrint.INSTANCE.CP_Port_OpenUsb(usbPort, 1)
                    if (h != Pointer.NULL) {
                        port = usbPort
                        break
                    }
                }
            }

            promise.success(port)
            AutoReplyPrint.INSTANCE.CP_Port_Close(h)
        } catch (e: Exception) {
            promise.error("PortDiscoveryError", "An error occurred during port discovery", e)
        }
    }

    fun connect(type: String?) {
//        val activity: Activity = getCurrentReactActivity()
    }

    fun disconnect(type: String?) {
//        val activity: Activity = getCurrentReactActivity()
    }

    fun checkStatus(port: String?) {
//        val activity: Activity = getCurrentReactActivity()
    }

    fun openCashDrawer(
        port: String, promise: Result
    ) {
        try {

            val h = openPort(port)
            if (h != Pointer.NULL) {
                AutoReplyPrint.INSTANCE.CP_Pos_KickOutDrawer(
                    h, 0, 100, 100
                )
                AutoReplyPrint.INSTANCE.CP_Pos_KickOutDrawer(
                    h, 1, 100, 100
                )
                promise.success("Success")
                AutoReplyPrint.INSTANCE.CP_Port_Close(
                    h
                )
            } else {
                promise.success("Print failed! Cannot open Port: $port")
            }
        } catch (e: Exception) {
            promise.error(
                "ERROR", e.toString(), e
            )
        }
    }

    fun print(
        port: String,
        commands: Map<String, Any>,
        promise: Result
    ) {
        try {
            val imageUrl =
                commands["imageUrl"] as? String
            val kickDrawer =
                commands["kickDrawer"] as? Boolean

            if (imageUrl != null) {
                val h = openPort(port)
                if (h != Pointer.NULL) {
                    val bitmap =
                        convertFileUriToBitmap(
                            imageUrl
                        )
                    if (bitmap != null) {
                        val printResult =
                            AutoReplyPrint.CP_Pos_PrintRasterImageFromData_Helper.PrintRasterImageFromBitmap(
                                    h,
                                    bitmap.width,
                                    bitmap.height,
                                    bitmap,
                                    AutoReplyPrint.CP_ImageBinarizationMethod_Thresholding,
                                    AutoReplyPrint.CP_ImageCompressionMethod_None
                                )

                        if (kickDrawer == true) {
                            AutoReplyPrint.INSTANCE.CP_Pos_KickOutDrawer(
                                h, 0, 100, 100
                            )
                            AutoReplyPrint.INSTANCE.CP_Pos_KickOutDrawer(
                                h, 1, 100, 100
                            )
                        }

                        AutoReplyPrint.INSTANCE.CP_Pos_FeedAndHalfCutPaper(h)
                        val resultMessage =
                            queryPrintResultMessage(
                                h
                            )
                        promise.success(
                            resultMessage
                        )
                    } else {
                        promise.success("Print failed! Cannot convert image URL to bitmap")
                    }
                    AutoReplyPrint.INSTANCE.CP_Port_Close(
                        h
                    )
                } else {
                    promise.success("Print failed! Cannot open Port: $port")
                }
            } else {
                promise.success("Print failed! Wrong commands, not found with field imageUrl.")
            }
        } catch (e: Exception) {
            promise.error(
                "ERROR", e.toString(), e
            )
        }
    }

    fun printMultiples(
        port: String,
        commands: Map<String, Any>,
        promise: Result
    ) {
        try {
            val imageUrlArray =
                commands["imageUrls"] as? ArrayList<String>
            if (imageUrlArray != null) {
                val h = openPort(port)
                if (h != Pointer.NULL) {
                    for (i in 0 until imageUrlArray.size) {
                        val imageUrl =
                            imageUrlArray[i]
                        val bitmap =
                            convertFileUriToBitmap(
                                imageUrl
                            )
                        if (bitmap != null) {
                            val printResult =
                                AutoReplyPrint.CP_Pos_PrintRasterImageFromData_Helper.PrintRasterImageFromBitmap(
                                        h,
                                        bitmap.width,
                                        bitmap.height,
                                        bitmap,
                                        AutoReplyPrint.CP_ImageBinarizationMethod_Thresholding,
                                        AutoReplyPrint.CP_ImageCompressionMethod_None
                                    )

                            AutoReplyPrint.INSTANCE.CP_Pos_FeedAndHalfCutPaper(
                                h
                            )
                            if (!printResult) {
                                val resultMessage =
                                    queryPrintResultMessage(
                                        h
                                    )
                                promise.success(
                                    resultMessage
                                )
                                break
                            }
                        }
                    }
                    AutoReplyPrint.INSTANCE.CP_Port_Close(
                        h
                    )
                } else {
                    promise.success("Print failed! Cannot open Port: $port")
                }
            } else {
                promise.success("Print failed! Wrong commands, not found with field imageUrls.")
            }
        } catch (e: Exception) {
            promise.error(
                "ERROR", e.toString(), e
            )
        }
    }

    private fun convertFileUriToBitmap(fileUri: String): Bitmap? {
        var bitmap: Bitmap? = null
        var fileInputStream: FileInputStream? =
            null
        try {
            val uri = Uri.parse(fileUri)
            val file = File(uri.path)
            fileInputStream =
                FileInputStream(file)
            bitmap = BitmapFactory.decodeStream(
                fileInputStream
            )
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } finally {
            fileInputStream?.let {
                try {
                    it.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return bitmap
    }

    private fun openPort(usbPort: String): Pointer {
        return AutoReplyPrint.INSTANCE.CP_Port_OpenUsb(
            usbPort, 1
        )
    }

    private fun queryPrintResultMessage(h: Pointer): String {
        val result =
            AutoReplyPrint.INSTANCE.CP_Pos_QueryPrintResult(
                h, 0, 30000
            )
        if (!result) {
            val printerErrorStatus =
                LongByReference()
            val printerInfoStatus =
                LongByReference()
            val timestampMsPrinterStatus =
                LongByReference()
            return if (AutoReplyPrint.INSTANCE.CP_Printer_GetPrinterStatusInfo(
                    h,
                    printerErrorStatus,
                    printerInfoStatus,
                    timestampMsPrinterStatus
                )
            ) {
                var errorStatusString =
                    String.format(
                        "Printer Error Status: 0x%04X",
                        printerErrorStatus.value and 0xffff
                    )
                val status =
                    AutoReplyPrint.CP_PrinterStatus(
                        printerErrorStatus.value,
                        printerInfoStatus.value
                    )
                if (status.ERROR_OCCURED()) {
                    if (status.ERROR_CUTTER()) errorStatusString += "[ERROR_CUTTER]"
                    if (status.ERROR_FLASH()) errorStatusString += "[ERROR_FLASH]"
                    if (status.ERROR_NOPAPER()) errorStatusString += "[ERROR_NOPAPER]"
                    if (status.ERROR_VOLTAGE()) errorStatusString += "[ERROR_VOLTAGE]"
                    if (status.ERROR_MARKER()) errorStatusString += "[ERROR_MARKER]"
                    if (status.ERROR_ENGINE()) errorStatusString += "[ERROR_ENGINE]"
                    if (status.ERROR_OVERHEAT()) errorStatusString += "[ERROR_OVERHEAT]"
                    if (status.ERROR_COVERUP()) errorStatusString += "[ERROR_COVERUP]"
                    if (status.ERROR_MOTOR()) errorStatusString += "[ERROR_MOTOR]"
                }
                errorStatusString
            } else {
                "CP_Printer_GetPrinterStatusInfo Failed"
            }
        }
        return "Success"
    }
}