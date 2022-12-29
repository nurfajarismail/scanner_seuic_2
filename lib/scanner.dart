
import 'dart:async';

import 'package:flutter/services.dart';

class Scanner {
  static const MethodChannel _channel = MethodChannel('scanner');

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static const String channelName = 'com.seuic.scanner/plugin';
  static EventChannel? _scannerPlugin;
  static StreamSubscription? _subscription;

  static List<Function> listeners = [];

  /// You need to initialize it as necessary, when the program starts for the first time.
  static void init() {
    _scannerPlugin ??= const EventChannel(channelName);
    _subscription = _scannerPlugin!
        .receiveBroadcastStream()
        .listen(_onEvent, onError: _onError);
  }

  static void registerListener(
      {required void Function(Map<String, dynamic> data) listener}) {
    if (!listeners.contains(listener)) listeners.add(listener);
  }

  static void unRegisterListener(
      void Function(Map<String, dynamic> data) listener) {
    if (listeners.contains(listener)) listeners.remove(listener);
  }

  static void dispose() {
    listeners.clear();
    assert(_subscription != null);
    _subscription!.cancel();
  }

  static void _onEvent(data) {
    var d = Map<String, dynamic>.from(data);
    listeners.forEach((listener) => listener(d));
  }

  static void _onError(Object error) {
    listeners.forEach((listener) => listener(error));
  }
}
