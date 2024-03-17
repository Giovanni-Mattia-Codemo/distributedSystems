package it.polimi.ds;

import java.text.SimpleDateFormat;
import java.util.*;

class Room{
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

    public void computeVectorClock(Message msg){
        //Check whether the message has already been sent
        if(isAbsent(msg.getVectorClock())){
            bufferedMessages.add(msg);
        }

        String sender = msg.getSender();
        VectorClock newClock = msg.getVectorClock();

        if(newClock.getClock().get(sender) == roomsClock.getClock().get(sender)+1){
            for(String p : participants){
                if(!Objects.equals(p, sender)){
                    if(newClock.getClock().get(p)<=roomsClock.getClock().get(p)){
                        addMessage(msg);
                        removeFromBuffer(msg);
                        checkMessages();
                    }
                }
            }
        }
    }

    public void removeFromBuffer(Message message){
        for(int i=0; i<bufferedMessages.size();i++){
            Message msg = bufferedMessages.get(i);
            for(String user : participants){
                if(Objects.equals(msg.getVectorClock().getClock().get(user), message.getVectorClock().getClock().get(user))){
                    bufferedMessages.remove(msg);
                }
            }
        }
    }

    public boolean isAbsent(VectorClock message){
        for(Message msg : bufferedMessages){
            for(String user : participants){
                if(!Objects.equals(msg.getVectorClock().getClock().get(user), message.getClock().get(user))){
                    return true;
                }
            }
        }
        return false;
    }

    public void checkMessages(){
        for (Message msg : bufferedMessages) {
            computeVectorClock(msg);
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