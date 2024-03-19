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
    private final InetAddress group;

    public Client(String name, InetAddress group){
        this.username = name;
        this.rooms = new HashMap<>();
        this.port = 5000;
        this.group = group;

        try {
            clientSocket = new MulticastSocket(port);
            clientSocket.joinGroup(new InetSocketAddress(group, port), NetworkInterface.getByInetAddress(InetAddress.getLocalHost())); //change this to allow multicast
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteRoom(String room){
        if(rooms.containsKey(room)) {
            createMessage(room, "Deletion", room);
            rooms.remove(room);
        }
        else System.out.println("You cannot delete a room you are not a participant of.");
    }

    public void createRoom(String roomName, List<String> participants){
        rooms.put(roomName, new Room(participants));
        Message msg = new Message("Room", this.username, roomName, participants, null, roomName);
        sendMessage(msg);
        System.out.println("> Room '" + roomName + "' has been created.");
    }

    public void createMessage(String room, String type, String content){
        List<String> participants = rooms.get(room).getParticipants();

        //Implement vector clock
        VectorClock vectorClock = rooms.get(room).getRoomsClock();
        vectorClock.increment(this.username);
        //End vector clock computation

        Message msg = new Message(type, this.username, content, participants, vectorClock, room);
        sendMessage(msg);
    }

    public void sendMessage(Message message){
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
            e.printStackTrace();
        }
    }

    public void receiveMessage(){
        try {
            while (!clientSocket.isClosed()) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                //block waiting for new message
                clientSocket.receive(packet);
                ByteArrayInputStream byteStream = new ByteArrayInputStream(packet.getData());
                ObjectInputStream inputStream = new ObjectInputStream(byteStream);
                Object obj = inputStream.readObject();


                if (obj instanceof Message msg) {
                    if (msg.getParticipants().contains(username)) {
                        switch (msg.getType()) {
                            case "Room":
                                if (!rooms.containsKey(msg.getContent())) {
                                    rooms.put(msg.getContent(), new Room(msg.getParticipants()));
                                    System.out.print("You have been added to room " + msg.getContent() + "\n> ");
                                }
                                break;
                            case "Message":
                                Room sendTo = rooms.get(msg.getRoom());
                                sendTo.computeVectorClock(msg);
                                break;
                            case "Deletion":
                                rooms.remove(msg.getContent());
                                System.out.print("Room \"" + msg.getContent() + "\" has been deleted.\n> ");
                                break;
                            default:
                                break;
                        }
                    }
                }
                inputStream.close();
                byteStream.close();
            }
        } catch (SocketException | AsynchronousCloseException e) {
            // Socket closed, break out of the loop
            System.out.println("Socket closed. Stopping receiving messages.");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void printRoomList(){
        System.out.println("You are a member of the following rooms:");
        for (String roomName : rooms.keySet()) {
            System.out.println("- " + roomName);
        }
    }

    public void disconnect(){
        try {
            clientSocket.leaveGroup(new InetSocketAddress(group, port), NetworkInterface.getByInetAddress(InetAddress.getLocalHost())); //change this to allow multicast
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        InetAddress group = null;
        try {
            group = InetAddress.getByName("224.0.2.0");
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

        // Start a thread to receive messages
        Thread receiver = new Thread(() -> {
            while (!client.clientSocket.isClosed()) {
                client.receiveMessage();
            }
        });
        receiver.start();

    }
}
