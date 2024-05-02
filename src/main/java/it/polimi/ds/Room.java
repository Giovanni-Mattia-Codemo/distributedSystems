package it.polimi.ds;
import java.util.*;

public class Room {
    private final String roomName;
    private final String username;
    private final List<String> participants;
    private HashMap<String, VectorClock> participantsClock;
    private VectorClock minimumClock;
    private final List<Message> roomMessages;
    private final List<Message> bufferedMessages;

    public Room(String roomName, String username, List<String> participants) {
        this.roomName = roomName;
        this.username = username;
        this.participants = participants;
        roomMessages = new ArrayList<>();
        bufferedMessages = new ArrayList<>();
        
        Map<String, Integer> clock = new HashMap<>();
        for (String p : participants) {
            clock.put(p, 0);
        }

        participantsClock = new HashMap<>();
        for (String p : participants) {
            participantsClock.put(p, new VectorClock(clock));
        }

        minimumClock = new VectorClock(clock);
    }

    public synchronized boolean computeVectorClock(Message msg) {
        String sender = msg.getSender();
        VectorClock newClock = msg.getVectorClock();

        if (newClock.getClock().get(sender) <= getRoomClock().getClock().get(sender)) {
            return false;
        }

        if (isAbsent(msg.getContent())) {
            insertInBuffer(msg);
        }
        if (newClock.getClock().get(sender) == getRoomClock().getClock().get(sender) + 1) {
            for (String p : participants) {
                if (!Objects.equals(p, sender)) {
                    if (newClock.getClock().get(p) > getRoomClock().getClock().get(p)) {
                        return false;
                    }
                }
            }
            addMessage(msg);
            removeFromBuffer(msg);
            return true;
        }
        return false;
    }

    public void removeFromBuffer(Message message) {
        for (int i = 0; i < bufferedMessages.size(); i++) {
            Message msg = bufferedMessages.get(i);
            if (message.getContent().equals(msg.getContent())) {
                bufferedMessages.remove(msg);
                break;
            }
        }
    }

    public boolean isAbsent(String message) {
        for (Message msg : bufferedMessages) {
            if (msg.getContent().equals(message)) {
                return false;
            }
        }
        return true;
    }

    public void checkMessages() {
        for (Message msg : bufferedMessages) {
            if (computeVectorClock(msg))
                break;
        }

    }

    public void addMessage(Message msg) {
        synchronized (roomMessages) {
            roomMessages.add(msg);
        }
        getRoomClock().increment(msg.getSender()); // not sure about this but i guess it's the same as doing the merge since
                                              // we must have received all the messages sent before
        System.out.println(msg.toString());
        checkMessages();
    }

    public synchronized List<Message> getRoomMessages() {
        synchronized (roomMessages) {
            return roomMessages;
        }
    }

    public synchronized VectorClock getRoomClock() {
        return participantsClock.get(username);
    }

    public synchronized Map<String, VectorClock> getParticipantsClock() {
        return participantsClock;
    }

    public synchronized List<Message> getBufferedMessages() {
        return bufferedMessages;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public int getParticipantIndex(String user) {
        int index = 0;
        for (String e : participants) {
            if (e.equals(user)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    public void insertInBuffer(Message msg) {
        bufferedMessages.add(msg);
    }

    public String getLastWriter() {
        synchronized (roomMessages) {
            return roomMessages.get(roomMessages.size() - 1).getSender();
        }
    }

    public String getRoomName() {
        return roomName;
    }

    public void updateMinimumClock() {
        // Iterate over each participant's clock

        for (Map.Entry<String, Integer> entry : getParticipantsClock().get(username).getClock().entrySet()) {
            String participant = entry.getKey();
            int value = entry.getValue();

            minimumClock.getClock().put(participant, value);
        }

        for (VectorClock vectorClock : getParticipantsClock().values()) {
            Map<String, Integer> clockMap = vectorClock.getClock();

            // Update minimumClock with the smallest value for each participant
            for (Map.Entry<String, Integer> entry : clockMap.entrySet()) {
                String participant = entry.getKey();
                int value = entry.getValue();

                if (!minimumClock.getClock().containsKey(participant) || value < minimumClock.getClock().get(participant)) {
                    minimumClock.getClock().put(participant, value);
                }
            }
        }

        //System.out.println(username + ": " + minimumClock.getClock());
    }

    public void removeDeliveredMessagesToAll() {
        synchronized(roomMessages) {
            Iterator<Message> iterator = roomMessages.iterator();
            while (iterator.hasNext()) {
                Message m = iterator.next();
                if (m.getVectorClock().isLessOrEqualThan(minimumClock)) {
                    iterator.remove();
                }
            }
        }
    }
}
