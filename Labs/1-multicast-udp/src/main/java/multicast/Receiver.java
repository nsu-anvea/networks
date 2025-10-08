package multicast;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;

public class Receiver {
    public static final String MULTICAST_GROUP = "239.1.2.3";
    public static final int PORT = 1234;

    public static void main(String[] args) {
        try {
            MulticastSocket socket = new MulticastSocket(0);
            InetAddress group = InetAddress.getByName(MULTICAST_GROUP);

            socket.joinGroup(group);

            System.out.println("Multicast Receiver started.");
            System.out.println("Listen on " + MULTICAST_GROUP + ":" + socket.getPort());

            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                socket.receive(packet);

                String message = new String(
                        packet.getData(),
                        packet.getOffset(),
                        packet.getLength(),
                        "UTF-8"
                );
                System.out.println("Received from " + packet.getAddress() + ": " + message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
