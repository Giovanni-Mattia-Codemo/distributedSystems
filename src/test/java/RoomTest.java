import it.polimi.ds.Message;
import it.polimi.ds.Room;
import it.polimi.ds.VectorClock;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomTest {
    @Test
    public void testRoom(){
        List<String> users = new ArrayList<>();
        users.add("Gio");
        users.add("Fede");
        Room room = new Room("boh","Gio",users);

        Map<String, Integer> map = new HashMap<>();
        map.put("Gio", 0);
        map.put("Fede", 1);
        VectorClock clock = new VectorClock(map);

        Message msg = new Message("Message","Fede","Come va?",users, clock, "Boh");

        Map<String, Integer> map1 = new HashMap<>();
        map.put("Gio", 1);
        map.put("Fede", 1);
        VectorClock clock1 = new VectorClock(map1);

        Message msg1 = new Message("Message","Gio","Bene",users, clock1, "Boh");

        room.insertInBuffer(msg);
        room.insertInBuffer(msg1);

        room.removeFromBuffer(msg1);

        //didn't crash... good enough for me

    }

}
