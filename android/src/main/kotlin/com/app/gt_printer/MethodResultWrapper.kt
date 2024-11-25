package com.app.gt_printer

import android.os.Handler
import android.os.Looper
import io.flutter.plugin.common.MethodChannel

class MethodResultWrapper(methodResult: MethodChannel.Result) :
  MethodChannel.Result {

  private val methodResult: MethodChannel.Result = methodResult
  private val handler: Handler = Handler(Looper.getMainLooper())

  override fun success(result: Any?) {
    if (result is JSONConvertible) {
      handler.post { methodResult.success(result.toMap()) }
    } else {
      handler.post { methodResult.success(result) }
    }
  }

  override fun error(errorCode: String, errorMessage: String?, errorDetails: Any?) {
    handler.post { methodResult.error(errorCode, errorMessage, errorDetails) }
  }

  override fun notImplemented() {
    handler.post { methodResult.notImplemented() }
  }
}