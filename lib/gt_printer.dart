
import 'gt_printer_platform_interface.dart';

class GtPrinter {
  Future<String?> getPlatformVersion() {
    return GtPrinterPlatform.instance.getPlatformVersion();
  }
}
