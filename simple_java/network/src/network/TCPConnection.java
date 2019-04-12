package network;

import java.net.Socket;
import java.io.*;
import java.nio.charset.Charset;
import java.lang.*;
import java.lang.String;

public class TCPConnection {

    private final Socket socket;
    private final Thread rxThread; //слушает поток ввода и входящие соединения
    private final TCPConnectionListener eventListener;
    private final BufferedReader in;
    private final BufferedWriter out;

    public TCPConnection(TCPConnectionListener eventListener, String ipAddr, int port) throws IOException { //создает новый сокет внутреннего соединения
        this(eventListener, new Socket(ipAddr, port));
    }

    public TCPConnection(TCPConnectionListener eventListener, Socket socket) throws IOException { //принимает готовый объект сокета (внешнего соединения)
        this.eventListener = eventListener;
        this.socket = socket;
        in = new BufferedReader(new InputStreamReader (socket.getInputStream(), Charset.forName("UTF-8")));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8")));
        rxThread = new Thread(new Runnable() {

            @Override
            public void run() { //слушаем входящие соединения
                try {
                    eventListener.onConnectionReady(TCPConnection.this);
                    while (!rxThread.isInterrupted()) {
                        eventListener.onReceiveString(TCPConnection.this, in.readLine() );
                    }
                } catch (IOException e) {
                    eventListener.onException(TCPConnection.this, e);
                } finally {
                    eventListener.onDisconnect(TCPConnection.this);
                }
            }
        });
        rxThread.start();

    }

    public synchronized void sendString(String value) { //отправка сообщения
        try {
            out.write(value + "\r\n"); // перевод строки и возврат каретки
            out.flush(); //сбрасывает все в буфер и отправляет
        }catch (IOException e) {
            eventListener.onException(TCPConnection.this, e);
            disconnect();
        }
    }

    public synchronized void disconnect() {
        rxThread.interrupt();
        try {
            socket.close();
        }catch (IOException e) {
            eventListener.onException(TCPConnection.this, e);
        }
    }


    @Override // переопред
    public String toString() {
        return "TCPConnection: " + socket.getInetAddress() + ": " + socket.getPort();
    }

}
