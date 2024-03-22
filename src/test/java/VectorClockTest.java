import it.polimi.ds.Client;
import it.polimi.ds.ClientHandler;
import it.polimi.ds.Message;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.Arrays;

import java.util.Scanner;

public class VectorClockTest {
    @Test
    public void testVectorClock() throws UnknownHostException, InterruptedException {
        Client client1 = new Client("Fede", InetAddress.getByName("224.0.2.0"));
        Client client2 = new Client("Gio", InetAddress.getByName("224.0.2.0"));
        Client client3 = new Client("Uno", InetAddress.getByName("224.0.2.0"));
        Client client4 = new Client("Due", InetAddress.getByName("224.0.2.0"));

        Thread t1 = clientReceiver(client1, new Scanner(System.in));
        Thread t2 = clientReceiver(client2, new Scanner(System.in));
        Thread t3 = clientReceiver(client3, new Scanner(System.in));
        Thread t4 = clientReceiver(client4, new Scanner(System.in));

        client1.createRoom("sksksk", Arrays.asList("Fede", "Gio", "Uno", "Due"));
        Thread.sleep(100);

        //client1.printRoomList();
        //client2.printRoomList();

        System.out.println(client1.createMessage("sksksk", "Message", "ciao!").getVectorClock().toString());
        System.out.println(client2.createMessage("sksksk", "Message", "i limoni signora").getVectorClock().toString());
        System.out.println(client1.createMessage("sksksk", "Message", ":/").getVectorClock().toString());
        Thread.sleep(200);
        System.out.println(client2.createMessage("sksksk", "Message", "SONO CLIENT 2").getVectorClock().toString());
        System.out.println(client1.createMessage("sksksk", "Message", "SONO CLIENT 1").getVectorClock().toString());
        Thread.sleep(200);
        System.out.println(client2.createMessage("sksksk", "Message", "SONO CLIENT 2").getVectorClock().toString());
        Thread.sleep(200);
        System.out.println(client4.createMessage("sksksk", "Message", "CI SONO ANCHE IO EH").getVectorClock().toString());
        Thread.sleep(200);
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
}
