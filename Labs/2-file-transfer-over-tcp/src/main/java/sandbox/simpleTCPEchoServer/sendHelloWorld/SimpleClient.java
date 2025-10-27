package sandbox.simpleTCPEchoServer.sendHelloWorld;

import java.io.*;
import java.net.*;

public class SimpleClient {
    public static void main(String[] args) throws IOException {
        String host = "localhost";
        int port = 5555;

        Socket socket = new Socket(host, port);
        System.out.println("Подключились к серверу");

        OutputStream output = socket.getOutputStream();
        InputStream input = socket.getInputStream();

        String helloWorld = "Hello, World!";
        byte[] bytes = helloWorld.getBytes("UTF-8");
        int strLen = bytes.length;

        // Отправляем число strLen
        output.write(intToBytes(strLen));
        output.flush();
        System.out.println("Клиент отправил число strLen");

        // Отправляем строку "Hello, World!"
        output.write(bytes);
        output.flush();
        System.out.println("Клиент отправил строку Hello, World!");

        // Читаем ответ
        byte[] numBuffer = new byte[4];
        input.read(numBuffer);
        int response = bytesToInt(numBuffer);
        System.out.println("Клиент получил число strLen: " + response);

        byte[] strBuffer = new byte[response];
        input.read(strBuffer);
        String recvStr = new String(strBuffer, "UTF-8");
        System.out.println("Клиент получил строку " + recvStr);

        socket.close();
    }

    static int bytesToInt(byte[] b) {
        return (b[0] & 0xFF) << 24 | (b[1] & 0xFF) << 16 |
                (b[2] & 0xFF) << 8 | (b[3] & 0xFF);
    }

    static byte[] intToBytes(int i) {
        return new byte[]{(byte)(i >> 24), (byte)(i >> 16),
                (byte)(i >> 8), (byte)i};
    }
}
