package it.polimi.ds;

import java.io.*;
import java.net.*;
import java.nio.channels.AsynchronousCloseException;
import java.util.*;

public class Client {

    private final int port;
    private MulticastSocket clientSocket;
    private final String username;
    private final HashMap<String, Room> rooms;
    private InetAddress group;
    private NetworkInterface iface;

    private UpToDateChecker upToDateChecker;

    public Client(String name, InetAddress group) {
        this.username = name;
        this.rooms = new HashMap<>();
        this.port = 5000;
        this.group = group;
        connect();
    }

    public static NetworkInterface findActiveWifiInterface() {
        Enumeration<NetworkInterface> interfaces;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            e.printStackTrace();
            return null;
        }

        while (interfaces.hasMoreElements()) {
            NetworkInterface iface = interfaces.nextElement();
            try {
                if (iface.isUp() && !iface.isLoopback() && hasIpAddress(iface)) {
                    System.out.println(iface.getName());
                    return iface;
                }
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }

        return null; // Active Wi-Fi interface not found
    }

    private static boolean hasIpAddress(NetworkInterface iface) {
        Enumeration<InetAddress> addresses = iface.getInetAddresses();
        while (addresses.hasMoreElements()) {
            InetAddress addr = addresses.nextElement();
            // Exclude IPv6 addresses and loopback addresses
            if (!addr.isLoopbackAddress() && addr.getHostAddress().indexOf(':') == -1) {
                return true; // Found an IPv4 address
            }
        }
        return false; // No valid IP address found
    }

    public void deleteRoom(String room) {
        synchronized (rooms) {
            if (rooms.containsKey(room)) {
                rooms.remove(room);
            } else
                System.out.println("[!] You cannot delete a room you are not a participant of");
        }
    }

    public void connect() {
        this.upToDateChecker = new UpToDateChecker(this);
    
        try {
            clientSocket = new MulticastSocket(port);
            this.iface = findActiveWifiInterface();
            /*
             * per usare i test in locale
             * NetworkInterface iface =
             * NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
             */
            clientSocket.joinGroup(new InetSocketAddress(group, port), this.iface);
  
        upToDateChecker.startCheckingTimer();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void createRoom(String roomName, List<String> participants) {
        if(rooms.containsKey(roomName)){
            System.out.println("[!] Room '" + roomName + "' already exists");
            return;
        }
        synchronized (rooms) {
            rooms.put(roomName, new Room(roomName, username, participants));
        }
        Message msg = new Message("Room", this.username, roomName, participants, null, roomName);
        sendMessage(msg);
        System.out.println("[!] Room '" + roomName + "' has been created");
    }

    public synchronized Message createMessage(String room, String type, String content) {
        if (rooms.get(room) == null) {
            System.out.println("[!] Room '" + room + "' does not exist");
            return null;    
        }
       
        Message msg;

        synchronized (rooms) {
            List<String> participants = rooms.get(room).getParticipants();
            VectorClock vectorClock = rooms.get(room).getRoomClock();

            if (!type.equals("Resend")) {
                vectorClock.increment(this.username);
            }

            msg = new Message(type, this.username, content, participants, vectorClock, room);

            if (type.equals("Message")) {
                synchronized(rooms.get(room).getRoomMessages()) {
                    rooms.get(room).getRoomMessages().add(msg);
                }
            }
        }

        sendMessage(msg);
        return msg;
    }

    public void sendMessage(Message message) {
        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            ObjectOutputStream outputStream = new ObjectOutputStream(byteStream);
            outputStream.writeObject(message);
            byte[] data = byteStream.toByteArray();
            DatagramPacket packet = new DatagramPacket(data, data.length, group, port);
            clientSocket.send(packet);
            outputStream.close();
            byteStream.close();
        } catch (IOException e) {
            upToDateChecker.stop();
            upToDateChecker = new UpToDateChecker(this);
            upToDateChecker.startCheckingTimer();
        }

    }

    public void receiveMessage() {
        try {
            while (!clientSocket.isClosed()) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                // block waiting for new message
                clientSocket.receive(packet);
                ByteArrayInputStream byteStream = new ByteArrayInputStream(packet.getData());
                ObjectInputStream inputStream = new ObjectInputStream(byteStream);
                Object obj = inputStream.readObject();

                if (obj instanceof Message msg) {
                    
                    if (msg.getParticipants().contains(username) && !msg.getSender().equals(username)) {
                        synchronized (rooms) {
                            switch (msg.getType()) {
                                case "Room":
                                    if (!rooms.containsKey(msg.getContent())) {
                                        upToDateChecker.stop();
                                        rooms.put(msg.getContent(), new Room(msg.getContent(), username, msg.getParticipants()));
                                        System.out.println("[!] You have been added to room '" + msg.getContent() + "'");
                                        upToDateChecker = new UpToDateChecker(this);
                                        upToDateChecker.startCheckingTimer();
                                    }
                                    break;
                                case "Message":
                                    if(!rooms.containsKey(msg.getRoom())){
                                        break;
                                    }
                                    Room sendTo = rooms.get(msg.getRoom());
                                    sendTo.computeVectorClock(msg);
                                    break;
                                case "Resend":
                                    if(!rooms.containsKey(msg.getRoom())){
                                        break;
                                    }
                                    /*
                                     * It is performed a scan over the messages list of the specified room, in which
                                     * the messages are already sorted causally. Once we find the first occurrence
                                     * of a non-delivered message (where clientClockValue == receivedClockValue + 1
                                     * for some participant different from the sender of the Resend message) we know
                                     * that it has to be delivered again along with all the following messages in
                                     * the list.
                                     * 
                                     * Due to multicast the retransmission of a message happens to all the client in
                                     * the room but we don't bother since non-interested clients simply filter it
                                     * out when checking its vector clock value.
                                     */
                                    Room room = rooms.get(msg.getRoom());
                                    VectorClock receivedVectorClock = msg.getVectorClock();
                                    boolean resend = false;

                                    room.getParticipantsClock().put(msg.getSender(), msg.getVectorClock());

                                    // System.out.println("[" + username + "] RICEVUTA RESEND DA " + msg.getSender()
                                    // + " PER ROOM " + msg.getRoom()); //
                                    // System.out.println(" RECEIVED CLOCK: " +
                                    // msg.getVectorClock().getClock().toString()); //

                                    synchronized(room.getRoomMessages()) {
                                        for (Message m : room.getRoomMessages()) {
                                            // System.out.println(" " + m.getContent() + ": " +
                                            // m.getVectorClock().getClock().toString()); //
                                            if (!resend) {
                                                VectorClock clientVectorClock = m.getVectorClock();
                                                for (Map.Entry<String, Integer> entry : clientVectorClock.getClock()
                                                        .entrySet()) {
                                                    String participant = entry.getKey();
                                                    int clientClockValue = entry.getValue();
                                                    int receivedClockValue = receivedVectorClock.getClock()
                                                            .get(participant);
                                                    // System.out.println(" "+ participant + " ClientVal: " +
                                                    // clientClockValue + "ReceivedVal +receivedClockValue);

                                                    if (!participant.equals(msg.getSender())
                                                            && clientClockValue == receivedClockValue + 1) {
                                                        resend = true;
                                                    }
                                                }
                                            }

                                            if (resend) {
                                                sendMessage(m);
                                            }
                                        }
                                    }
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                }
                inputStream.close();
                byteStream.close();
            }
        } catch (SocketException | AsynchronousCloseException e) {
            // Socket closed, break out of the loop
            System.out.println("[!] Socket closed. Stopping receiving messages.");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("Closing socket");

    }

    public void printRoomList() {
        System.out.println("[!] You are a member of the following rooms:");
        synchronized (rooms) {
            for (String roomName : rooms.keySet()) {
                System.out.println("- " + roomName);
            }
        }
    }

    public void disconnect() {
        try {
            clientSocket.leaveGroup(new InetSocketAddress(group, port), iface);
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        upToDateChecker.stop();
    }

    public static void main(String[] args) {
        InetAddress group = null;
        try {
            group = InetAddress.getByName("239.1.1.1");
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your username: ");
        String name = scanner.nextLine();

        Client client = new Client(name, group);

        // Start a thread to receive messages
        ClientHandler clientHandler = new ClientHandler(scanner, client);
        Thread receiverThread = new Thread(clientHandler);
        receiverThread.start();

        Thread receiver = new Thread(() -> {
            while (!client.getClientSocket().isClosed()) {
                client.receiveMessage();
            }
        });
        receiver.start();
    }

    public MulticastSocket getClientSocket() {
        return clientSocket;
    }

    public String getUsername() {
        return username;
    }

    public synchronized Map<String, Room> getRooms() {
        return rooms;
    }

    public InetAddress getGroup() {
        return group;
    }

    public void setGroup() {
        try {
            clientSocket = new MulticastSocket(port);
            clientSocket.joinGroup(new InetSocketAddress(group, port),
                    findActiveWifiInterface()); 
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
