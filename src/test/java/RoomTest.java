import it.polimi.ds.Message;
import it.polimi.ds.Room;
import it.polimi.ds.VectorClock;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

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
        Room room = new Room(users);

        Map<String, Integer> map = new HashMap<>();
        map.put("Gio", 0);
        map.put("Fede", 1);
        VectorClock clock = new VectorClock(map);

        Message msg = new Message("Message","Fede","Coglione",users, clock, "Boh");

        Map<String, Integer> map1 = new HashMap<>();
        map.put("Gio", 1);
        map.put("Fede", 1);
        VectorClock clock1 = new VectorClock(map1);

        Message msg1 = new Message("Message","Fede","Lo so",users, clock1, "Boh");

        room.insertMessage(msg);
        room.insertMessage(msg1);

        room.removeFromBuffer(msg1);

        //didn't crash... good enough for me

    }
}
