import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:gt_printer/gt_scanner_platform_interface.dart';
import 'package:gt_printer/models/logger.dart';

/// An implementation of [GtScannerPlatform] that uses method channels.
class MethodChannelGtScanner extends GtScannerPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('gt_scanner');

  @override
  Future<String?> getPluginStatus() async {
    logger.d("getPluginStatus");
    if (!_isScannerPlatformSupport(throwError: true)) return null;
    final version = await methodChannel.invokeMethod<String>('getPluginStatus');
    return version;
  }

  bool _isScannerPlatformSupport({bool throwError = false}) {
    if (Platform.isAndroid) return true;
    if (throwError) {
      throw PlatformException(
          code: "platformNotSupported", message: "Device not supported");
    }
    return false;
  }

  @override
  Future<bool?> connectScanner() async {
    logger.d("connectScanner");
    if (!_isScannerPlatformSupport(throwError: true)) return false;
    return await methodChannel.invokeMethod('connectScanner');
  }

  @override
  Future<String?> startScan() async {
    logger.d("startScan");
    if (!_isScannerPlatformSupport(throwError: true)) return null;
    return await methodChannel.invokeMethod<String>('startScan');
  }

  @override
  Future<void> enableScanner() async {
    logger.d("enableScanner");
    if (!_isScannerPlatformSupport(throwError: true)) return;
    await methodChannel.invokeMethod('enableScanner');
  }

  @override
  Future<void> disableScanner() async {
    logger.d("disableScanner");
    if (!_isScannerPlatformSupport(throwError: true)) return;
    await methodChannel.invokeMethod('disableScanner');
  }
}
