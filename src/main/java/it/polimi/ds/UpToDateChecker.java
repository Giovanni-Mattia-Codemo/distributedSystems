package it.polimi.ds;

import java.util.Timer;
import java.util.TimerTask;

public class UpToDateChecker {
    private final Timer checkTimer = new Timer();
    private final Client client;
    private static final int RESEND_INTERVAL_MS = 5000;

    public UpToDateChecker(Client client) {
        this.client = client;
    }

    public void startCheckingTimer() {
        checkTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                synchronized (client.getRooms()) {
                    for (Room room : client.getRooms().values()) {
                        client.createMessage(room.getRoomName(), "Resend", null);

                        room.updateMinimumClock();
                        room.removeDeliveredMessagesToAll();
                    }
                }
            }
        }, 0, RESEND_INTERVAL_MS);
    }

    public void stop() {
        try {
            checkTimer.cancel();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }
}