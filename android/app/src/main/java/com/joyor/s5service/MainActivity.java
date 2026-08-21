package com.joyor.s5service;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.HashMap;

public class MainActivity extends Activity {

    private static final String ACTION_USB_PERM = "com.joyor.s5service.USB_PERMISSION";

    private WebView webView;
    private UsbManager usbManager;
    private UsbDevice usbDevice;
    private UsbDeviceConnection connection;
    private UsbInterface usbInterface;
    private UsbEndpoint epIn;
    private UsbEndpoint epOut;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private volatile boolean reading = false;
    private Thread readThread;

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERM.equals(action)) {
                UsbDevice dev = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    if (dev != null) openDevice(dev);
                } else {
                    jsCallback("onUsbError", "USB-Berechtigung verweigert");
                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                jsCallback("onUsbDisconnected", "true");
                closeDevice();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setStatusBarColor(0xFF1A1D23);

        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERM);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(usbReceiver, filter);

        webView = findViewById(R.id.webview);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setDatabaseEnabled(true);
        settings.setTextZoom(100);

        webView.addJavascriptInterface(new UsbJsBridge(), "AndroidUsb");
        webView.setWebViewClient(new WebViewClient());
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        } else {
            webView.loadUrl("file:///android_asset/index.html");
        }
    }

    private void jsCallback(String fn, String arg) {
        handler.post(() -> webView.evaluateJavascript(
                "if(window." + fn + ")" + fn + "('" + arg.replace("'", "\\'") + "')", null));
    }

    private String bytesToHex(byte[] bytes, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) sb.append(String.format("%02x", bytes[i] & 0xff)).append(' ');
        return sb.toString().trim();
    }

    private void openDevice(UsbDevice dev) {
        usbDevice = dev;
        connection = usbManager.openDevice(dev);
        if (connection == null) {
            jsCallback("onUsbError", "Gerät konnte nicht geöffnet werden");
            return;
        }

        usbInterface = null;
        epIn = null;
        epOut = null;

        for (int i = 0; i < dev.getInterfaceCount(); i++) {
            UsbInterface iface = dev.getInterface(i);
            if (iface.getInterfaceClass() == UsbConstants.USB_CLASS_CDC_DATA
                    || iface.getInterfaceClass() == UsbConstants.USB_CLASS_VENDOR_SPEC
                    || iface.getInterfaceClass() == UsbConstants.USB_CLASS_COMM) {
                for (int j = 0; j < iface.getEndpointCount(); j++) {
                    UsbEndpoint ep = iface.getEndpoint(j);
                    if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                        if (ep.getDirection() == UsbConstants.USB_DIR_IN) epIn = ep;
                        else epOut = ep;
                    }
                }
                if (epIn != null || epOut != null) {
                    usbInterface = iface;
                    break;
                }
            }
        }

        if (usbInterface == null) {
            for (int i = 0; i < dev.getInterfaceCount(); i++) {
                UsbInterface iface = dev.getInterface(i);
                for (int j = 0; j < iface.getEndpointCount(); j++) {
                    UsbEndpoint ep = iface.getEndpoint(j);
                    if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                        if (ep.getDirection() == UsbConstants.USB_DIR_IN && epIn == null) epIn = ep;
                        else if (ep.getDirection() == UsbConstants.USB_DIR_OUT && epOut == null) epOut = ep;
                    }
                }
                if (epIn != null || epOut != null) {
                    usbInterface = iface;
                    break;
                }
            }
        }

        if (usbInterface == null) {
            jsCallback("onUsbError", "Kein serielles Interface gefunden");
            connection.close();
            connection = null;
            return;
        }

        connection.claimInterface(usbInterface, true);

        // Set baud rate 115200 for common USB-serial chips (CH340, CP210x, FTDI)
        // CH340 set baud
        connection.controlTransfer(0x40, 0x9A, 0x1312, 0xB282, null, 0, 1000);
        connection.controlTransfer(0x40, 0x9A, 0x0F2C, 0x0008, null, 0, 1000);
        // CP210x set baud
        byte[] baudBytes = {0x00, (byte) 0xC2, 0x01, 0x00}; // 115200
        connection.controlTransfer(0x41, 0x1E, 0, 0, baudBytes, 4, 1000);
        // FTDI set baud
        connection.controlTransfer(0x40, 0x03, 0x001A, 0, null, 0, 1000);
        // Enable DTR/RTS
        connection.controlTransfer(0x21, 0x22, 0x03, 0, null, 0, 1000);

        jsCallback("onUsbConnected", dev.getDeviceName());

        String info = "{\"vendor\":\"0x" + String.format("%04x", dev.getVendorId())
                + "\",\"product\":\"0x" + String.format("%04x", dev.getProductId())
                + "\",\"name\":\"" + (dev.getProductName() != null ? dev.getProductName() : dev.getDeviceName()) + "\"}";
        jsCallback("onUsbDeviceInfo", info);

        startReading();
    }

    private void startReading() {
        if (epIn == null || connection == null) return;
        reading = true;
        readThread = new Thread(() -> {
            byte[] buf = new byte[64];
            while (reading && connection != null) {
                int len = connection.bulkTransfer(epIn, buf, buf.length, 100);
                if (len > 0) {
                    final String hex = bytesToHex(buf, len);
                    jsCallback("onUsbData", hex);
                }
            }
        });
        readThread.setDaemon(true);
        readThread.start();
    }

    private void closeDevice() {
        reading = false;
        if (readThread != null) {
            try { readThread.join(500); } catch (InterruptedException ignored) {}
            readThread = null;
        }
        if (connection != null) {
            if (usbInterface != null) connection.releaseInterface(usbInterface);
            connection.close();
            connection = null;
        }
        usbDevice = null;
        usbInterface = null;
        epIn = null;
        epOut = null;
    }

    class UsbJsBridge {
        @JavascriptInterface
        public boolean isAvailable() {
            return usbManager != null;
        }

        @JavascriptInterface
        public void connect() {
            HashMap<String, UsbDevice> devices = usbManager.getDeviceList();
            if (devices.isEmpty()) {
                jsCallback("onUsbError", "Kein USB-Gerät gefunden. Kabel prüfen.");
                return;
            }
            UsbDevice dev = devices.values().iterator().next();
            if (usbManager.hasPermission(dev)) {
                openDevice(dev);
            } else {
                PendingIntent pi = PendingIntent.getBroadcast(MainActivity.this, 0,
                        new Intent(ACTION_USB_PERM), PendingIntent.FLAG_IMMUTABLE);
                usbManager.requestPermission(dev, pi);
            }
        }

        @JavascriptInterface
        public void disconnect() {
            jsCallback("onUsbDisconnected", "true");
            closeDevice();
        }

        @JavascriptInterface
        public void sendBytes(String hexStr) {
            if (epOut == null || connection == null) return;
            String[] parts = hexStr.trim().split("\\s+");
            byte[] data = new byte[parts.length];
            for (int i = 0; i < parts.length; i++) data[i] = (byte) Integer.parseInt(parts[i], 16);
            connection.bulkTransfer(epOut, data, data.length, 1000);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        closeDevice();
        try { unregisterReceiver(usbReceiver); } catch (Exception ignored) {}
        super.onDestroy();
    }
}
