package it.polimi.ds;

import java.text.SimpleDateFormat;
import java.util.*;

public class Room{
    private final List<String> participants;
    private VectorClock roomsClock;
    private final List<Message> roomMessages;
    private final List<Message> bufferedMessages;

    public Room(List<String> participants) {
        this.roomMessages = new ArrayList<>();
        this.participants = participants;
        Map<String, Integer> clock = new HashMap<>();
        for(String p : participants){
            clock.put(p,0);
        }
        this.roomsClock = new VectorClock(clock);
        this.bufferedMessages = new ArrayList<>();
    }

    /*  now the hard part is decide how to recover lost messages since we can have
        different vector clocks in the case messages are sent from different clients
        at the same moment (no order can be inferred)
     */
    public boolean computeVectorClock(Message msg){
        String sender = msg.getSender();
        VectorClock newClock = msg.getVectorClock();

        /* Ignore if the message is an old message
           This works cuz the client resending the lost messages is always the same
         */
        if(newClock.getClock().get(sender) <= roomsClock.getClock().get(sender)){
            return false;
        }

        //Check whether the message has already been saved
        if(isAbsent(msg.getContent())){
            bufferedMessages.add(msg);
        }
        if(newClock.getClock().get(sender) == roomsClock.getClock().get(sender)+1){
            for(String p : participants){
                if(!Objects.equals(p, sender)){
                    if(newClock.getClock().get(p)<=roomsClock.getClock().get(p)){
                        addMessage(msg);
                        removeFromBuffer(msg);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void removeFromBuffer(Message message){
        //simplified check on content since each vector clock could be different
        for(int i=0; i<bufferedMessages.size();i++){
            Message msg = bufferedMessages.get(i);
            if(message.getContent().equals(msg.getContent())){
                bufferedMessages.remove(msg);
                break;
            }
        }
    }

    public boolean isAbsent(String message){
        //simplified check on content since each vector clock could be different
        for(Message msg : bufferedMessages){
            if(msg.getContent().equals(message)){
                return false;
            }
        }
        return true;
    }

    //there's an issue if messages are eliminated
    public void checkMessages(){
        for (Message msg : bufferedMessages) {
            if(computeVectorClock(msg))
                break;
        }
    }

    public void addMessage(Message msg){
        roomMessages.add(msg);
        roomsClock.increment(msg.getSender()); //not sure about this but i guess it's the same as doing the merge since we must have received all the messages sent before
        System.out.println("Messages for room '" + msg.getRoom() + "':");
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        sdf.setTimeZone(TimeZone.getDefault());
        Calendar calendar = Calendar.getInstance();
        String formattedTime = sdf.format(calendar.getTime());
        System.out.println("[" + formattedTime + "] [" + msg.getSender() + "]: " + msg.getContent());
        checkMessages();
    }

    public List<Message> getRoomMessages() {
        return roomMessages;
    }

    public VectorClock getRoomsClock() {
        return roomsClock;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public int getParticipantIndex(String user){
        int index = 0;
        for(String e : participants){
            if(e.equals(user)){
                return index;
            }
            index++;
        }
        return -1;
    }

    public void insertMessage(Message msg){
        bufferedMessages.add(msg);
    }
}

    /*
    public synchronized void receiveMessage(Message message) {
        VectorClock messageClock = message.getVectorClock();
        if (!bufferedMessages.containsKey(message.getSenderId())) {
            bufferedMessages.put(message.getSenderId(), new LinkedList<>());
        }

        Queue<Message> messageQueue = bufferedMessages.get(message.getSenderId());

        // If message is in order, process it immediately
        if (messageQueue.isEmpty() || messageQueue.peek().getVectorClock().happenedBefore(messageClock)) {
            processMessage(message);
            // Process subsequent messages in the buffer if they are in order
            while (!messageQueue.isEmpty() && messageQueue.peek().getVectorClock().happenedBefore(messageClock)) {
                processMessage(messageQueue.poll());
            }
        } else {
            // Buffer out-of-order message
            messageQueue.add(message);
        }
    }

    // Method to process a received message
    private void processMessage(Message message) {
        System.out.println("Received message: " + message.getContent());
    }

    // Method to request retransmission of missing or out-of-order messages
    public synchronized void requestRetransmission(String senderId, VectorClock currentClock) {
        Queue<Message> messageQueue = bufferedMessages.get(senderId);
        if (messageQueue != null) {
            for (Message message : messageQueue) {
                if (!currentClock.happenedBefore(message.getVectorClock())) {
                    // Request retransmission of missing or out-of-order message
                    System.out.println("Requesting retransmission of message: " + message.getContent());
                }
            }
        }
    }

    public synchronized void bufferMessage(Message message) {
        VectorClock messageClock = message.getVectorClock();
        Queue<Message> messageQueue = bufferedMessages.computeIfAbsent(messageClock, k -> new LinkedList<>());
        messageQueue.add(message);
    }

   */