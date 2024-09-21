import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:gt_printer/gt_printer.dart';
import 'package:gt_printer/gt_printer_method_channel.dart';
import 'package:gt_printer/gt_printer_platform_interface.dart';
import 'package:gt_printer/models/enums.dart';
import 'package:gt_printer/models/models.dart';
import 'package:logger/logger.dart';
import 'package:permission_handler/permission_handler.dart';

final logger = Logger();

void main() {
  GtPrinterPlatform.instance = MethodChannelGtPrinter();
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  final _gtPrinterPlugin = GtPrinter();
  List<PrinterModel> printers = [];

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    // We also handle the message potentially returning null.
    try {
      platformVersion = await _gtPrinterPlugin.getPlatformVersion() ??
          'Unknown platform version';
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('GT Printer Plugin'),
        ),
        body: SafeArea(
          child: Row(
            mainAxisSize: MainAxisSize.max,
            children: [
              Column(
                mainAxisSize: MainAxisSize.max,
                children: [
                  Text('Running on: $_platformVersion\n'),
                  ElevatedButton(
                      onPressed: () => onDiscovery(PrinterPortType.tcp),
                      child: const Text('Discovery TCP')),
                  ElevatedButton(
                      onPressed: () => onDiscovery(PrinterPortType.usb),
                      child: const Text('Discovery USB')),
                  ElevatedButton(
                      onPressed: () => onDiscovery(PrinterPortType.bluetooth),
                      child: const Text('Discovery Bluetooth')),
                  ElevatedButton(
                      onPressed: () => onBleRequestPermission(),
                      child: const Text('Request runtime permission')),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }

  onDiscovery(PrinterPortType type) async {
    try {
      List<PrinterModel>? data =
          await GtPrinterPlatform.instance.onDiscovery(type: type);
      logger.d('Did discover ${data?.length}');
      if (data != null && data.isNotEmpty) {
        for (var element in data) {
          logger.d(element.toJson());
        }
        setState(() {
          printers = data;
        });
      } else {
        setState(() {
          printers = [];
        });
      }
    } catch (e) {
      logger.e("Error: $e");
    }
  }

  void onBleRequestPermission() async {
    Map<Permission, PermissionStatus> statuses = await [
      Permission.location,
      Permission.bluetooth,
      Permission.bluetoothConnect,
      Permission.bluetoothScan,
    ].request();
    logger.d(statuses[Permission.bluetooth]);
  }
}
