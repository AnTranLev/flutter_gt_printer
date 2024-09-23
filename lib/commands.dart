import 'dart:typed_data';

class GTCommand {
  String _enumText(dynamic enumName) {
    List<String> ns = enumName.toString().split('.');
    if (ns.isNotEmpty) {
      return ns.last;
    }
    return enumName.toString();
  }

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

  Map<String, dynamic> appendBitmap(
      dynamic data, int width, int height, int posX, int posY) {
    Map<String, dynamic> cmd = {"id": "addImage", "value": data};
    cmd['width'] = width;
    cmd['height'] = height;
    cmd['posX'] = posX;
    cmd['posY'] = posY;

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
  //   Epos2Barcode type = Epos2Barcode.EPOS2_BARCODE_EAN13,
  //   int? width,
  //   int? height,
  //   EpsonEPOSFont font = EpsonEPOSFont.FONT_A,
  //   Epos2Hri position = Epos2Hri.EPOS2_HRI_BELOW,
  // }) {
  //   final fontData = _enumText(font);

  //   return {
  //     "id": "addBarcode",
  //     "barcode": barcode,
  //     "width": width,
  //     "height": height,
  //     "font": fontData,
  //     "position": position.value,
  //     "type": type.value,
  //   };
  // }
}
