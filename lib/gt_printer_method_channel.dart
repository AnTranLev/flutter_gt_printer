import 'dart:io';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:gt_printer/models/enums.dart';
import 'package:gt_printer/models/models.dart';
import 'package:logger/logger.dart';

import 'gt_printer_platform_interface.dart';

var logger = Logger();

/// An implementation of [GtPrinterPlatform] that uses method channels.
class MethodChannelGtPrinter extends GtPrinterPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('gt_printer');

  @override
  Future<String?> getPlatformVersion() async {
    logger.d("getPlatformVersion");
    final version =
        await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }

  bool _isPrinterPlatformSupport({bool throwError = false}) {
    if (Platform.isAndroid) return true;
    if (throwError) {
      throw PlatformException(
          code: "platformNotSupported", message: "Device not supported");
    }
    return false;
  }

  @override
  Future<List<GTPrinterModel>?> onDiscovery(
      {PrinterPortType type = PrinterPortType.usb}) async {
    if (!_isPrinterPlatformSupport(throwError: true)) return null;
    String printType = type.value;
    final Map<String, dynamic> params = {"type": printType};
    String? rep = await methodChannel.invokeMethod('onDiscovery', params);
    if (rep != null) {
      try {
        final response = PrinterResponse.fromRawJson(rep);

        List<dynamic>? prs = response.content;
        if (prs == null) {
          return [];
        }
        if (prs.isNotEmpty) {
          return prs.map((e) {
            final modelName = e['model'];
            return GTPrinterModel(
              ipAddress: e['ipAddress'],
              bdAddress: e['bdAddress'],
              macAddress: e['macAddress'],
              type: printType,
              model: modelName,
              target: e['target'],
            );
          }).toList();
        } else {
          return [];
        }
      } catch (e) {
        rethrow;
      }
    }
    return [];
  }

  @override
  Future<dynamic> onPrint(
      GTPrinterModel printer, List<Map<String, dynamic>> commands) async {
    final Map<String, dynamic> params = {
      "type": printer.type,
      "commands": commands,
      "target": printer.model,
    };
    return await methodChannel.invokeMethod('onPrint', params);
  }
}
