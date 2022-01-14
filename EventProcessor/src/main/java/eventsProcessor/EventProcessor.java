package eventsProcessor;

import com.google.gson.Gson;
import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import groupCommunication.GroupMember;
import model.Event;
import model.Group;
import model.Message;
import model.MessageType;
import spread.SpreadException;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class EventProcessor {

    private static String IP_BROKER="34.91.220.227";
    private static String daemonIP="34.65.7.229";
    private static final String queueName = "EventsToEPG";
    private static final int brokerPort = 5672;
    private static final int daemonPort = 4803;

    public static void main(String[] args) {
        int userID = 0;
        switch (args.length){
            case 3:
                daemonIP = args[2];
            case 2:
                IP_BROKER = args[1];
            case 1:
                userID = Integer.parseInt(args[0]);
                break;
        }

        try {

            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(IP_BROKER);
            factory.setPort(brokerPort);

            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            Scanner scan = new Scanner(System.in);
            Gson gson = new Gson();

            GroupMember member = new GroupMember(userID, daemonIP, daemonPort, Group.EPG.name());

            // Consumer handler to receive messages, check the speed value and finally send to Front-End Group
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {

                String recMessage = new String(delivery.getBody(), StandardCharsets.UTF_8);
                Event event = gson.fromJson(recMessage, Event.class);

                if (event.getSpeed() >= 120){
                    //Send to Front-End Group
                    try {
                        Message message = new Message(MessageType.Event,gson.toJson(event));
                        member.sendMessage("FE",gson.toJson(message));
                    } catch (SpreadException e) {
                        e.printStackTrace();
                    }

                    System.out.println(event.getDate()+":"+event.getSid()+":"+event.getLocal()+":"+event.getSpeed());
                }
            };

            // Consumer handler to receive cancel receiving messages
            CancelCallback cancelCallback=(consumerTag)-> System.out.println("CANCEL Received! "+consumerTag);

            String consumerTag = channel.basicConsume(queueName, true, deliverCallback, cancelCallback);
            System.out.println("Waiting for events");

            do {
                System.out.println("Enter 'exit' to finish");
            }while(!scan.nextLine().equals("exit"));

            channel.basicCancel(consumerTag);
            channel.close();
            connection.close();
            member.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
