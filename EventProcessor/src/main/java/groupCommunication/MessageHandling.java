package groupCommunication;

import com.google.gson.Gson;
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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class MessageHandling implements BasicMessageListener {
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
            //Messages sent by group members or front end coordinator
            if(spreadMessage.isRegular()) {
                String body = new String(spreadMessage.getData(), StandardCharsets.UTF_8);
                Message message = gson.fromJson(body,Message.class);
                switch (message.getType()){
                    //Request sent from the Front-End group
                    case Get_Consumers:
                        if (groupMember.isCoordinator()){
                            int consumersNumber = groupMember.getConsumersNumber();
                            sendConsumersNumberToFrontEnd(consumersNumber);
                        }
                        break;
                    //Notification of the coordinator to new members
                    case Coordinator:
                        groupMember.setCoordinatorID(Integer.parseInt(message.getValue()));
                        break;
                }

            }
            //Messages sent by daemon
            if ( spreadMessage.isMembership()) {
                SpreadGroup[] members = spreadMessage.getMembershipInfo().getMembers();
                MembershipInfo info = spreadMessage.getMembershipInfo();

                if(info.isCausedByJoin()) {
                    int joined = getIDFromMemberName(info.getJoined().toString());
                    if (members.length == 1) {
                        //If the member is the first in the group, then is leader
                        groupMember.setCoordinatorID(getIDFromMemberName(info.getJoined().toString()));
                        groupMember.addMember(joined);
                    } else if(groupMember.getConsumersNumber() == 0) {
                        //If there is already other members in the group, update the list of members
                        for (SpreadGroup member : members) {
                            groupMember.addMember(getIDFromMemberName(member.toString()));
                        }
                    } else {
                        //in this case, the actual member was already on the group, that means that another member joined
                        groupMember.addMember(joined);
                    }

                    //The coordinator will notify the Front-End group and update the state of the new member
                    if (groupMember.isCoordinator()) {
                        notifyFrontEndIncrementation();
                        updateNewMember(spreadMessage);
                    }
                } else if(info.isCausedByLeave() | info.isCausedByDisconnect()) {
                    String memberName = info.isCausedByLeave() ? info.getLeft().toString() : info.getDisconnected().toString();
                    int memberID = getIDFromMemberName(memberName);

                    groupMember.removeMember(memberID);
                    //In case that the coordinator left, new election is required
                    if (memberID == groupMember.getCoordinatorID()) {
                        //New election
                        groupMember.chooseNewCoordinator();
                        System.out.println("New coordinator is: " + groupMember.getCoordinatorID());
                    }

                    //The Coordinator notifies the Front-End group about the disconnection of a member
                    if (groupMember.isCoordinator()) {
                        notifyFrontEndDecrementation();
                    }

                }

            }
            PrintMessages.MessageDetails(spreadMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Send message to Front-End group with a notification of incrementation of the consumers number
    private void notifyFrontEndIncrementation() throws SpreadException {
        Message message = new Message(MessageType.Notification,"Increment");
        groupMember.sendMessage(Group.FE.name(),gson.toJson(message));
    }

    //Send message to Front-End group with a notification of decrementation of the consumers number
    private void notifyFrontEndDecrementation() throws SpreadException {
        Message message = new Message(MessageType.Notification, "Decrement");
        groupMember.sendMessage(Group.FE.name(),gson.toJson(message));
    }

    //Send message to the Front-End group with the number of consumers online
    private void sendConsumersNumberToFrontEnd(int consumers) throws SpreadException {
        Message message = new Message(MessageType.Consumers,String.valueOf(consumers));
        groupMember.sendMessage(Group.FE.name(),gson.toJson(message));
    }

    //Get the numerical unique identifier fo a member from his name
    private int getIDFromMemberName(String memberName){
        String[] names = memberName.split("#");
        return Integer.parseInt(names[1]);
    }

    //Update the state of the new member sending him the ID of the coordinator
    private void updateNewMember(SpreadMessage spreadMessage) throws SpreadException {
        SpreadGroup myPrivateGroup = connection.getPrivateGroup();
        SpreadGroup senderPrivateGroup = spreadMessage.getMembershipInfo().getJoined();

        if (!myPrivateGroup.equals(senderPrivateGroup)) {
            int coordinatorID = groupMember.getCoordinatorID();
            Message message = new Message(MessageType.Coordinator,String.valueOf(coordinatorID));
            SpreadMessage msg = new SpreadMessage();
            msg.setSafe();
            msg.addGroup(senderPrivateGroup.toString());
            msg.setData(gson.toJson(message).getBytes());
            connection.multicast(msg);
        }
    }
}

