package simulator;

import model.Event;
import java.util.Scanner;

public class ManualSimulator extends Simulator {

    public ManualSimulator(String brokerIP){
        super();
        setIP_BROKER(brokerIP);
    }

    public ManualSimulator(){
        super();
    }

    public void simulate(){
        try {
            this.openConnection();

            Scanner scaninput = new Scanner(System.in);
            int sid;
            String local;
            String date;
            int speed;

            System.out.println("Welcome");
            do {
                System.out.println("Introduce sid:");
                sid = Integer.parseInt(scaninput.nextLine());
                System.out.println("Introduce local:");
                local = scaninput.nextLine();
                System.out.println("Introduce date in format (dd-MM-yyyy HH:mm:ss):");
                date = scaninput.nextLine();
                System.out.println("Introduce a speed between 30 and 150:");
                speed = Integer.parseInt(scaninput.nextLine());
                if (speed < 30 || speed > 150){
                    System.out.println("Please input properly");
                    continue;
                }
                this.sendEvent(new Event(sid,local,date,speed));
                System.out.println("Type 'exit' to finish the execution or anything else to continue");
            } while(!scaninput.nextLine().equals("exit"));

            this.closeConnection();

        } catch (Exception ex) {
            ex.printStackTrace();

        }
    }
}
