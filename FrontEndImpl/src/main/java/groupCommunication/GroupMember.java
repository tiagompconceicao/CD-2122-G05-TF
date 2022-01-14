package groupCommunication;

import model.Event;
import spread.SpreadConnection;
import spread.SpreadException;
import spread.SpreadGroup;
import spread.SpreadMessage;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class GroupMember {

    private SpreadConnection connection;
    private SpreadGroup group;

    private final int userID;
    private int coordinatorID;

    private final ArrayList<Integer> members;
    private final ArrayList<Event> eventList;
    private int consumersNumber;

    private MessageHandling msgHandling;
    public GroupMember(int userID, String address, int port, String groupName) {
        this.coordinatorID = -1;
        this.consumersNumber = 0;
        this.userID = userID;
        this.members = new ArrayList<>();
        this.eventList = new ArrayList<>();

        //Establish the spread connection.
        try  {
            connection = new SpreadConnection();
            connection.connect(InetAddress.getByName(address), port, String.valueOf(userID), false, true);
            msgHandling = new MessageHandling(connection, this);
            connection.add(msgHandling);
            group = new SpreadGroup();
            group.join(connection,groupName);
        }
        catch(SpreadException e)  {
            System.err.println("There was an error connecting to the daemon.");
            e.printStackTrace();
            System.exit(1);
        }
        catch(UnknownHostException e) {
            System.err.println("Can't find the daemon " + address);
            System.exit(1);
        }
    }

    //Sent a Spread message to a destiny group
    public void sendMessage(String groupToSend, String txtMessage) throws SpreadException {
        SpreadMessage msg = new SpreadMessage();
        msg.setSafe();
        msg.addGroup(groupToSend);
        msg.setData(txtMessage.getBytes());
        connection.multicast(msg);
    }

    //Close protocol, leave from Spread group, remove message listener and close connection
    public void close() throws SpreadException {
        // Remove consumer from group
        if(group != null) {
            group.leave();
        }
        // Remove listener
        connection.remove(msgHandling);
        // Disconnect.
        connection.disconnect();
    }

    public int getCoordinatorID() {
        return coordinatorID;
    }

    public ArrayList<Event> getEventList() {
        return eventList;
    }

    public int getConsumersNumber() {
        return consumersNumber;
    }

    public ArrayList<Integer> getMembers() {
        return members;
    }

    public void addEvent(Event event){
        eventList.add(event);
    }

    public void addEvents(ArrayList<Event> events){
        if (events.size() == 0) return;
        eventList.addAll(events);
    }

    public void setCoordinatorID(int coordinatorID) {
        this.coordinatorID = coordinatorID;
    }

    public void setConsumersNumber(int consumersNumber) {
        this.consumersNumber = consumersNumber;
    }

    public void incrementConsumers(){
        this.consumersNumber += 1;
    }

    public void decrementConsumers(){
        this.consumersNumber -= 1;
    }

    public boolean isCoordinator(){
        return coordinatorID == userID;
    }

    public void addMember(int userID){
        members.add(userID);
    }

    public void removeMember(int userID){
        members.remove(Integer.valueOf(userID));
    }

    public void chooseNewCoordinator() {
        int bestUser = -1;

        for (int userID: members) {
            System.out.println(userID);
            if (userID > bestUser){
                bestUser = userID;
            }
        }
        setCoordinatorID(bestUser);
    }
}
