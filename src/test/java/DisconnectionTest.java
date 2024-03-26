import it.polimi.ds.Client;
import it.polimi.ds.ClientHandler;
import it.polimi.ds.Message;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.UnknownHostException;

import java.util.Arrays;

import java.util.Scanner;

public class DisconnectionTest {
    @Test
    public void testVectorClock() throws UnknownHostException, InterruptedException {
        Client client1 = new Client("Fede", InetAddress.getByName("224.0.2.0"));
        Client client2 = new Client("Gio", InetAddress.getByName("224.0.2.0"));
        Client client3 = new Client("Simo", InetAddress.getByName("224.0.2.0"));

        clientReceiver(client1, new Scanner(System.in));
        clientReceiver(client2, new Scanner(System.in));
        clientReceiver(client3, new Scanner(System.in));

        client1.createRoom("sksksk", Arrays.asList("Fede", "Gio", "Simo"));
        Thread.sleep(4000);

        System.out.println(client1.createMessage("sksksk", "Message", "ciao!").getVectorClock().toString());

        System.out.println(client3.createMessage("sksksk", "Message", "i limoni signora").getVectorClock().toString());
        System.out.println(client2.createMessage("sksksk", "Message", ":/").getVectorClock().toString());

        Thread.sleep(1000);

        stopCommunication(client1);
        System.out.println(client2.createMessage("sksksk", "Message", "://").getVectorClock().toString());
        Thread.sleep(1000);
        System.out.println(client3.createMessage("sksksk", "Message", ":///").getVectorClock().toString());
        Thread.sleep(1000);
        System.out.println(client2.createMessage("sksksk", "Message", ":////").getVectorClock().toString());
        Thread.sleep(1000);
        System.out.println(client3.createMessage("sksksk", "Message", "://///").getVectorClock().toString());
        Thread.sleep(1000);
        resumeCommunication(client1, "224.0.2.0");

        Thread.sleep(5000);
        System.out.println(client1.createMessage("sksksk", "Message", "FINE").getVectorClock().toString());
        Thread.sleep(1000);


    }

    public Thread clientReceiver(Client client, Scanner scanner) {
        // Start a thread to receive messages
        ClientHandler clientHandler = new ClientHandler(scanner, client);
        Thread receiverThread = new Thread(clientHandler);
        receiverThread.start();

        // Start a thread to receive messages
        Thread receiver = new Thread(() -> {
            while (!client.getClientSocket().isClosed()) {
                client.receiveMessage();
            }
        });
        receiver.start();

        return receiver;
    }

    public void stopCommunication(Client client) {
        try {
            client.getSocket().leaveGroup(new InetSocketAddress(client.getGroup(), 5000), NetworkInterface.getByInetAddress(InetAddress.getLocalHost()));
            client.getSocket().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void resumeCommunication(Client client, String host) {
        try {
            client.setGroup(InetAddress.getByName(host));
            clientReceiver(client, new Scanner(System.in));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
