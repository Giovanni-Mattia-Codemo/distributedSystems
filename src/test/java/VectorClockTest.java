import it.polimi.ds.Client;
import it.polimi.ds.ClientHandler;
import it.polimi.ds.Message;
import it.polimi.ds.Room;
import it.polimi.ds.VectorClock;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class VectorClockTest {
    @Test
    public void testVectorClock() throws UnknownHostException, InterruptedException {
        Client client1 = new Client("Fede", InetAddress.getByName("224.0.2.0"));
        Client client2 = new Client("Gio", InetAddress.getByName("224.0.2.0"));

        clientReceiver(client1, new Scanner(System.in));
        clientReceiver(client2, new Scanner(System.in));

        client1.createRoom("sksksk", Arrays.asList("Fede", "Gio"));
        Thread.sleep(100);

        client1.printRoomList();
        client2.printRoomList();

        System.out.println(client1.createMessage("sksksk", "Message", "ciao!").getVectorClock().toString());
        System.out.println(client2.createMessage("sksksk", "Message", "i limoni signora").getVectorClock().toString());
        Thread.sleep(100);
        System.out.println(client1.createMessage("sksksk", "Message", ":/").getVectorClock().toString());
        Thread.sleep(100);

    }

    public void clientReceiver(Client client, Scanner scanner) {
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
    }
}
