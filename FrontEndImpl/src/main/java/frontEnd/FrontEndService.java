package frontEnd;

import com.google.protobuf.Empty;
import events.ConsumersInfo;
import events.EventDateInfo;
import events.EventInfo;
import events.EventsGrpc;
import groupCommunication.GroupMember;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.StreamObserver;
import model.Event;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

public class FrontEndService extends EventsGrpc.EventsImplBase {

    private final GroupMember groupMember;
    private static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public FrontEndService(GroupMember groupMember){
        this.groupMember = groupMember;
    }

    @Override
    public void getConsumersNumber(Empty request, StreamObserver<ConsumersInfo> streamObserver){
        int consumersNumber = groupMember.getConsumersNumber();
        ConsumersInfo consumersInfo = ConsumersInfo.newBuilder().setConsumersNumber(consumersNumber).build();

        streamObserver.onNext(consumersInfo);
        streamObserver.onCompleted();
    }

    @Override
    public void getEventAverageBetweenDates(EventDateInfo eventDateInfo, StreamObserver<EventInfo> streamObserver){

        ArrayList<Event> events = groupMember.getEventList();
        int speedSum = 0;
        int eventsCount = 0;
        LocalDateTime beginDate;
        LocalDateTime endDate;

        try{
            //Verification of the input, in case of exception, the client didn't input properly
            beginDate = LocalDateTime.parse(eventDateInfo.getBeginDate(), dateFormat);
            endDate = LocalDateTime.parse(eventDateInfo.getEndDate(), dateFormat);
        } catch (DateTimeParseException exception){
            Throwable th=new StatusException(
                    Status.INVALID_ARGUMENT.withDescription("Invalid date")
            );
            streamObserver.onError(th);
            return;
        }

        //If begin date is greater than end date, than end date become begin date and so on.
        if (beginDate.compareTo(endDate) > 0){
            LocalDateTime aux = beginDate;
            beginDate = endDate;
            endDate = aux;
        }

        LocalDateTime eventDate;
        //Event iteration, check if date is in bounds and sum to the total speed buffer
        for (Event event: events) {
            eventDate = LocalDateTime.parse(event.getDate(), dateFormat);
            if (eventDate.compareTo(beginDate) > 0 & eventDate.compareTo(endDate) < 0) {
                speedSum += event.getSpeed();
                eventsCount++;
            }
        }
        int averageSpeed = 0;

        //Verification of total speed value to avoid division by 0
        if (speedSum != 0) averageSpeed = speedSum/eventsCount;
        EventInfo eventInfo = EventInfo.newBuilder().setSpeed(averageSpeed).build();
        streamObserver.onNext(eventInfo);
        streamObserver.onCompleted();
    }

    @Override
    public void getHighestSpeedEvent(Empty request, StreamObserver<EventInfo> eventStreamObserver){
        ArrayList<Event> events = groupMember.getEventList();
        float maxSpeed = 0;
        for (Event event: events) {
            float eventSpeed = event.getSpeed();
            if (eventSpeed > maxSpeed){
                maxSpeed = eventSpeed;
            }
        }

        EventInfo eventInfo = EventInfo.newBuilder().setSpeed(maxSpeed).build();
        eventStreamObserver.onNext(eventInfo);
        eventStreamObserver.onCompleted();
    }
}
