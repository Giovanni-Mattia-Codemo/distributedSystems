package it.polimi.ds;

import java.text.SimpleDateFormat;
import java.util.*;

public class Room{
    private final String roomName;
    private final List<String> participants;
    private VectorClock roomClock;
    private final List<Message> roomMessages;
    private final List<Message> bufferedMessages;


    public Room(String roomName, List<String> participants) {
        this.roomName = roomName;
        this.roomMessages = new ArrayList<>();
        this.participants = participants;
        Map<String, Integer> clock = new HashMap<>();
        for(String p : participants){
            clock.put(p,0);
        }
        this.roomClock = new VectorClock(clock);
        this.bufferedMessages = new ArrayList<>();
    }

    public synchronized boolean computeVectorClock(Message msg){
        String sender = msg.getSender();
        VectorClock newClock = msg.getVectorClock();

        if(newClock.getClock().get(sender) <= roomClock.getClock().get(sender)){
            return false;
        }

        if(isAbsent(msg.getContent())){
            insertInBuffer(msg);
        }
        if(newClock.getClock().get(sender) == roomClock.getClock().get(sender)+1){
            for(String p : participants){
                if(!Objects.equals(p, sender)){
                    if(newClock.getClock().get(p)>roomClock.getClock().get(p)){
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

    public void removeFromBuffer(Message message){
        for(int i=0; i<bufferedMessages.size();i++){
            Message msg = bufferedMessages.get(i);
            if(message.getContent().equals(msg.getContent())){
                bufferedMessages.remove(msg);
                break;
            }
        }
    }

    public boolean isAbsent(String message){
        for (Message msg : bufferedMessages) {
            if (msg.getContent().equals(message)) {
                return false;
            }
        }
        return true;

    }

    //there's an issue if messages are eliminated
    public void checkMessages(){
        for (Message msg : bufferedMessages) {
            if (computeVectorClock(msg))
                break;
        }

    }

    public void addMessage(Message msg){
        roomMessages.add(msg);
        roomClock.increment(msg.getSender()); //not sure about this but i guess it's the same as doing the merge since we must have received all the messages sent before
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        sdf.setTimeZone(TimeZone.getDefault());
        Calendar calendar = Calendar.getInstance();
        String formattedTime = sdf.format(calendar.getTime());
        System.out.println("[" + formattedTime + "] [" + msg.getRoom() + "] [" + msg.getSender() + "]: " + msg.getContent());
        checkMessages();
    }

    public synchronized List<Message> getRoomMessages() {
        return roomMessages;
    }

    public synchronized VectorClock getRoomClock() {
        return roomClock;
    }

    public synchronized List<Message> getBufferedMessages(){
        return bufferedMessages;
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

    public void insertInBuffer(Message msg){
        bufferedMessages.add(msg);
    }

    public String getLastWriter(){
        return roomMessages.get(roomMessages.size()-1).getSender();
    }

    public String getRoomName() {
        return roomName;
    }
}