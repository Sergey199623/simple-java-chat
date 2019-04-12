package network;

import java.net.Socket;
import java.io.*;
import java.nio.charset.Charset;

public interface TCPConnectionListener {

    void onConnectionReady (TCPConnection tcpConnection); //запуск соединения
    void onReceiveString(TCPConnection tcpConnection, String value); //приняли соединение
    void onDisconnect(TCPConnection tcpConnection); // дисконнект (потеря соединения)
    void onException(TCPConnection tcpConnection, Exception e); //возможные исключения

}
