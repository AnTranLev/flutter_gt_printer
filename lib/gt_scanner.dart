import 'package:gt_printer/gt_scanner_platform_interface.dart';

class GtScanner {
  Future<String?> getPluginStatus() {
    return GtScannerPlatform.instance.getPluginStatus();
  }
}
