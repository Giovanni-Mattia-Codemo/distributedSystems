package it.polimi.ds;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class ClientHandler implements Runnable {
    private final Scanner in;
    private final Client client;

    public ClientHandler(Scanner in, Client client) {
        this.in = in;
        this.client = client;
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
        printLegend();
        boolean running = true;

        while (running) {
            try {
                System.out.print("> ");
                String input = in.nextLine().trim().toLowerCase();
                String[] parts = input.split(" ", 2);
                String command = parts[0];
                String args = parts.length > 1 ? parts[1] : "";

                switch (command) {
                    case "/exit" -> running = false;
                    case "/help" -> printLegend();
                    case "/list" -> client.printRoomList();
                    case "/create" -> handleCreateCommand(args);
                    case "/delete" -> handleDeleteCommand(args);
                    case "/send" -> handleSendMessageCommand(args);
                    default -> System.out.println("Invalid command. Type '/help' to see available commands.");
                }
            } catch (RuntimeException e) {
                System.out.println("An error occurred: " + e.getMessage());
                running = false;
            }
        }
        client.disconnect();
        in.close();
    }

    private void printLegend() {
        System.out.println("Legend:");
        System.out.println("- /send <room_name> <message>: Send a message to the specified room.");
        System.out.println("  Example: /send room1 Hello, everyone!");
        System.out.println(
                "- /create <room_name> <participant1> <participant2> ...: Create a new room with the given name and participants.");
        System.out.println("  Example: /create room2 user1 user2");
        System.out.println("- /delete <room_name>: Delete the specified room. Only available to the room creator.");
        System.out.println("  Example: /delete room2");
        System.out.println("- /list: List all the rooms you are a member of.");
        System.out.println("- /help: Print the command legend.");
        System.out.println("- /exit: Exit the chat application.");
    }

    private void handleCreateCommand(String args) {
        String[] parts = args.split(" ");
        if (parts.length < 2) {
            System.out.println("Invalid format. Usage: /create <room_name> <participant1> <participant2> ...");
        } else {
            String roomName = parts[0];
            List<String> participants = Arrays.asList(Arrays.copyOfRange(parts, 1, parts.length));
            client.createRoom(roomName, participants);
        }
    }

    private void handleDeleteCommand(String args) {
        if (args.isEmpty()) {
            System.out.println("Invalid format. Usage: /delete <room_name>");
        } else {
            client.deleteRoom(args);
        }
    }

    private void handleSendMessageCommand(String args) {
        String[] parts = args.split(" ", 2);
        if (parts.length != 2) {
            System.out.println("Invalid format. Usage: /send <room_name> <message>");
        } else {
            String roomName = parts[0];
            String message = parts[1];
            client.createMessage(roomName, "Message", message);
        }
    }
}
