package it.polimi.ds;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class ClientHandler implements Runnable{

    private final Scanner in;
    private Client client;
    public ClientHandler(Scanner in, Client client){
        this.in = in;
        this.client = client;
    }

    private void printLegend(){
        System.out.println("Legend:");
        System.out.println("- /send <room_name> <message>: Send a message to the specified room.");
        System.out.println("  Example: /send room1 Hello, everyone!");
        System.out.println("- /create <room_name> <participant1> <participant2> ...: Create a new room with the given name and participants.");
        System.out.println("  Example: /create room2 user1 user2");
        System.out.println("- /delete <room_name>: Delete the specified room. Only available to the room creator.");
        System.out.println("  Example: /delete room2");
        System.out.println("- /list: List all the rooms you are a member of.");
        System.out.println("- /read <room_name>: Read messages from the specified room. Available to participants of the room.");
        System.out.println("  Example: /read room1");
        System.out.println("- /help: Print the command legend.");
        System.out.println("- /exit: Exit the chat application.");
    }

    /**
     * When an object implementing interface {@code Runnable} is used
     * to create a thread, starting the thread causes the object's
     * {@code run} method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method {@code run} is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        String input;
        printLegend();
        while(true){
            try{
                System.out.print("> ");
                input = in.nextLine();
                if (input.equalsIgnoreCase("/exit")) {
                    break;
                } else if (input.equals("/help")) {
                    printLegend();
                } else if (input.equals("/list")) {
                    client.printRoomList();
                } else if (input.startsWith("/create ")) {
                    String[] parts = input.split(" ");
                    if (parts.length < 3) {
                        System.out.println("Invalid format. Usage: /create <room_name> <participant1> <participant2> ...");
                    } else {
                        String roomName = parts[1];
                        List<String> participants = new ArrayList<>(Arrays.asList(parts).subList(2, parts.length));
                        client.createRoom(roomName, participants);
                    }
                } else if (input.startsWith("/delete ")) {
                    String[] parts = input.split(" ");
                    if (parts.length != 2) {
                        System.out.println("Invalid format. Usage: /delete <room_name>");
                    } else {
                        String roomName = parts[1];
                        client.deleteRoom(roomName);
                    }
                } else {
                    String[] parts = input.split(" ", 2);
                    if (parts.length == 2 && parts[0].equalsIgnoreCase("/send")) {
                        String[] subParts = parts[1].split(" ", 2);
                        if (subParts.length == 2) {
                            String roomName = subParts[0];
                            String message = subParts[1];
                            client.createMessage(roomName, "Message", message);
                        } else {
                            System.out.println("Invalid format. Usage: /send <room_name> <message>");
                        }
                    } else {
                        System.out.println("Invalid command. Type '/help' to see available commands.");
                    }
                }
            }catch (RuntimeException e){
                break;
            }

        }
        client.disconnect();
        in.close();
    }
}
