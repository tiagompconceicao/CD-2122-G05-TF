import com.google.protobuf.Empty;
import events.ConsumersInfo;
import events.EventDateInfo;
import events.EventInfo;
import events.EventsGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.Scanner;

public class EventsUser {

    private static String frontEndIP = "34.65.7.229";
    private static int serverPort = 9000;

    public static void main(String[] args) {
        ManagedChannel channel;
        switch (args.length){
            case 2:
                serverPort = Integer.parseInt(args[1]);
            case 1:
                frontEndIP = args[0];
                break;
        }

        channel = ManagedChannelBuilder.forAddress(frontEndIP, serverPort)
                .usePlaintext()
                .build();
        EventsGrpc.EventsBlockingStub frontEndStub = EventsGrpc.newBlockingStub(channel);

        EventInfo eventInfo;
        boolean end=false;
        while (!end) {
            int option = Menu();
            switch (option) {
                case 1: // average speed
                    try {
                        getAverageSpeed(frontEndStub);
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                    }
                    break;
                case 2 : // highest speed
                    eventInfo = frontEndStub.getHighestSpeedEvent(Empty.newBuilder().build());
                    System.out.println("The highest speed is: " + eventInfo.getSpeed());
                    break;
                case 3 : // number of consumers
                    ConsumersInfo consumersInfo = frontEndStub.getConsumersNumber(Empty.newBuilder().build());
                    System.out.println("The number of active consumers is: " + consumersInfo.getConsumersNumber());
                    break;
                case 99:
                    end = true;
                    break;
            }
        }
    }

    private static void getAverageSpeed(EventsGrpc.EventsBlockingStub frontEndStub) {
        Scanner scan = new Scanner(System.in);
        String firstDate = read("Introduce the begin date (dd-mm-yyyy HH:MM:SS)", scan);
        String secondDate = read("Introduce the end date (dd-mm-yyyy HH:MM:SS)", scan);
        EventDateInfo dateInfo = EventDateInfo.newBuilder().setBeginDate(firstDate).setEndDate(secondDate).build();
        EventInfo eventInfo = frontEndStub.getEventAverageBetweenDates(dateInfo);
        System.out.println("The average speed in the given range is: " + eventInfo.getSpeed());
    }

    private  static int Menu() {
        int op;
        Scanner scan = new Scanner(System.in);
        do {
            System.out.println();
            System.out.println("    MENU");
            System.out.println(" 1 - Get average speed in dates");
            System.out.println(" 2 - Get Highest speed registered");
            System.out.println(" 3 - Get number of consumers");
            System.out.println("99 - Exit");
            System.out.println();
            System.out.println("Choose an Option?");
            op = scan.nextInt();
        } while (!((op >= 1 && op <= 3) || op == 99));
        return op;
    }

    private static String read(String msg, Scanner scanInput) {
        System.out.println(msg);
        return scanInput.nextLine();
    }
}
