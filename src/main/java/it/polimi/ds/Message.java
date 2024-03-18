package it.polimi.ds;

import java.io.*;
import java.util.List;

public class Message implements Serializable {

    private static final long serialVersionUID = 1L;
    private final String type;
    private final String sender;
    private final String content;
    private final String room;
    private VectorClock vectorClock;
    private List<String> participants;

    public Message(String type, String sender, String content, List<String> participants, VectorClock vectorClock, String room) {
        this.type = type;
        this.sender = sender;
        this.content = content;
        this.participants = participants;
        this.room = room;
        this.vectorClock = vectorClock;
    }

    public String getRoom() {
        return room;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public String getType(){return type;}

    public String getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

    public VectorClock getVectorClock() {
        return vectorClock;
    }
}
