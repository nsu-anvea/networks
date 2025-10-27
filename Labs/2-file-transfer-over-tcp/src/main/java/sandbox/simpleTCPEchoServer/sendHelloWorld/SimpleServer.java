package sandbox.simpleTCPEchoServer.sendHelloWorld;

import java.io.*;
import java.net.*;

public class SimpleServer {
    public static void main(String[] args) throws IOException {
        int port = 5555;
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Сервер ждёт на порту " + port);

        Socket clientSocket = serverSocket.accept();
        System.out.println("Клиент подключился!");

        // Получаем входной поток (читаем от клиента)
        InputStream input = clientSocket.getInputStream();
        // Получаем выходной поток (пишем клиенту)
        OutputStream output = clientSocket.getOutputStream();

        // Читаем одно число strLen
        byte[] numBuffer = new byte[4];
        input.read(numBuffer);
        int strLen = bytesToInt(numBuffer);
        System.out.println("Сервер получил число strLen=" + strLen);

        // Читаем полученную строку
        byte[] strBuffer = new byte[strLen];
        input.read(strBuffer);
        String recvStr = new String(strBuffer, "UTF-8");
        System.out.println("Сервер получил строку recvStr=\"" + recvStr + "\"");

        // Отправляем обратно то же число strLen
        output.write(intToBytes(strLen));
        output.flush();
        System.out.println("Сервер отправил число strLen");

        // Отправляем обратно строку
        strBuffer = recvStr.getBytes("UTF-8");
        output.write(strBuffer);
        output.flush();
        System.out.println("Сервер отправил строку=" + recvStr);

        clientSocket.close();
        serverSocket.close();
    }

    // Преобразуем 4 байта в int
    static int bytesToInt(byte[] b) {
        return (b[0] & 0xFF) << 24 | (b[1] & 0xFF) << 16 |
                (b[2] & 0xFF) << 8 | (b[3] & 0xFF);
    }

    // Преобразуем int в 4 байта
    static byte[] intToBytes(int i) {
        return new byte[]{(byte)(i >> 24), (byte)(i >> 16),
                (byte)(i >> 8), (byte)i};
    }
}
