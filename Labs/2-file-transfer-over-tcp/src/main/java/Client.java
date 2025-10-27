import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Client {
    static int bytesRead;
    static byte[] buffer = new byte[4096];

    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.out.println("Too few arguments!");
            return;
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String filePath = args[2];

        Socket socket = new Socket(host, port);
        System.out.println("Подключились к серверу");

        // не нужно закрывать отдельно т.к. после закрытия socket, они автоматически закроются
        InputStream input = socket.getInputStream();
        OutputStream output = socket.getOutputStream();

        byte[] bytes = filePath.getBytes(StandardCharsets.UTF_8);

        output.write(intToBytes(bytes.length));
        output.flush();
        System.out.println("Клиент отправил filePathLength=" + bytes.length);

        output.write(bytes);
        output.flush();
        System.out.println("Клиент отправил pathToFile=" + filePath);

        bytesRead = input.read(buffer, 0, 1); // server response
        if (bytesRead != 1) {
            System.out.println("Не получили ответ от сервера!");
            socket.close();
            return;
        }
        if (buffer[0] == 0) {
            System.out.println("Попытка escape из uploads!");
            socket.close();
            return;
        }

        File file = new File(filePath);
        output.write(longToBytes(file.length()));
        output.flush();
        System.out.println("Клиент отправил fileSize=" + file.length());

        try (FileInputStream fis = new FileInputStream(file)) {
            while ((bytesRead = fis.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
                output.flush();
                System.out.println("Клиент отправил пакет " + bytesRead + " байт");
            }
        }

        bytesRead = input.read(buffer, 0, 1);
        boolean success = (buffer[0] == 1);
        if (success) {
            System.out.println("Файл успешно отправлен");
        } else {
            System.out.println("Ошибка при отправке файла");
        }

        socket.close();
    }

    static byte[] intToBytes(int i) {
        return new byte[]{(byte)(i >> 24), (byte)(i >> 16),
                (byte)(i >> 8), (byte)i};
    }

    static byte[] longToBytes(long l) {
        return new byte[]{
                (byte)(l >> 56), (byte)(l >> 48), (byte)(l >> 40), (byte)(l >> 32),
                (byte)(l >> 24), (byte)(l >> 16), (byte)(l >> 8), (byte)l
        };
    }
}