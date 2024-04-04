package it.polimi.ds;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class Message implements Serializable {

    private static final long serialVersionUID = 1L;
    private final String type;
    private final String sender;
    private final String content;
    private final String room;
    private VectorClock vectorClock;
    private List<String> participants;

    public Message(String type, String sender, String content, List<String> participants, VectorClock vectorClock,
            String room) {
        this.type = type;
        this.sender = sender;
        this.content = content;
        this.participants = participants;
        this.room = room;
        if (vectorClock != null) {
            this.vectorClock = new VectorClock(vectorClock.getClock());
        } else {
            this.vectorClock = vectorClock;
        }
    }

    public String getRoom() {
        return room;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public String getType() {
        return type;
    }

    public String getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

    public VectorClock getVectorClock() {
        return vectorClock;
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        sdf.setTimeZone(TimeZone.getDefault());
        Calendar calendar = Calendar.getInstance();
        String formattedTime = sdf.format(calendar.getTime());
        return "[" + formattedTime + "] [" + room + "] [" + sender + "]: " + content;
    }
}
