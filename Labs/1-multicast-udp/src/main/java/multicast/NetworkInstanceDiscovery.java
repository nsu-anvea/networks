package multicast;

import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class NetworkInstanceDiscovery {
    private static final String DEFAULT_GROUP = "230.0.0.1";
    private static final int PORT = 4446;
    private static final int HEARTBEAT_INTERVAL = 2000;
    private static final int TIMEOUT = 3000;

    private String instanceId;
    private int localPort;
    private String multicastGroup;
    private volatile boolean running = true;
    private NetworkInterface networkInterface;

    private Map<String, Long> aliveInstances = new ConcurrentHashMap<>();

    public NetworkInstanceDiscovery(String multicastGroup) {
        this.multicastGroup = multicastGroup;
        this.instanceId = UUID.randomUUID().toString();
        this.localPort = 10000 + new Random().nextInt(55536);
        this.networkInterface = findAppropriateNetworkInterface();
    }

    public void start() {
        new Thread(this::startServer).start();
        new Thread(this::startClient).start();
        new Thread(this::checkTimeouts).start();

        System.out.println("Instance started. ID: " + instanceId);
        System.out.println("Local port: " + localPort);
        System.out.println("Multicast group: " + multicastGroup);
        System.out.println("Using network interface: " +
                (networkInterface != null ? networkInterface.getDisplayName() : "system default"));
    }

    private NetworkInterface findAppropriateNetworkInterface() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (ni.isUp() && !ni.isLoopback() && ni.supportsMulticast()) { // loopback (127.0.0.1 localhost) или (::1 для ipv6)
                    // Проверяем, есть ли у интерфейса адреса нужного типа
                    Enumeration<InetAddress> addresses = ni.getInetAddresses(); // получаем все ip адреса этого интерфейса
                    while (addresses.hasMoreElements()) {
                        InetAddress addr = addresses.nextElement();
                        try {
                            InetAddress group = InetAddress.getByName(multicastGroup);
                            if ((group instanceof Inet6Address && addr instanceof Inet6Address) ||
                                    (group instanceof Inet4Address && addr instanceof Inet4Address)) {
                                return ni;
                            }
                        } catch (UnknownHostException e) {
                        }
                    }
                }
            }
        } catch (SocketException e) {
            System.err.println("Error while searching for network interfaces: " + e.getMessage());
        }
        return null;
    }

    private void startServer() {
        try (DatagramSocket socket = new DatagramSocket()) {
            // Для IPv6 устанавливаем правильный scope ID если нужно
            InetAddress group = InetAddress.getByName(multicastGroup);
            if (group instanceof Inet6Address) {
                Inet6Address ipv6Group = (Inet6Address) group;
                if (ipv6Group.getScopeId() == 0 && networkInterface != null) {
                    // Автоматически устанавливаем scope ID на основе выбранного интерфейса
                    group = Inet6Address.getByAddress(
                            multicastGroup,
                            ipv6Group.getAddress(),
                            networkInterface
                    );
                }
            }

            while (running) {
                String message = instanceId + "|" +
                        getLocalHostAddress() + "|" +
                        localPort;
                byte[] buffer = message.getBytes();

                DatagramPacket packet =
                        new DatagramPacket(buffer, buffer.length, group, PORT);

                socket.send(packet);

                try {
                    Thread.sleep(HEARTBEAT_INTERVAL);
                } catch (InterruptedException e) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startClient() {
        try (MulticastSocket socket = new MulticastSocket(PORT)) {
            InetAddress group = InetAddress.getByName(multicastGroup);

            // Для IPv6 устанавливаем правильный scope ID если нужно
            // scope ID - это числовой идентификатора сетевого интерфейса в IPv6
            // например для eth0 соответствует 15
            if (group instanceof Inet6Address) {
                Inet6Address ipv6Group = (Inet6Address) group;
                if (ipv6Group.getScopeId() == 0 && networkInterface != null) {
                    group = Inet6Address.getByAddress(
                            multicastGroup,
                            ipv6Group.getAddress(),
                            networkInterface
                    );
                }
            }

            // Присоединяемся к multicast группе на выбранном интерфейсе
            if (networkInterface != null) {
                socket.setNetworkInterface(networkInterface);
            }
            socket.joinGroup(new InetSocketAddress(group, PORT), networkInterface);

            byte[] buffer = new byte[1024];

            while (running) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String received = new String(
                        packet.getData(),
                        packet.getOffset(),
                        packet.getLength()
                );
                String[] parts = received.split("\\|");

                if (parts.length == 3 && !parts[0].equals(instanceId)) {
                    String sourceInstanceId = parts[0];
                    String sourceAddress = parts[1];
                    String sourcePort = parts[2];

                    String uniqueId = sourceAddress + ":" + sourcePort;

                    aliveInstances.put(uniqueId, System.currentTimeMillis());
                    printAliveInstances();
                }
            }

            try {
                socket.leaveGroup(new InetSocketAddress(group, PORT), networkInterface);
            } catch (Exception e) {}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getLocalHostAddress() {
        try {
            if (networkInterface != null) {
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    InetAddress group = InetAddress.getByName(multicastGroup);
                    if ((group instanceof Inet6Address && addr instanceof Inet6Address) ||
                            (group instanceof Inet4Address && addr instanceof Inet4Address)) {
                        return addr.getHostAddress();
                    }
                }
            }
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            try {
                return InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException ex) {
                return "unknown";
            }
        }
    }

    private void checkTimeouts() {
        while (running) {
            long currentTime = System.currentTimeMillis();
            boolean changed = false;

            Iterator<Map.Entry<String, Long>> iterator =
                    aliveInstances.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<String, Long> entry = iterator.next();
                if (currentTime - entry.getValue() > TIMEOUT) {
                    System.out.println("Instance timeout: " + entry.getKey());
                    iterator.remove();
                    changed = true;
                }
            }

            if (changed) {
                printAliveInstances();
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private void printAliveInstances() {
        System.out.println("\n--- Alive instances ---");
        for (String uniqueId : aliveInstances.keySet()) {
            System.out.println("Instance: " + uniqueId);
        }
        System.out.println("Total: " + aliveInstances.size());
        System.out.println("-----------------------");
    }

    public void stop() {
        running = false;
    }

    public static void main(String[] args) {
        String group = args.length > 0 ? args[0] : DEFAULT_GROUP;

        try {
            InetAddress groupAddr = InetAddress.getByName(group);
            if (!groupAddr.isMulticastAddress()) {
                System.err.println("Warning: '" + group + "' is not a valid multicast address");
                System.err.println("IPv4 multicast range: 224.0.0.0 - 239.255.255.255");
                System.err.println("IPv6 multicast prefix: ff00::/8");
            }
        } catch (UnknownHostException e) {
            System.err.println("Invalid multicast address: " + group);
            return;
        }

        NetworkInstanceDiscovery discovery = new NetworkInstanceDiscovery(group);
        discovery.start();

        Runtime.getRuntime().addShutdownHook(new Thread(discovery::stop));
    }
}