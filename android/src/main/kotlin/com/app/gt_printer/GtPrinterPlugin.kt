package com.app.gt_printer


import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.LocationManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import com.caysn.autoreplyprint.AutoReplyPrint
import com.caysn.autoreplyprint.AutoReplyPrint.CP_Label_BarcodeType_EAN13
import com.caysn.autoreplyprint.AutoReplyPrint.CP_OnBluetoothDeviceDiscovered_Callback
import com.caysn.autoreplyprint.AutoReplyPrint.CP_OnNetPrinterDiscovered_Callback
import com.sun.jna.Pointer
import com.sun.jna.WString
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.LongByReference
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** GtPrinterPlugin */
class GtPrinterPlugin: FlutterPlugin, MethodCallHandler,
  ActivityAware {

  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel: MethodChannel

  private val logTag = "GtPrinterPlugin"
  private lateinit var context: Context
  private lateinit var activity: Activity

  private var printers: ArrayList<PrinterInfo> =
    ArrayList()

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    activity = binding.activity
  }

  override fun onDetachedFromActivityForConfigChanges() {
  }

  override fun onReattachedToActivityForConfigChanges(
    binding: ActivityPluginBinding
  ) {
  }

  override fun onDetachedFromActivity() {
  }

  override fun onAttachedToEngine(
    flutterPluginBinding: FlutterPlugin.FlutterPluginBinding
  ) {
    channel = MethodChannel(
      flutterPluginBinding.binaryMessenger,
      "gt_printer"
    )
    channel.setMethodCallHandler(this)
    context =
      flutterPluginBinding.applicationContext
  }

  override fun onMethodCall(
    call: MethodCall,
    rawResult: Result
  ) {
    val result = MethodResultWrapper(rawResult)
    Thread(MethodRunner(call, result)).start()
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

  inner class MethodRunner(
    call: MethodCall,
    result: Result
  ) : Runnable {
    private val call: MethodCall = call
    private val result: Result = result

    override fun run() {
      Log.d(
        logTag,
        "Method Called: ${call.method}"
      )
      when (call.method) {
        "getPlatformVersion" -> {
          result.success("Android ${android.os.Build.VERSION.RELEASE}")
        }

        "onDiscovery" -> {
          onDiscovery(call, result)
        }

        "onPrint" -> {
          onPrint(call, result)
        }
//        "onGetPrinterInfo" -> {
//          onGetPrinterInfo(call, result)
//        }
//        "isPrinterConnected" -> {
//          isPrinterConnected(call, result)
//        }
//        "getPrinterSetting" -> {
//          getPrinterSetting(call, result)
//        }
//        "setPrinterSetting" -> {
//          setPrinterSetting(call, result)
//        }
//        "requestRuntimePermission" -> {
//          requestRuntimePermission(call, result)
//        }
        else -> {
          Log.d(
            logTag,
            "Method: ${call.method} is not supported yet"
          )
          result.notImplemented()
        }
      }
    }
  }

  /**
   * Discovery printers
   */
  private fun onDiscovery(
    @NonNull call: MethodCall,
    @NonNull result: Result
  ) {
    val printType: String =
      call.argument<String>("type") as String
    Log.d(logTag, "onDiscovery type: $printType")
    when (printType) {
      "TCP" -> {
        onDiscoveryPrinter(
          call,
          Discovery.PORTTYPE_TCP,
          result
        )
      }

      "USB" -> {
        onDiscoveryPrinter(
          call,
          Discovery.PORTTYPE_USB,
          result
        )
      }

      "BT" -> {
        onDiscoveryPrinter(
          call,
          Discovery.PORTTYPE_BLUETOOTH,
          result
        )
      }

      else -> result.notImplemented()
    }
  }

  /**
   * Discovery Printers GENERIC
   */
  private fun onDiscoveryPrinter(
    @NonNull call: MethodCall,
    portType: String,
    @NonNull result: Result
  ) {
    var delay: Long = 7000;
    if (portType == Discovery.PORTTYPE_USB) {
      delay = 1000;
    }
    printers.clear()

    Log.e(
      "onDiscoveryPrinter",
      "Filter = $portType"
    );

    var resp =
      PrinterResult("onDiscoveryPrinter", false)

    when (portType) {
      "TCP" -> {
        onDiscoverNet()
      }

      "USB" -> {
        onDiscoverUsb()
      }

      "BT" -> {
        onDiscoverBle()
      }

      else -> result.notImplemented()
    }

    try {
      Handler(Looper.getMainLooper()).postDelayed(
        {
          resp.success = true
          resp.message = "Successfully!"
          resp.content = printers
          result.success(resp.toJSON())
        },
        delay
      )
    } catch (e: Exception) {
      Log.e(
        "onDiscoveryPrinter",
        "Start not working ${call.method}"
      );
      resp.success = false
      resp.message = "Error while search printer"
      e.printStackTrace()
      result.success(resp.toJSON())
    }
  }

  private var inBleEnum = false

  private fun onDiscoverBle() {
    if (!checkBluetoothPermission()) return
    if (inBleEnum) return
    inBleEnum = true
    Thread {
      val cancel = IntByReference(0)
      val callback =
        CP_OnBluetoothDeviceDiscovered_Callback { deviceName, deviceAddress, privateData ->
          if (!printers.any { it.bdAddress == deviceAddress }) printers.add(
            PrinterInfo(
              bdAddress = deviceAddress,
              ipAddress = deviceAddress,
              model = deviceName,
              type = Discovery.PORTTYPE_BLUETOOTH
            )
          )
        }
      AutoReplyPrint.INSTANCE.CP_Port_EnumBleDevice(
        7000, // Timeout
        cancel,
        callback,
        null
      )
      inBleEnum = false
    }.start()
  }

  private fun enableBluetooth() {
    val bluetoothManager =
      context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val adapter: BluetoothAdapter? =
      bluetoothManager.adapter

    if (null != adapter) {
      if (!adapter.isEnabled) {
        if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_CONNECT
          ) != PackageManager.PERMISSION_GRANTED
        ) {
          // TODO: Consider calling
          //    ActivityCompat#requestPermissions
          // here to request the missing permissions, and then overriding
          //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
          //                                          int[] grantResults)
          // to handle the case where the user grants the permission. See the documentation
          // for ActivityCompat#requestPermissions for more details.
          return
        }
        if (!adapter.isEnabled) {
          throw Exception("Failed to enable bluetooth adapter")
        }
      }
    }
  }

  private fun checkGPSEnabled(): Boolean {
    var isEnabled = false
    val lm =
      context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val ok =
      lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
    if (ok) {
      isEnabled = true
    } else {
      throw Exception("Please enable gps else will not search ble printer")
//      val intent = Intent()
//      intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
//      startActivityForResult(intent, 2)
    }
    return isEnabled
  }

  private fun checkLocationPermission(): Boolean {
    var hasPermission = false
    if (Build.VERSION.SDK_INT >= 23) {
      if (ActivityCompat.checkSelfPermission(
          context,
          Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
      ) {
        val permissionLOCATIONGPS = arrayOf(
          Manifest.permission.ACCESS_COARSE_LOCATION,
          Manifest.permission.ACCESS_FINE_LOCATION,
          Manifest.permission.READ_PHONE_STATE
        )
        ActivityCompat.requestPermissions(
          activity,
          permissionLOCATIONGPS,
          1
        )
      } else {
        hasPermission = true
      }
    } else {
      hasPermission = true
    }
    return hasPermission
  }

  private fun checkBluetoothPermission(): Boolean {
    return checkGPSEnabled() && checkLocationPermission()
  }

  private var inNetEnum = false

  private fun onDiscoverNet() {
    if (inNetEnum) return
    inNetEnum = true
    Thread {
      val cancel =
        IntByReference(0)
      val callback =
        CP_OnNetPrinterDiscovered_Callback { localIp, discoveredMac, discoveredIp, discoveredName, privateData ->
          activity.runOnUiThread {
            if (!printers.any { it.ipAddress == discoveredIp }) printers.add(
              PrinterInfo(
                bdAddress = discoveredIp,
                ipAddress = discoveredIp,
                model = discoveredName,
                macAddress = discoveredMac,
                type = Discovery.PORTTYPE_TCP
              )
            )
          }
        }
      AutoReplyPrint.INSTANCE.CP_Port_EnumNetPrinter(
        3000,
        cancel,
        callback,
        null
      )
      inNetEnum = false
    }.start()
  }

  private fun onDiscoverUsb() {
    val devicePaths =
      AutoReplyPrint.CP_Port_EnumUsb_Helper.EnumUsb()
    if (devicePaths != null) {
      for (i in devicePaths.indices) {
        val name = devicePaths[i]

        if (!printers.any { it.model == name }) printers.add(
          PrinterInfo(
            model = name,
            type = Discovery.PORTTYPE_USB
          )
        )
      }
    }
  }

  private fun openPort(usbPort: String): Pointer {
    return AutoReplyPrint.INSTANCE.CP_Port_OpenUsb(
      usbPort, 1
    )
  }

  private var mPrinter: Pointer? = Pointer.NULL

  /**
   * Print
   */
  private fun onPrint(
    @NonNull call: MethodCall,
    @NonNull result: Result
  ) {
    val type: String =
      call.argument<String>("type") as String
    val target: String =
      call.argument<String>("target") as String

    val commands: ArrayList<Map<String, Any>> =
      call.argument<ArrayList<Map<String, Any>>>("commands") as ArrayList<Map<String, Any>>

    Log.i(logTag, commands.toString())

    var resp =
      PrinterResult("onPrint${type}", false)
    try {
      val printerPointer = openPort(target)
      mPrinter = printerPointer
      if (printerPointer == Pointer.NULL) {
        resp.success = false
        resp.message =
          "Print failed! Cannot open Port: $target"
        result.success(resp.toJSON())
        Log.e(
          logTag,
          "Cannot ConnectPrinter $resp"
        )
        return
      }

      resetPrinter(printerPointer)

      commands.forEach {
        onGenerateCommand(printerPointer, it)
      }
      val resultMessage = queryPrintResultMessage(printerPointer)

      AutoReplyPrint.INSTANCE.CP_Pos_FeedAndHalfCutPaper(printerPointer)
      disconnectPrinter(printerPointer)


      resp.success = true
      resp.message = "Printed $target - $resultMessage"
      Log.d(logTag, resp.toJSON())
      result.success(resp.toJSON());
    } catch (e: Exception) {
      e.printStackTrace()
      resp.success = false
      resp.message = "Print error"
      result.success(resp.toJSON())
      disconnectPrinter(mPrinter)
    }
  }

  private fun resetPrinter(h: Pointer) {
    AutoReplyPrint.INSTANCE.CP_Pos_ResetPrinter(h)
//    AutoReplyPrint.INSTANCE.CP_Pos_SetMultiByteMode(
//      h
//    )
//    AutoReplyPrint.INSTANCE.CP_Pos_SetMultiByteEncoding(
//      h,
//      AutoReplyPrint.CP_MultiByteEncoding_UTF8
//    )
//    AutoReplyPrint.INSTANCE.CP_Pos_SetCharacterCodepage(h, AutoReplyPrint.CP_CharacterCodepage_TCVN3)
  }

  private fun onGenerateCommand(
    h: Pointer,
    command: Map<String, Any>
  ) {
    Log.d(logTag, "onGenerateCommand: $command")

    var commandId: String = command["id"] as String
    if (!commandId.isNullOrEmpty()) {
      var commandValue = command["value"]

      when (commandId) {
        "appendText" -> {
          Log.d(
            logTag,
            "appendText: $commandValue"
          )
          AutoReplyPrint.INSTANCE.CP_Pos_PrintTextInUTF8(h, WString(commandValue.toString()))
        }

        "addBarcode" -> {
          var barcode = ""
          val code = command["barcode"] as? String
          if (code != null) {
            barcode = code
          }

          var type = CP_Label_BarcodeType_EAN13
          val codeType = command["type"] as? Int
          if (codeType != null) {
            type = codeType
          }

          var textPosition = AutoReplyPrint.CP_Label_BarcodeTextPrintPosition_BelowBarcode
          val position =
            command["position"] as? Int
          if (position != null) {
            textPosition = position
          }

          Log.d(
            logTag,
            "addBarcode: $barcode $type $textPosition"
          )

//          AutoReplyPrint.INSTANCE.CP_Pos_SetBarcodeUnitWidth(
//            h,
//            2
//          )
//          AutoReplyPrint.INSTANCE.CP_Pos_SetBarcodeHeight(
//            h,
//            60
//          )
//          AutoReplyPrint.INSTANCE.CP_Pos_SetBarcodeReadableTextFontType(
//            h,
//            0
//          )
//          AutoReplyPrint.INSTANCE.CP_Pos_SetBarcodeReadableTextPosition(
//            h,
//            textPosition
//          )
//
//          AutoReplyPrint.INSTANCE.CP_Pos_PrintBarcode(
//            h,
//            type,
//            barcode
//          )
        }

        "printImage" -> {
          try {
            val bitmap: Bitmap? =
              convertBase64toBitmap(commandValue as String)
            Log.d(
              logTag,
              "appendBitmap: bitmap ${bitmap?.byteCount}"
            )
            if (bitmap != null) {
              AutoReplyPrint.CP_Pos_PrintRasterImageFromData_Helper.PrintRasterImageFromBitmap(
                  h,
                  bitmap.width,
                  bitmap.height,
                  bitmap,
                  AutoReplyPrint.CP_ImageBinarizationMethod_Thresholding,
                  AutoReplyPrint.CP_ImageCompressionMethod_None
                )
            } else {
              throw Exception("Print failed! Cannot convert image URL to bitmap")
            }

          } catch (e: Exception) {

            Log.e(
              logTag,
              "onGenerateCommand Error" + e.localizedMessage
            )
            throw e
          }
        }
      }
    }
  }

  private fun disconnectPrinter(printer: Pointer?) {
    if (printer != Pointer.NULL) {
      AutoReplyPrint.INSTANCE.CP_Port_Close(
        printer
      )
    }
  }

  private fun convertBase64toBitmap(base64Str: String): Bitmap? {
    val decodedBytes: ByteArray = Base64.decode(base64Str, Base64.DEFAULT)
    return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
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



