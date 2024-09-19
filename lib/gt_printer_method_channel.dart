import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'gt_printer_platform_interface.dart';

/// An implementation of [GtPrinterPlatform] that uses method channels.
class MethodChannelGtPrinter extends GtPrinterPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('gt_printer');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }
}
