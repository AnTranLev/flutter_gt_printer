package com.app.gt_printer

import android.content.Context
import com.zebra.scannercontrol.DCSSDKDefs
import com.zebra.scannercontrol.DCSScannerInfo
import com.zebra.scannercontrol.SDKHandler
import com.zebra.scannercontrol.IDcsSdkApiDelegate
import com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_ACTION_HIGH_HIGH_LOW_LOW_BEEP


class GTScannerHandler private constructor(context: Context) {

    private var currentScanner: DCSScannerInfo? = null
    private var sdkHandler: SDKHandler? = null

    init {
        try {
            sdkHandler = SDKHandler(context.applicationContext, true)
            initializeDcsSdk()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initializeDcsSdk() {
        if (sdkHandler == null) return

        // start sdk scanner
        sdkHandler!!.dcssdkEnableAvailableScannersDetection(true)
        // Not enabling BLE mode as it's not configured and can throw an exception
        // sdkHandler!!.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_BT_NORMAL)
        // sdkHandler!!.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_BT_LE)

        sdkHandler!!.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_SNAPI)
        sdkHandler!!.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_USB_CDC)

        val scannerList = ArrayList<DCSScannerInfo>()
        sdkHandler!!.dcssdkGetAvailableScannersList(scannerList)

        // get list of available scanners
        if (scannerList.isNotEmpty()) {
            currentScanner = scannerList[0]
            sdkHandler!!.dcssdkEstablishCommunicationSession(currentScanner!!.scannerID)
            sdkHandler!!.dcssdkEnableAutomaticSessionReestablishment(true, currentScanner!!.scannerID)
            beepScanner()
            aimOff()
            vibrateScanner()
        }
    }

    // Method to provide the global point of access to the singleton instance
    companion object {
        @Volatile
        private var instance: GTScannerHandler? = null

        fun getInstance(context: Context): GTScannerHandler =
            instance ?: synchronized(this) {
                instance ?: GTScannerHandler(context).also { instance = it }
            }
    }

    // Method to get the SDKHandler instance
    fun getSdkHandler(): SDKHandler? = sdkHandler

    // Method to set the delegate
    fun setDelegate(delegate: IDcsSdkApiDelegate?) {
        sdkHandler?.dcssdkSetDelegate(delegate)
    }

    fun executeCommand(opCode: DCSSDKDefs.DCSSDK_COMMAND_OPCODE, inXML: String): Boolean {
        if (sdkHandler != null && currentScanner != null) {
            val outXML = StringBuilder()
            val result = sdkHandler!!.dcssdkExecuteCommandOpCodeInXMLForScanner(opCode, inXML, outXML, currentScanner!!.scannerID)
            return result == DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_SUCCESS
        }
        return false
    }

    fun pullTrigger(): Boolean {
        return currentScanner?.let {
            val inXml = "<inArgs><scannerID>${it.scannerID}</scannerID></inArgs>"
            executeCommand(DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_DEVICE_PULL_TRIGGER, inXml)
        } ?: false
    }

    fun releaseTrigger(): Boolean {
        return currentScanner?.let {
            val inXml = "<inArgs><scannerID>${it.scannerID}</scannerID></inArgs>"
            executeCommand(DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_DEVICE_PULL_TRIGGER, inXml)
        } ?: false
    }

    fun enableScanning() {
        currentScanner?.let {
            val inXml = "<inArgs><scannerID>${it.scannerID}</scannerID></inArgs>"
            executeCommand(DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_DEVICE_SCAN_ENABLE, inXml)
        }
    }

    fun disableScanning() {
        currentScanner?.let {
            val inXml = "<inArgs><scannerID>${it.scannerID}</scannerID></inArgs>"
            executeCommand(DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_DEVICE_SCAN_DISABLE, inXml)
        }
    }

    fun aimOn() {
        currentScanner?.let {
            val inXml = "<inArgs><scannerID>${it.scannerID}</scannerID></inArgs>"
            executeCommand(DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_DEVICE_AIM_ON, inXml)
        }
    }

    fun aimOff() {
        currentScanner?.let {
            val inXml = "<inArgs><scannerID>${it.scannerID}</scannerID></inArgs>"
            executeCommand(DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_DEVICE_AIM_OFF, inXml)
        }
    }

    private fun turnOnLEDPattern() {
        currentScanner?.let {
            val inXml = "<inArgs><scannerID>${it.scannerID}</scannerID><cmdArgs><arg-int>88</arg-int></cmdArgs></inArgs>"
            executeCommand(DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_SET_ACTION, inXml)
        }
    }

    private fun turnOffLEDPattern() {
        currentScanner?.let {
            val inXml = "<inArgs><scannerID>${it.scannerID}</scannerID><cmdArgs><arg-int>90</arg-int></cmdArgs></inArgs>"
            executeCommand(DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_SET_ACTION, inXml)
        }
    }

    private fun vibrateScanner() {
        currentScanner?.let {
            val inXml = "<inArgs><scannerID>${it.scannerID}</scannerID><cmdArgs>"
            executeCommand(DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_DEVICE_VIBRATION_FEEDBACK, inXml)
        }
    }

    private fun beepScanner() {
        currentScanner?.let {
            val inXml = "<inArgs><scannerID>${it.scannerID}</scannerID><cmdArgs><arg-int>${RMD_ATTR_VALUE_ACTION_HIGH_HIGH_LOW_LOW_BEEP}</arg-int></cmdArgs></inArgs>"
            executeCommand(DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_SET_ACTION, inXml)
        }
    }
}
