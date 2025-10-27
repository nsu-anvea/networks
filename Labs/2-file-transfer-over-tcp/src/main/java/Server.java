import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static class ClientHandler extends Thread {
        private Socket socket;
        private byte[] buffer = new byte[4096];

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public String getFileNameFromFilePath(String filePath) {
            String[] parts = filePath.split("/");
            return parts[parts.length - 1];
        }

        @Override
        public void run() {
            long threadId = Thread.currentThread().threadId();
            String clientId = "Клиент-" + threadId;

            try (Socket clientSocket = this.socket) {
                int bytesRead;
                InputStream is = clientSocket.getInputStream();
                OutputStream os = clientSocket.getOutputStream();

                bytesRead = is.read(buffer,0, 4);
                int filePathLength = bytesToInt(buffer);
                System.out.println(clientId + ": Сервер получил длину пути файла: " + filePathLength);

                bytesRead = is.read(buffer, 0, filePathLength);
                String filePath = new String(buffer, 0, bytesRead, "UTF-8");
                System.out.println(clientId + ": Сервер получил путь файла: " + filePath);
                String fileName = getFileNameFromFilePath(filePath);

                File uploadsDir = new File("uploads");
                if (!uploadsDir.exists()) {
                    if (!uploadsDir.mkdirs()) {
                        System.out.println("Не удалось создать директорию.");
                    }
                }
                File outputFile = new File(uploadsDir, fileName);

                // проверка на выход из uploads
                if (!outputFile.getCanonicalPath().startsWith(uploadsDir.getCanonicalPath() + File.separator)) {
                    System.out.println("Попытка выйти из uploads!");

                    // отправляем клиенту 0 (ошибка)
                    os.write(0);
                    os.flush();

                    throw new IllegalAccessException("Попытка выйти из uploads!");
                }
                // отправляем клиенту 1 (все ок)
                os.write(1);
                os.flush();

                // создать промежуточные директории в пути до клиентского файла
                File parentDir = outputFile.getParentFile();
                if (!parentDir.exists()) {
                    parentDir.mkdir();
                }

                bytesRead = is.read(buffer, 0, 8);
                long fileSize = bytesToLong(buffer);
                System.out.println(clientId + ": Сервер получил размер файла: " + fileSize);

                try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                    long startTime = System.currentTimeMillis();
                    System.out.println("[START TIME]" + startTime);
                    long lastReportTime = startTime;
                    long lastReportBytes = 0;
                    long bytesReceived = 0;
                    while (bytesReceived < fileSize) {
                        int toRead = (int) Math.min(buffer.length, fileSize - bytesReceived);

                        bytesRead = is.read(buffer, 0, toRead);
                        if (bytesRead == -1) break;

//                        sleep(10000); // типо обработка клиента

                        fos.write(buffer, 0, bytesRead);

                        System.out.println(clientId + ": Сервер получил пакет: " + bytesRead + "байт");
                        bytesReceived += bytesRead;

                        long currentTime = System.currentTimeMillis();
                        if (currentTime - lastReportTime >= 3000) {
                            long elapsedSeconds = (currentTime - startTime) / 1000;
                            long bytesInLastPeriod = bytesReceived - lastReportBytes;

                            double instantSpeed = (double) bytesInLastPeriod / 3; // байт/сек
                            double avgSpeed = (double) bytesReceived / elapsedSeconds; // байт/сек

                            System.out.println(clientId + ": Мгновенная: " + formatSpeed(instantSpeed) + " KB/s, Средняя: " + formatSpeed(avgSpeed) + " KB/s");

                            lastReportTime = currentTime;
                            lastReportBytes = bytesReceived;
                        }
                    }
                    long totalTimeMs = System.currentTimeMillis() - startTime;
                    System.out.println("[TOTAL TIME]" + totalTimeMs);
                    if (totalTimeMs > 0) {
                        double finalAvgSpeed = (double) bytesReceived / totalTimeMs * 1000; // байт/сек
                        System.out.println(clientId + ": Финальная средняя скорость: " + formatSpeed(finalAvgSpeed) + "KB/s");
                    }

                    boolean success = (fileSize == bytesReceived);
                    os.write(success ? 1 : 0);
                    os.flush();
                }
            } catch (IllegalAccessException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Too few arguments!");
            return;
        }
        int port = Integer.parseInt(args[0]);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер ждет на порту " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Клиент подключился!");
                new ClientHandler(clientSocket).start();
            }
        }
    }

    static String formatSpeed(double bytesPerSec) {
        if (bytesPerSec >= 1024 * 1024) {
            return String.format("%.2f MB/s", bytesPerSec / (1024 * 1024));
        } else if (bytesPerSec >= 1024) {
            return String.format("%.2f KB/s", bytesPerSec / 1024);
        } else {
            return String.format("%.2f B/s", bytesPerSec);
        }
    }

    static int bytesToInt(byte[] b) {
        return (b[0] & 0xFF) << 24 | (b[1] & 0xFF) << 16 |
                (b[2] & 0xFF) << 8 | (b[3] & 0xFF);
    }

    static long bytesToLong(byte[] b) {
        return ((long)(b[0] & 0xFF) << 56) | ((long)(b[1] & 0xFF) << 48) |
                ((long)(b[2] & 0xFF) << 40) | ((long)(b[3] & 0xFF) << 32) |
                ((long)(b[4] & 0xFF) << 24) | ((long)(b[5] & 0xFF) << 16) |
                ((long)(b[6] & 0xFF) << 8) | (long)(b[7] & 0xFF);
    }
}