package com.app.gt_printer

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.NonNull

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** GtPrinterPlugin */
class GtPrinterPlugin: FlutterPlugin, MethodCallHandler {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel

  private val TAG = "GTPrinterBridgeModule"

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "gt_printer")
    channel.setMethodCallHandler(this)
  }

  override fun onMethodCall(call: MethodCall, rawResult: Result) {
    val result = MethodResultWrapper(rawResult)
    Thread(MethodRunner(call, result)).start()
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

  inner class MethodRunner(call: MethodCall, result: Result) : Runnable {
    private val call: MethodCall = call
    private val result: Result = result

    override fun run() {
      Log.d(TAG, "Method Called: ${call.method}")
      when (call.method) {
        "onDiscovery" -> {
          onDiscovery(call, result)
        }
//        "onPrint" -> {
//          onPrint(call, result)
//        }
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
          Log.d(TAG, "Method: ${call.method} is not supported yet")
          result.notImplemented()
        }
      }
    }
  }

  /**
   * Discovery printers
   */
  private fun onDiscovery(@NonNull call: MethodCall, @NonNull result: Result) {
    val printType: String = call.argument<String>("type") as String
    Log.d(TAG, "onDiscovery type: $printType")
    when (printType) {
      "TCP" -> {
        onDiscoveryPrinter(call, Discovery.PORTTYPE_TCP, result)
      }
      "USB" -> {
        onDiscoveryPrinter(call, Discovery.PORTTYPE_USB, result)
      }
      "BT" -> {
        onDiscoveryPrinter(call, Discovery.PORTTYPE_BLUETOOTH, result)
      }
      "ALL" -> {
        onDiscoveryPrinter(call, Discovery.TYPE_PRINTER, result)
      }
      else -> result.notImplemented()
    }
  }

  /**
   * Discovery Printers GENERIC
   */
  private fun onDiscoveryPrinter(@NonNull call: MethodCall, portType: Int, @NonNull result: Result) {
    var delay:Long = 7000;
    if(portType == Discovery.PORTTYPE_USB){
      delay = 1000;
    }
    printers.clear()
    var filter = FilterOption()
    filter.portType = portType;
    Log.e("onDiscoveryPrinter", "Filter = $portType");

    var resp = EpsonEposPrinterResult("onDiscoveryPrinter", false)
    try {
      Discovery.start(context, filter, mDiscoveryListener)
      Handler(Looper.getMainLooper()).postDelayed({
        resp.success = true
        resp.message = "Successfully!"
        resp.content = printers
        result.success(resp.toJSON())
        stopDiscovery()
      }, delay)
    } catch (e: Exception) {
      Log.e("onDiscoveryPrinter", "Start not working ${call.method}");
      resp.success = false
      resp.message = "Error while search printer"
      e.printStackTrace()
      result.success(resp.toJSON())
    }
  }
}
