package com.app.gt_printer

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.zebra.barcode.sdk.sms.ConfigurationUpdateEvent
import com.zebra.scannercontrol.DCSSDKDefs
import com.zebra.scannercontrol.DCSScannerInfo
import com.zebra.scannercontrol.FirmwareUpdateEvent
import com.zebra.scannercontrol.IDcsSdkApiDelegate
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.Result

class GtScannerPlugin(context: Context) :
    IDcsSdkApiDelegate,
    MethodChannel.MethodCallHandler {

    private var eventCallback: Result? =
        null // Store the callback
    private val appContext: Context?
        private get() = context?.applicationContext
    private var context: Context

    init {
        this.context = context
        val mainHandler =
            Handler(Looper.getMainLooper())

        mainHandler.post {
            if (this.context != null) {
                GTScannerHandler.getInstance(this.context)
            }
        }
    }

    val name: String
        get() = "GtScannerPlugin"

    override fun onMethodCall(
        call: MethodCall,
        rawResult: Result
    ) {
        val result = MethodResultWrapper(rawResult)
        Thread(MethodRunner(call, result)).start()
    }

    override fun dcssdkEventScannerAppeared(
        dcsScannerInfo: DCSScannerInfo
    ) {
        Log.i(TAG, "dcssdkEventScannerAppeared")
    }

    override fun dcssdkEventScannerDisappeared(i: Int) {}
    override fun dcssdkEventCommunicationSessionEstablished(
        dcsScannerInfo: DCSScannerInfo
    ) {
        Log.i(
            TAG,
            "dcssdkEventCommunicationSessionEstablished"
        )
    }

    override fun dcssdkEventCommunicationSessionTerminated(
        i: Int
    ) {
    }

    override fun dcssdkEventBarcode(
        bytes: ByteArray,
        i: Int,
        i1: Int
    ) {
        val barcode = String(bytes)
        Log.i(TAG, barcode)

        // Call the callback if it's registered
        eventCallback?.success(barcode)
    }

    override fun dcssdkEventImage(
        bytes: ByteArray,
        i: Int
    ) {
    }

    override fun dcssdkEventVideo(
        bytes: ByteArray,
        i: Int
    ) {
    }

    override fun dcssdkEventBinaryData(
        bytes: ByteArray,
        i: Int
    ) {
    }

    override fun dcssdkEventFirmwareUpdate(
        firmwareUpdateEvent: FirmwareUpdateEvent
    ) {
    }

    override fun dcssdkEventAuxScannerAppeared(
        dcsScannerInfo: DCSScannerInfo,
        dcsScannerInfo1: DCSScannerInfo
    ) {
    }

    override fun dcssdkEventConfigurationUpdate(
        configurationUpdateEvent: ConfigurationUpdateEvent
    ) {
    }

    // call to active sdk
    fun connectScanner(scannerCallback: Result?) {
        GTScannerHandler.getInstance(appContext!!).setDelegate(this)
        val sdkHandler = GTScannerHandler.getInstance(appContext!!).getSdkHandler()
        sdkHandler!!.dcssdkSubsribeForEvents(DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_BARCODE.value)

        scannerCallback?.success(true);
    }

    // call manual on scan
    fun startScan(scannerCallback: Result?) {
        val handler =
            GTScannerHandler.getInstance(
                appContext!!
            )
        val status = handler.pullTrigger()
        if (status) {
            eventCallback = scannerCallback // Store the callback
        }
    }

    // call manual off scan
    fun offScan() {
        val handler =
            GTScannerHandler.getInstance(
                appContext!!
            )
        val status = handler.releaseTrigger()
        eventCallback = null
    }

    // call enable scan
    fun enableScanner(scannerCallback: Result?) {
        val handler =
            GTScannerHandler.getInstance(
                appContext!!
            )
        handler.enableScanning()
    }

    // call disable scan
    fun disableScanner(scannerCallback: Result?) {
        val handler =
            GTScannerHandler.getInstance(
                appContext!!
            )
        handler.disableScanning()
    }

    companion object {
        private const val TAG =
            "GtScannerPlugin" // Define a tag for logging
    }

    inner class MethodRunner(
        call: MethodCall,
        result: Result
    ) : Runnable {
        private val call: MethodCall = call
        private val result: Result = result

        override fun run() {
            Log.d(
                TAG,
                "Method Called: ${call.method}"
            )
            when (call.method) {
                "getPluginStatus" -> {
                    result.success("Connected with Android ${android.os.Build.VERSION.RELEASE}")
                }

                "connectScanner" -> {
                    connectScanner(result)
                }

                "offScan" -> {
                    offScan()
                }

                "startScan" -> {
                    startScan(result)
                }

                "enableScanner" -> {
                    enableScanner(result)
                }

                "disableScanner" -> {
                    disableScanner(result)
                }

                else -> {
                    Log.d(
                        TAG,
                        "Method: ${call.method} is not supported yet"
                    )
                    result.notImplemented()
                }
            }
        }
    }
}

