import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:gt_printer/commands.dart';
import 'package:gt_printer/gt_printer.dart';
import 'package:gt_printer/gt_printer_method_channel.dart';
import 'package:gt_printer/gt_printer_platform_interface.dart';
import 'package:gt_printer/models/enums.dart';
import 'package:gt_printer/models/models.dart';
import 'package:logger/logger.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:image/image.dart' as img;

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
          child: Column(
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
              ListView.builder(
                itemBuilder: (BuildContext context, int index) {
                  final printer = printers[index];
                  return Column(
                    children: [
                      Text('${printer.model}'),
                      Row(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          TextButton(
                            onPressed: () {
                              onPrintText(printer);
                            },
                            child: const Text('Print Text'),
                          ),
                          TextButton(
                            onPressed: () {
                              onPrintImage(printer);
                            },
                            child: const Text('Print Image'),
                          ),
                          TextButton(
                            onPressed: () {
                              onPrintImage(printer);
                            },
                            child: const Text('Print Printer info'),
                          ),
                        ],
                      ),
                    ],
                  );
                },
                itemCount: printers.length,
                primary: false,
                shrinkWrap: true,
                physics: const NeverScrollableScrollPhysics(),
              )
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
          printers = data!;
        });
      } else {
        if (kDebugMode) {
          data = [];
          data.add(PrinterModel(
              type: PrinterPortType.usb.value,
              model: "USD Debug printer model"));
        }
        setState(() {
          printers = data ?? [];
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

  void onPrintText(PrinterModel printer) async {
    try {
      GTCommand command = GTCommand();
      List<Map<String, dynamic>> commands = [];

      commands
          .add(command.append('Đây bước chân kẻ phong trần Lang thang cõi\n'));
      commands.add(command.append("麻辣香锅（上梅林店）\r\n2018年2月7日15:51:00\r\n\r\n"));

      final data = await GtPrinterPlatform.instance.onPrint(printer, commands);
      logger.d('Did discover ${data?.length}');
    } catch (e) {
      logger.e("Error: $e");
    }
  }

  void onPrintImage(PrinterModel printer) async {
    try {
      GTCommand command = GTCommand();
      List<Map<String, dynamic>> commands = [];

      // Load the image as a byte array
      final ByteData imageData = await rootBundle.load('assets/receipt.png');
      // Convert the image to Uint8List
      final Uint8List bytes = imageData.buffer.asUint8List();
      // Decode the image using the 'image' package
      img.Image? originalImage = img.decodeImage(bytes);
      if (originalImage == null) {
        throw Exception("Failed to decode image");
      }
      // Scale the image to 384 pixels in width while maintaining the aspect ratio
      img.Image resizedImage = img.copyResize(originalImage, width: 384);
      // Encode the resized image to PNG format
      Uint8List resizedImageBytes =
          Uint8List.fromList(img.encodePng(resizedImage));
      // Convert the image bytes to a Base64 string
      String base64String = base64Encode(resizedImageBytes);

      commands.add(command.printBitmap(base64String));

      logger.d('${resizedImage.width} - ${resizedImage.height}');

      final data = await GtPrinterPlatform.instance.onPrint(printer, commands);
      logger.d('Did discover ${data?.length}');
    } catch (e) {
      logger.e("Error: $e");
    }
  }

  void onPrintPrinterInfo(PrinterModel printer) async {
    try {
      GTCommand command = GTCommand();
      List<Map<String, dynamic>> commands = [];
      commands.add(command.printPrinterInfo());

      final data = await GtPrinterPlatform.instance.onPrint(printer, commands);
      logger.d('Did discover ${data?.length}');
    } catch (e) {
      logger.e("Error: $e");
    }
  }
}
