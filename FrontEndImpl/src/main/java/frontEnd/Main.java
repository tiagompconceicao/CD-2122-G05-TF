package frontEnd;

import groupCommunication.GroupMember;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import model.Group;
import java.util.logging.Logger;

public class Main {

    private static final Logger logger = Logger.getLogger(Main.class.getName());
    private static String daemonIP="34.65.7.229";
    private static final int daemonPort=4803;
    private static int serverPort = 9000;

    public static void main(String[] args) {
        int userID = 0;
        switch (args.length){
            case 3:
                serverPort = Integer.parseInt(args[2]);
            case 2:
                daemonIP = args[1];
            case 1:
                userID = Integer.parseInt(args[0]);
                break;
        }

        GroupMember member = new GroupMember(userID, daemonIP, daemonPort, Group.FE.name());

        try {
            final Server svc = ServerBuilder.forPort(serverPort)
                    .addService(new FrontEndService(member))
                    .build()
                    .start();
            logger.info("Front-End Server started, listening on " + serverPort);

            svc.awaitTermination();
            member.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
