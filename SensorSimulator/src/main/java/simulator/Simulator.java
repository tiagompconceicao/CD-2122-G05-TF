package simulator;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import model.Event;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public abstract class Simulator {

    private String IP_BROKER="34.91.85.35";
    private final String exchangeName = "Events";
    private final String routingKey = "";
    Channel channel;
    Connection connection;

    public void openConnection() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(IP_BROKER);
        factory.setPort(5672);

        this.connection = factory.newConnection();
        this.channel = connection.createChannel();
    }
    public  void closeConnection() throws IOException, TimeoutException {
        channel.close();
        connection.close();

    }
    public void sendEvent(Event event) throws IOException {
        if (channel == null){
            return;
        }
        Gson gson = new Gson();

        channel.basicPublish(exchangeName, routingKey, null, gson.toJson(event).getBytes());
    }

    public void setIP_BROKER(String newIp){
        this.IP_BROKER = newIp;
    }

    abstract public void simulate();
}
