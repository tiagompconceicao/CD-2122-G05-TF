package simulator;

public class SensorSimulator {

    public static void main(String[] args) {
        int simulatorMode = 0;
        String brokerIP = "";
        int numOfEvents = 0;
        Simulator simulator;
        switch (args.length){
            case 3:
                numOfEvents =  Integer.parseInt(args[2]);
            case 2:
                brokerIP = args[1];
            case 1:
                simulatorMode = Integer.parseInt(args[0]);
                break;
        }

        //Mode 1 means automatic mode, whatever other value means manual mode
        if (simulatorMode == 1){
            if (!brokerIP.equals("")){
                if (numOfEvents != 0){
                    simulator = new AutomaticSimulator(brokerIP,numOfEvents);
                } else {
                    simulator = new AutomaticSimulator(brokerIP);
                }
            } else {
                simulator = new AutomaticSimulator();
            }
        } else {
            if (!brokerIP.equals("")){
                simulator = new ManualSimulator(brokerIP);
            } else {
                simulator = new ManualSimulator();
            }
        }

        simulator.simulate();


    }
}
