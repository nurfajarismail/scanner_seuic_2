package com.seuic.scanner;

import androidx.annotation.NonNull;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.EventChannel;

import android.content.Context;
import android.util.Log;

import java.util.HashMap;

public class ScannerPlugin implements FlutterPlugin, EventChannel.StreamHandler, DecodeInfoCallBack {

  private String TAG = "ScannerPlugin";
  private String CHANNEL = "com.seuic.scanner/plugin";

  private Context context = null;
  private Scanner scanner = null;
  private boolean mScanRunning = false;

  private EventChannel channel = null;
  private EventChannel.EventSink eventSink = null;

  public ScannerPlugin() {
  }

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new EventChannel(flutterPluginBinding.getBinaryMessenger(), CHANNEL);
    context = flutterPluginBinding.getApplicationContext();
    scanner = ScannerFactory.getScanner(context);
    scanner.open();
    scanner.setDecodeInfoCallBack(this);
    scanner.enable();
    mScanRunning = true;
    Log.d(TAG, "onAttachedToEngine: Scanner On");
    new Thread(runnable).start();
    channel.setStreamHandler(this);
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    mScanRunning = false;
    ScannerKey.close();
    scanner.setDecodeInfoCallBack(null);
    scanner.close();
    scanner = null;
    channel.setStreamHandler(null);
  }

  @Override
  public void onListen(Object arguments, EventChannel.EventSink events) {
    eventSink = events; // Inisialisasi eventSink ketika mendengarkan event
  }

  @Override
  public void onCancel(Object arguments) {
    Log.i(TAG, "ScannerPlugin:onCancel");
    eventSink = null; // Reset eventSink ketika channel dibatalkan
  }

  Runnable runnable = () -> {
    int ret1 = ScannerKey.open();
    if (ret1 > -1) {
      while (mScanRunning) {
        int ret = ScannerKey.getKeyEvent();
        if (ret > -1) {
          switch (ret) {
            case ScannerKey.KEY_DOWN:
              if (scanner != null && mScanRunning) {
                scanner.startScan();
              }
              break;
            case ScannerKey.KEY_UP:
              if (scanner != null && mScanRunning) {
                scanner.stopScan();
              }
              break;
          }
        }
      }
    }
  };

  @Override
  public void onDecodeComplete(DecodeInfo decodeInfo) {
    HashMap<String, Object> map = new HashMap<>();
    map.put("barcode", decodeInfo.barcode);
    map.put("codetype", decodeInfo.codetype);
    map.put("length", decodeInfo.length);

    Log.d(TAG, "onDecodeComplete: Scanned$map");

    // Pastikan eventSink tidak null sebelum memanggil eventSink.success
     // Pastikan kita memanggil eventSink.success di thread UI
    new Handler(Looper.getMainLooper()).post(() -> {
        if (eventSink != null) {
            eventSink.success(map);
        } else {
            Log.e(TAG, "onDecodeComplete: EventSink is null, unable to send scan result.");
        }
    });
  }
}
