package simulator;

import data.EventGenerator;

public class AutomaticSimulator extends Simulator {

    private int eventsNumber = 10000;

    public AutomaticSimulator(String brokerIP, int eventsNumber){
        super();
        setIP_BROKER(brokerIP);
        this.eventsNumber = eventsNumber;
    }

    public AutomaticSimulator(String brokerIP){
        super();
        setIP_BROKER(brokerIP);
    }

    public AutomaticSimulator(){
        super();
    }

    public void simulate(){
        try {
            this.openConnection();
            EventGenerator eventGenerator = new EventGenerator();

            //Send automatically a number eventNumber of events to the second level
            for (int i=0; i<this.eventsNumber;i++){
                this.sendEvent(eventGenerator.generate());
            }

            this.closeConnection();

        } catch (Exception ex) {
            ex.printStackTrace();

        }
    }

}
