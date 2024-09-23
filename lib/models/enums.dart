// ignore_for_file: non_constant_identifier_names

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

enum BarcodeTextPrintPosition {
  aboveBarcode,
  belowBarcode,
  aboveAndBelowBarcode,
}

extension BarcodeTextPrintPositionExtension on BarcodeTextPrintPosition {
  int get value {
    switch (this) {
      case BarcodeTextPrintPosition.aboveBarcode:
        return 1;
      case BarcodeTextPrintPosition.belowBarcode:
        return 2;
      case BarcodeTextPrintPosition.aboveAndBelowBarcode:
        return 3;
      default:
        return -1; // Fallback for unexpected values
    }
  }
}

enum BarcodeType {
  upca,
  upce,
  ean13,
  ean8,
  code39,
  itf,
  codebar,
  code93,
  code128,
  code11,
  msi,
  code128M,
  ean128,
  _25C,
  _39C,
  _39,
  ean13Plus2,
  ean13Plus5,
  ean8Plus2,
  ean8Plus5,
  post,
  upcaPlus2,
  upcaPlus5,
  upcePlus2,
  upcePlus5,
  cpost,
  msic,
  plessey,
  itf14,
  ean14,
}

extension BarcodeTypeExtension on BarcodeType {
  int get value {
    switch (this) {
      case BarcodeType.upca:
        return 0;
      case BarcodeType.upce:
        return 1;
      case BarcodeType.ean13:
        return 2;
      case BarcodeType.ean8:
        return 3;
      case BarcodeType.code39:
        return 4;
      case BarcodeType.itf:
        return 5;
      case BarcodeType.codebar:
        return 6;
      case BarcodeType.code93:
        return 7;
      case BarcodeType.code128:
        return 8;
      case BarcodeType.code11:
        return 9;
      case BarcodeType.msi:
        return 10;
      case BarcodeType.code128M:
        return 11;
      case BarcodeType.ean128:
        return 12;
      case BarcodeType._25C:
        return 13;
      case BarcodeType._39C:
        return 14;
      case BarcodeType._39:
        return 15;
      case BarcodeType.ean13Plus2:
        return 16;
      case BarcodeType.ean13Plus5:
        return 17;
      case BarcodeType.ean8Plus2:
        return 18;
      case BarcodeType.ean8Plus5:
        return 19;
      case BarcodeType.post:
        return 20;
      case BarcodeType.upcaPlus2:
        return 21;
      case BarcodeType.upcaPlus5:
        return 22;
      case BarcodeType.upcePlus2:
        return 23;
      case BarcodeType.upcePlus5:
        return 24;
      case BarcodeType.cpost:
        return 25;
      case BarcodeType.msic:
        return 26;
      case BarcodeType.plessey:
        return 27;
      case BarcodeType.itf14:
        return 28;
      case BarcodeType.ean14:
        return 29;
      default:
        return -1; // Handle unknown cases
    }
  }
}
