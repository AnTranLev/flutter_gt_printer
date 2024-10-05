import 'package:gt_printer/gt_scanner_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

abstract class GtScannerPlatform extends PlatformInterface {
  /// Constructs a GtScannerPlatform.
  GtScannerPlatform() : super(token: _token);

  static final Object _token = Object();

  static GtScannerPlatform _instance = MethodChannelGtScanner();

  /// The default instance of [GtScannerPlatform] to use.
  ///
  /// Defaults to [MethodChannelGtScanner].
  static GtScannerPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [GtScannerPlatform] when
  /// they register themselves.
  static set instance(GtScannerPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPluginStatus() {
    throw UnimplementedError('getPluginStatus() has not been implemented.');
  }

  Future<bool?> connectScanner() {
    throw UnimplementedError('connectScanner() has not been implemented.');
  }

  Future<void> offScan() {
    throw UnimplementedError('offScan() has not been implemented.');
  }

  Future<String?> startScan() {
    throw UnimplementedError('startScan() has not been implemented.');
  }

  Future<void> enableScanner() {
    throw UnimplementedError('enableScanner() has not been implemented.');
  }

  Future<void> disableScanner() {
    throw UnimplementedError('disableScanner() has not been implemented.');
  }
}
