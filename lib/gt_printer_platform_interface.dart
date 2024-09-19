import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'gt_printer_method_channel.dart';

abstract class GtPrinterPlatform extends PlatformInterface {
  /// Constructs a GtPrinterPlatform.
  GtPrinterPlatform() : super(token: _token);

  static final Object _token = Object();

  static GtPrinterPlatform _instance = MethodChannelGtPrinter();

  /// The default instance of [GtPrinterPlatform] to use.
  ///
  /// Defaults to [MethodChannelGtPrinter].
  static GtPrinterPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [GtPrinterPlatform] when
  /// they register themselves.
  static set instance(GtPrinterPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
