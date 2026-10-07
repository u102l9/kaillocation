package com.kail.location.inject.utils;

import android.os.Handler;
import android.os.HandlerThread;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AsyncSocketClient {
    private final Handler socketHandler;
    private Socket socket;
    private String host;
    private int port;
    private SocketCallback primaryCallback;
    private List<SocketCallback> callbacks = Collections.synchronizedList(new ArrayList());
    private boolean connected;
    private BufferedReader reader;
    private DataOutputStream writer;
    private int connectTimeoutMillis = 0;
    Object lock = new Object();

    class ConnectTask implements Runnable {
        final String targetHost;
        final int targetPort;

        ConnectTask(String host, int port) {
            this.targetHost = host;
            this.targetPort = port;
        }

        @Override
        public void run() {
            AsyncSocketClient.this.connect(this.targetHost, this.targetPort);
        }
    }

    class ReadLoop implements Runnable {
        ReadLoop() {
        }

        @Override
        public void run() {
            int index = 0;
            try {
                AsyncSocketClient.this.connected = true;
                while (AsyncSocketClient.this.connected) {
                    if (AsyncSocketClient.this.isConnected()) {
                        String message = AsyncSocketClient.this.reader.readLine();
                        if (message != null) {
                            try {
                                if (AsyncSocketClient.this.primaryCallback != null) {
                                    AsyncSocketClient.this.primaryCallback.onMessage(message);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            for (int i = 0; i < AsyncSocketClient.this.callbacks.size(); i++) {
                                try {
                                    AsyncSocketClient.this.callbacks.get(i).onMessage(message);
                                } catch (Exception e2) {
                                    e2.printStackTrace();
                                }
                            }
                        }
                    } else {
                        AsyncSocketClient.this.connected = false;
                        throw new Exception("disconnect.");
                    }
                }
            } catch (Exception unused) {
                try {
                    if (AsyncSocketClient.this.primaryCallback != null) {
                        AsyncSocketClient.this.primaryCallback.onDisconnected();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                while (index < AsyncSocketClient.this.callbacks.size()) {
                    try {
                        AsyncSocketClient.this.callbacks.get(index).onDisconnected();
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                    index++;
                }
            }
        }
    }

    class SendStringTask implements Runnable {
        final String message;

        SendStringTask(String message) {
            this.message = message;
        }

        @Override
        public void run() {
            AsyncSocketClient.this.sendLine(this.message);
        }
    }

    public interface SocketCallback {
        void onConnected();

        void onMessage(String message);

        void onDisconnected();

        void onError(String message);
    }

    public AsyncSocketClient() {
        HandlerThread handlerThread = new HandlerThread("LSocketClient");
        handlerThread.start();
        this.socketHandler = new Handler(handlerThread.getLooper());
    }

    public void connectAsync(String host, int port) {
        new Thread(new ConnectTask(host, port)).start();
    }

    public void connect(String host, int port) {
        long connectStartTimeMillis;
        this.host = host;
        this.port = port;
        Socket currentSocket = this.socket;
        if (currentSocket != null && !currentSocket.isClosed()) {
            try {
                this.socket.close();
                this.socket = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        int callbackIndex = 0;
        try {
            Socket newSocket = new Socket();
            this.socket = newSocket;
            newSocket.setReuseAddress(true);
            connectStartTimeMillis = System.currentTimeMillis();
            try {
                this.socket.connect(new InetSocketAddress(host, port), this.connectTimeoutMillis);
                try {
                    this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                try {
                    this.writer = new DataOutputStream(this.socket.getOutputStream());
                } catch (Exception e3) {
                    e3.printStackTrace();
                }
                new Thread(new ReadLoop()).start();
                try {
                    SocketCallback callback = this.primaryCallback;
                    if (callback != null) {
                        callback.onConnected();
                    }
                } catch (Exception e4) {
                    e4.printStackTrace();
                }
                while (callbackIndex < this.callbacks.size()) {
                    try {
                        this.callbacks.get(callbackIndex).onConnected();
                    } catch (Exception e5) {
                        e5.printStackTrace();
                    }
                    callbackIndex++;
                }
            } catch (Exception e6) {
                String message = e6.getMessage();
                if (connectStartTimeMillis != -1 && this.connectTimeoutMillis != 0 && System.currentTimeMillis() - connectStartTimeMillis >= this.connectTimeoutMillis) {
                    message = "Connect Timeout.";
                }
                try {
                    SocketCallback callback = this.primaryCallback;
                    if (callback != null) {
                        callback.onError(message);
                    }
                } catch (Exception e7) {
                    e7.printStackTrace();
                }
                while (callbackIndex < this.callbacks.size()) {
                    try {
                        this.callbacks.get(callbackIndex).onError(message);
                    } catch (Exception e8) {
                        e8.printStackTrace();
                    }
                    callbackIndex++;
                }
            }
        } catch (Exception e9) {
            connectStartTimeMillis = -1;
        }
    }

    public boolean isConnected() {
        try {
            Socket currentSocket = this.socket;
            if (currentSocket == null || !currentSocket.isConnected() || this.socket.isClosed() || this.socket.isInputShutdown() || this.socket.isOutputShutdown()) {
                return false;
            }
            this.socket.sendUrgentData(255);
            return true;
        } catch (Exception unused) {
            return false;
        }
    }

    public void sendAsync(String message) {
        this.socketHandler.post(new SendStringTask(message));
    }

    public void setCallback(SocketCallback callback) {
        this.primaryCallback = callback;
    }

    public boolean sendBytes(byte[] bytes) {
        Socket currentSocket;
        if (bytes == null || (currentSocket = this.socket) == null || currentSocket.isClosed() || !this.socket.isConnected() || this.socket.isOutputShutdown()) {
            return false;
        }
        try {
            this.writer.write(bytes);
            this.writer.flush();
            return true;
        } catch (Exception unused) {
            return false;
        }
    }

    public boolean sendLine(String message) {
        return sendBytes((message + "\n").getBytes());
    }
}
