import 'package:gt_printer/commands.dart';
import 'package:gt_printer/layout/font_a_chars_per_line.dart';
import 'package:gt_printer/layout/space_between.dart';
import 'package:gt_printer/models/models.dart';

// ignore: constant_identifier_names
const DEFAULT_PAPER_WIDTH = 80;

Map<String, dynamic> addTextLine(
    GTPrinterModel printer, SpaceBetweenParams params) {
  final printerCharsPerLinePerWidth = getFontACharsPerLine(printer.model ?? '');
  // final setting = await EpsonEPOS.getPrinterSetting(printer);

  final charsPerLine = printerCharsPerLinePerWidth[DEFAULT_PAPER_WIDTH] ?? 42;

  final text = spaceBetween((charsPerLine).ceil(), params);
  return GTCommand().append(text);
}
