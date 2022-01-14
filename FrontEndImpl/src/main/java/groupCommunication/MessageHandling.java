package groupCommunication;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import model.Event;
import model.Group;
import model.Message;
import model.MessageType;
import spread.BasicMessageListener;
import spread.MembershipInfo;
import spread.SpreadConnection;
import spread.SpreadException;
import spread.SpreadGroup;
import spread.SpreadMessage;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class MessageHandling implements BasicMessageListener {

    private static final int MAX_EVENTS = 1620;
    private final SpreadConnection connection;
    private final GroupMember groupMember;
    private static final Gson gson = new Gson();

    public MessageHandling(SpreadConnection connection, GroupMember groupMember) {
        this.connection = connection;
        this.groupMember = groupMember;
    }

    @Override
    public void messageReceived(SpreadMessage spreadMessage) {
        try {
            //Messages sent by a group member (Event Processing Group or Front-End Group)
            if(spreadMessage.isRegular()) {
                String body = new String(spreadMessage.getData(), StandardCharsets.UTF_8);
                Message message = gson.fromJson(body,Message.class);
                switch (message.getType()){
                    //Event sent from the Event Processing Group
                    case Event:
                        Event event = gson.fromJson(message.getValue(),Event.class);
                        groupMember.addEvent(event);
                        break;

                    //Set of events sent from the coordinator
                    case Events:
                        if (!groupMember.isCoordinator()){
                            Type founderListType = new TypeToken<ArrayList<Event>>(){}.getType();
                            ArrayList<Event> events = gson.fromJson(message.getValue(), founderListType);
                            System.out.println("Received: " + events.size() + " events");
                            groupMember.addEvents(events);
                        }
                        break;
                    //Join or leave notification sent by the Event Processing Group coordinator
                    case Notification:
                        String notification = message.getValue();
                        if (notification.equals("Increment")){
                            groupMember.incrementConsumers();
                        } else if (notification.equals("Decrement")){
                            groupMember.decrementConsumers();
                        }
                        break;
                    case Consumers:
                    //Message sent from the coordinator of FE group or from the EPG group with the number of consumers online
                        int consumers = Integer.parseInt(message.getValue());
                        groupMember.setConsumersNumber(consumers);
                        break;
                    case Coordinator:
                    //Message sent from the coordinator of the group with his own numerical ID
                        if (!groupMember.isCoordinator()){
                            System.out.println("New coordinator is:" + message.getValue());
                            int coordinator = Integer.parseInt(message.getValue());
                            groupMember.setCoordinatorID(coordinator);
                        }
                        break;
                }
            }

            //Internal group events sent by daemon
            if (spreadMessage.isMembership()) {
                SpreadGroup[] members = spreadMessage.getMembershipInfo().getMembers();
                MembershipInfo info = spreadMessage.getMembershipInfo();

                //For joins on the group, numerical ID is added to the array of members and the new member will receive
                // the events,consumers number and coordinator id to the new, unless the joined member is the only one in the group
                //For disconnections and leaves is verified if is the actual coordinator left/disconnected
                //Only if that happen that is chosen a new coordinator
                if(info.isCausedByJoin()) {
                    int joined = getIDFromMemberName(info.getJoined().toString());


                    if (members.length == 1) {
                        //First member entering the group can be considered as coordinator
                        groupMember.addMember(joined);
                        groupMember.setCoordinatorID(joined);
                        //The first member will ask the Event Processing Group the number of consumers online
                        requestConsumersNumber();
                    } else if(groupMember.getMembers().size() == 0) {
                        //New member will get the members list
                        for (SpreadGroup member : members) {
                            groupMember.addMember(getIDFromMemberName(member.toString()));
                        }
                    } else {
                        groupMember.addMember(joined);
                        //Only the coordinator can update the state of new members
                        if (groupMember.isCoordinator()){
                             updateNewMember(spreadMessage);
                        }
                    }
                } else if (info.isCausedByDisconnect() | info.isCausedByLeave()) {
                    String memberName = info.isCausedByLeave() ? info.getLeft().toString() : info.getDisconnected().toString();
                    int memberID = getIDFromMemberName(memberName);
                    groupMember.removeMember(memberID);
                    //In case that the coordinator is no more contactable, a new election is required
                    if (memberID == groupMember.getCoordinatorID()) {
                        groupMember.chooseNewCoordinator();
                        System.out.println("New coordinator is: " + groupMember.getCoordinatorID());
                    }
                }
            }
            PrintMessages.MessageDetails(spreadMessage);
        } catch (SpreadException e) {
            e.printStackTrace();
        }
    }

    //Ask the Event Processing Group how many consumers are alive
    private void requestConsumersNumber() throws SpreadException {
        Message message = new Message(MessageType.Get_Consumers,"");
        groupMember.sendMessage(Group.EPG.name(),gson.toJson(message));
    }

    //Get the numerical unique identifier fo a member from his name
    private int getIDFromMemberName(String memberName){
        String[] names = memberName.split("#");
        return Integer.parseInt(names[1]);
    }

    //Atualizar estado do novo membro para manter o sistema consistente
    private void updateNewMember(SpreadMessage spreadMessage) throws SpreadException {
        SpreadGroup myPrivateGroup = connection.getPrivateGroup();
        SpreadGroup senderPrivateGroup = spreadMessage.getMembershipInfo().getJoined();

        if (!myPrivateGroup.equals(senderPrivateGroup)) {
            SpreadMessage msg = new SpreadMessage();
            msg.setSafe();
            msg.addGroup(senderPrivateGroup.toString());
            Message message;

            //Message with the ID of the group coordinator
            int coordinatorID = groupMember.getCoordinatorID();
            message = new Message(MessageType.Coordinator,String.valueOf(coordinatorID));
            msg.setData(gson.toJson(message).getBytes());
            connection.multicast(msg);

            //Message with the current number of consumers online
            int consumersNumber = groupMember.getConsumersNumber();
            message = new Message(MessageType.Consumers,String.valueOf(consumersNumber));
            msg.setData(gson.toJson(message).getBytes());
            connection.multicast(msg);

            //Message or a set of messages with the events on memory
            ArrayList<Event> events = groupMember.getEventList();
            for (ArrayList<Event> setOfEvents: this.splitEvents(events)) {
                message = new Message(MessageType.Events,gson.toJson(setOfEvents));
                msg.setData(gson.toJson(message).getBytes());
                connection.multicast(msg);
            }

        }
    }

    //Split the ArrayList of Events in a set of Arraylist of Events with max length of MAX_EVENTS
    private ArrayList<ArrayList<Event>> splitEvents(ArrayList<Event> events){
        ArrayList<ArrayList<Event>> list = new ArrayList<>();
        //One single set with all events
        if (events.size() <= MAX_EVENTS){
            list.add(events);
            return list;
        }

        int startIndex = 0;
        int endIndex = MAX_EVENTS;
        ArrayList<Event> aux;
        //Building a ArrayList of ArrayList with MAX_EVENTS length
        for (int i = 0; i < (events.size()/MAX_EVENTS); i++){
            aux = new ArrayList<>();
            for (int j = startIndex; j < endIndex; j++){
                aux.add(events.get(j));
            }
            list.add(aux);
            startIndex = endIndex;
            endIndex += MAX_EVENTS;
        }

        //Catch the rest of elements
        int remaining = (events.size()/MAX_EVENTS);
        if (((float) events.size()/(float) MAX_EVENTS) > remaining){
            aux = new ArrayList<>();
            for (int k = startIndex; k < events.size(); k++){
                aux.add(events.get(k));
            }
            list.add(aux);
        }

        return list;
    }
}

