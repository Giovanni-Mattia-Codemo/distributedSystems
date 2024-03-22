package it.polimi.ds;

public class BufferChecker implements Runnable {
    private Room room;
    private String savedMessage;
    private Client client;

    public BufferChecker(Client client, Room room, String savedMessage){
        this.client = client;
        this.room = room;
        this.savedMessage = savedMessage;
    }

    /**
     * When an object implementing interface {@code Runnable} is used
     * to create a thread, starting the thread causes the object's
     * {@code run} method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method {@code run} is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        try{
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(!room.getBufferedMessages().isEmpty()){
            if(savedMessage.equals(room.getBufferedMessages().get(0).getContent())){
                String name = room.getLastWriter();
                client.createMessage(room.getRoomName(), "Resend", name);
            }
        }
    }
}