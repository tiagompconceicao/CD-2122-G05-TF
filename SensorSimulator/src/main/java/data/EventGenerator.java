package data;

import model.Event;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.PrimitiveIterator;
import java.util.Random;

public class EventGenerator {
    private final String[] locals = new String[]{"Belem", "Rossio", "Cascais", "Ponte Vasco da Gama","Avenida da Liberdade","Ponte 25 de Abril"};
    private final int minSpeed = 30;
    private final int maxSpeed = 150;
    private final PrimitiveIterator.OfInt numberGenerator;
    private final PrimitiveIterator.OfInt speedGenerator;
    DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    public EventGenerator(){
        Random randomizer = new Random();
        numberGenerator = randomizer.ints(0, locals.length).iterator();
        speedGenerator = randomizer.ints(minSpeed,maxSpeed).iterator();
    }

    //Generate a pseudo-random event
    public Event generate(){
        LocalDateTime myDateObj = LocalDateTime.now();
        int localIndex = numberGenerator.nextInt();
        return new Event(
                localIndex,locals[localIndex],
                myDateObj.format(myFormatObj),
                speedGenerator.nextInt()
        );
    }
}
