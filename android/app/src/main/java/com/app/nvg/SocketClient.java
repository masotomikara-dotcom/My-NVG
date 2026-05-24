package com.app.nvg;

import android.graphics.Rect;
import android.graphics.YuvImage;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.Socket;

public class SocketClient {

    private Socket socket;
    private OutputStream outputStream;
    private final String serverIp;
    private final int serverPort;
	
    public SocketClient(String ip, int port) {
        this.serverIp = ip;
        this.serverPort = port;
    }

    public void connect() {
        new Thread(() -> {
            try {
                socket = new Socket(serverIp, serverPort);
                outputStream = socket.getOutputStream();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void sendFrame(byte[] data, int width, int height, int format) {
        if (outputStream == null) return;
        new Thread(() -> {
            try {
                YuvImage yuvImage = new YuvImage(data, format, width, height, null);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                yuvImage.compressToJpeg(new Rect(0, 0, width, height), 80, bos);
                byte[] jpegBytes = bos.toByteArray();

                int size = jpegBytes.length;
                byte[] sizeBytes = new byte[]{
                    (byte) (size & 0xFF),
                    (byte) ((size >> 8) & 0xFF),
                    (byte) ((size >> 16) & 0xFF),
                    (byte) ((size >> 24) & 0xFF)
                };
                
                outputStream.write(sizeBytes);
                outputStream.write(jpegBytes);
                outputStream.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void disconnect() {
        try {
            if (outputStream != null) outputStream.close();
            if (socket != null) socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}