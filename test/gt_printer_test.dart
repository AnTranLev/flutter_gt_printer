import 'package:flutter_test/flutter_test.dart';
import 'package:gt_printer/gt_printer.dart';
import 'package:gt_printer/gt_printer_platform_interface.dart';
import 'package:gt_printer/gt_printer_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockGtPrinterPlatform
    with MockPlatformInterfaceMixin
    implements GtPrinterPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final GtPrinterPlatform initialPlatform = GtPrinterPlatform.instance;

  test('$MethodChannelGtPrinter is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelGtPrinter>());
  });

  test('getPlatformVersion', () async {
    GtPrinter gtPrinterPlugin = GtPrinter();
    MockGtPrinterPlatform fakePlatform = MockGtPrinterPlatform();
    GtPrinterPlatform.instance = fakePlatform;

    expect(await gtPrinterPlugin.getPlatformVersion(), '42');
  });
}
