package sandbox.simpleTCPEchoServer.send42;

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

        // Отправляем число 42
        output.write(intToBytes(42));
        output.flush();
        System.out.println("Клиент отправил число 42");

        // Читаем ответ
        byte[] buffer = new byte[4];
        input.read(buffer);
        int response = bytesToInt(buffer);
        System.out.println("Клиент получил ответ: " + response);

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