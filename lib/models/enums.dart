enum PrinterPortType { tcp, bluetooth, usb }

extension PrinterPortTypeValue on PrinterPortType {
  String get value {
    switch (this) {
      case PrinterPortType.tcp:
        return "TCP";
      case PrinterPortType.bluetooth:
        return "BT";
      case PrinterPortType.usb:
        return "USB";
    }
  }
}
