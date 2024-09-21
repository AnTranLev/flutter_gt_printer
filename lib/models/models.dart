// ignore_for_file: public_member_api_docs, sort_constructors_first
import 'dart:convert';

///
/// Printer Model
///
class PrinterModel {
  /// Connectivity type: TCP | BT | USB
  String? type;

  /// Address
  String? ipAddress;
  String? bdAddress;
  String? macAddress;
  String? model;
  String? target;
  PrinterModel({
    this.type,
    this.ipAddress,
    this.bdAddress,
    this.macAddress,
    this.model,
    this.target,
  });

  PrinterModel copyWith({
    String? type,
    String? ipAddress,
    String? bdAddress,
    String? macAddress,
    String? model,
    String? target,
  }) {
    return PrinterModel(
      type: type ?? this.type,
      ipAddress: ipAddress ?? this.ipAddress,
      bdAddress: bdAddress ?? this.bdAddress,
      macAddress: macAddress ?? this.macAddress,
      model: model ?? this.model,
      target: target ?? this.target,
    );
  }

  Map<String, dynamic> toMap() {
    return <String, dynamic>{
      'type': type,
      'ipAddress': ipAddress,
      'bdAddress': bdAddress,
      'macAddress': macAddress,
      'model': model,
      'target': target,
    };
  }

  factory PrinterModel.fromMap(Map<String, dynamic> map) {
    return PrinterModel(
      type: map['type'] != null ? map['type'] as String : null,
      ipAddress: map['ipAddress'] != null ? map['ipAddress'] as String : null,
      bdAddress: map['bdAddress'] != null ? map['bdAddress'] as String : null,
      macAddress:
          map['macAddress'] != null ? map['macAddress'] as String : null,
      model: map['model'] != null ? map['model'] as String : null,
      target: map['target'] != null ? map['target'] as String : null,
    );
  }

  String toJson() => json.encode(toMap());

  factory PrinterModel.fromJson(String source) =>
      PrinterModel.fromMap(json.decode(source) as Map<String, dynamic>);

  @override
  String toString() {
    return 'PrinterModel(type: $type, ipAddress: $ipAddress, bdAddress: $bdAddress, macAddress: $macAddress, model: $model, target: $target)';
  }

  @override
  bool operator ==(covariant PrinterModel other) {
    if (identical(this, other)) return true;

    return other.type == type &&
        other.ipAddress == ipAddress &&
        other.bdAddress == bdAddress &&
        other.macAddress == macAddress &&
        other.model == model &&
        other.target == target;
  }

  @override
  int get hashCode {
    return type.hashCode ^
        ipAddress.hashCode ^
        bdAddress.hashCode ^
        macAddress.hashCode ^
        model.hashCode ^
        target.hashCode;
  }
}

///
/// Response
///
class PrinterResponse {
  PrinterResponse({
    required this.type,
    required this.success,
    this.message,
    this.content,
  });

  String type;
  bool success;
  String? message;
  dynamic content;

  PrinterResponse copyWith({
    required String type,
    required bool success,
    String? message,
    dynamic content,
  }) =>
      PrinterResponse(
        type: type,
        success: success,
        message: message ?? this.message,
        content: content ?? this.content,
      );

  factory PrinterResponse.fromRawJson(String str) =>
      PrinterResponse.fromJson(json.decode(str));

  String toRawJson() => json.encode(toJson());

  factory PrinterResponse.fromJson(Map<String, dynamic> json) =>
      PrinterResponse(
        type: json["type"],
        success: json["success"],
        message: json["message"],
        content: json["content"],
      );

  Map<String, dynamic> toJson() => {
        "type": type,
        "success": success,
        "message": message,
        "content": content,
      };
}
