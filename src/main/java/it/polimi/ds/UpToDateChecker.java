package it.polimi.ds;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class UpToDateChecker {
    private final Timer checkTimer = new Timer();
    private final Random random = new Random();
    private final Client client;
    private static final int RESEND_INTERVAL_MS = 3000;

    public UpToDateChecker(Client client) {
        this.client = client;
    }

    public void startCheckingTimer() {
        checkTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                for(Room room : client.getRooms().values()) {
                    String randomParticipant = getRandomParticipant(room, client.getUsername());
                    client.createMessage(room.getRoomName(), "Resend", randomParticipant);
                }
            }
        }, 0, RESEND_INTERVAL_MS);
    }

    public String getRandomParticipant(Room room, String excludeParticipant) {
        List<String> participants = new ArrayList<>(room.getParticipants());
        participants.removeIf(p -> p.equals(excludeParticipant));
        
        return participants.isEmpty() ? null : participants.get(random.nextInt(participants.size()));
    }

    public void stop() {
        checkTimer.cancel();
    }
}