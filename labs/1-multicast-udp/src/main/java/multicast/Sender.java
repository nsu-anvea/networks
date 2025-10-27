package multicast;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;

public class Sender {
    private static final String MULTICAST_GROUP = "239.1.2.3";
    private static final int PORT = 1234;
    private static final int TTL = 32;

    public static void main(String[] args) {
        try {
            MulticastSocket socket = new MulticastSocket();
            socket.setTimeToLive(TTL);
            InetAddress group = InetAddress.getByName(MULTICAST_GROUP);

            for (int i = 0; i < 10; i++) {
                String message = i + ": Hello, Multicast World!";
                byte[] buffer = message.getBytes(StandardCharsets.UTF_8);

                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
                socket.send(packet);
                System.out.println("Sent: " + message);

                Thread.sleep(5000);

            }
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
