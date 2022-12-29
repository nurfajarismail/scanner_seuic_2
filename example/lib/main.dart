import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:scanner/scanner.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  void listenScanner() {
    Scanner.registerListener(listener: onScanned);
  }

  void onScanned(Map<String, dynamic> data) {
    print("DATA: $data");
  }

  @override
  void initState() {
    Scanner.init();
    listenScanner();
    super.initState();
  }

  @override
  void dispose() {
    Scanner.unRegisterListener(onScanned);
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            children: [
              Text('Code: '),
            ],
          ),
        ),
      ),
    );
  }
}
