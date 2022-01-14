package logger;

import com.google.gson.Gson;
import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import model.Event;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Logger {

    private static String IP_BROKER="34.141.248.164";
    private static final String queueName = "EventsToLog";
    private static final String fileName = "log.txt";
    private static final int PORT_BROKER = 5672;

    public static void main(String[] args) {
        try {
            if(args.length != 0){
                IP_BROKER = args[0];
            }
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(IP_BROKER);
            factory.setPort(PORT_BROKER);

            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            Scanner scan = new Scanner(System.in);
            Gson gson = new Gson();
            FileWriter fileWriter = new FileWriter(fileName, true);
            PrintWriter printWriter = new PrintWriter(fileWriter);

            // Consumer handler to receive messages
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String recMessage = new String(delivery.getBody(), StandardCharsets.UTF_8);
                Event event = gson.fromJson(recMessage, Event.class);
                System.out.println(event.getDate()+":"+event.getSid()+":"+event.getLocal()+":"+event.getSpeed());
                printWriter.println(event.getDate()+":"+event.getSid()+":"+event.getLocal()+":"+event.getSpeed());

            };
            // Consumer handler to receive cancel receiving messages
            CancelCallback cancelCallback=(consumerTag)->{
                System.out.println("CANCEL Received! "+consumerTag);
            };

            String consumerTag =  channel.basicConsume(queueName, true, deliverCallback, cancelCallback);

            System.out.println("Waiting for events");
            do {
                System.out.println("Enter 'exit' to finish");
            }while(!scan.nextLine().equals("exit"));

            channel.basicCancel(consumerTag);
            channel.close();
            connection.close();
            //The new content of the file only is available after the program exit
            printWriter.close();
            fileWriter.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
