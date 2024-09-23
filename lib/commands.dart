import 'models/enums.dart';

class GTCommand {
  Map<String, dynamic> append(String data) {
    return {"id": "appendText", "value": data};
  }

  // Map<String, dynamic> rawData(Uint8List data) {
  //   return {"id": "printRawData", "value": data};
  // }

  Map<String, dynamic> addFeedLine(int line) {
    return {"id": "addFeedLine", "value": line};
  }

  // Map<String, dynamic> addLineSpace(dynamic data) {
  //   return {"id": "addLineSpace", "value": data};
  // }

  Map<String, dynamic> addCut() {
    return {"id": "addCut", "value": true};
  }

  // Map<String, dynamic> addTextAlign(EpsonEPOSTextAlign data) {
  //   final cutData = _enumText(data);
  //   return {"id": "addTextAlign", "value": cutData};
  // }

  Map<String, dynamic> printBitmap(String data) {
    Map<String, dynamic> cmd = {"id": "printImage", "value": data};

    return cmd;
  }

  Map<String, dynamic> printPrinterInfo() {
    Map<String, dynamic> cmd = {"id": "printPrinterInfo", "value": true};
    return cmd;
  }

  // Map<String, dynamic> addTextFont(EpsonEPOSFont data) {
  //   final cutData = _enumText(data);
  //   return {"id": "addTextFont", "value": cutData};
  // }

  // Map<String, dynamic> addTextSmooth(bool data) {
  //   return {"id": "addTextSmooth", "value": data};
  // }

  // Map<String, dynamic> addTextSize(int width, int height) {
  //   return {"id": "addTextSize", "width": width, "height": height};
  // }

  // Map<String, dynamic> addTextStyle(
  //     {bool? reverse, bool? underline, bool? bold, Color? color}) {
  //   return {
  //     "id": "addTextStyle",
  //     "reverse": reverse,
  //     "ul": underline,
  //     "em": bold,
  //     "color": color?.name,
  //   };
  // }

  // Map<String, dynamic> addBarcode({
  //   required String barcode,
  //   BarcodeType type = BarcodeType.ean13,
  //   BarcodeTextPrintPosition position = BarcodeTextPrintPosition.belowBarcode,
  // }) {
  //   return {
  //     "id": "addBarcode",
  //     "barcode": barcode,
  //     "position": position.value,
  //     "type": type.value,
  //   };
  // }
}
