package model;

import java.time.LocalDateTime;

public class Event {
    private final int sid;
    private final String local;
    private final String date;
    private final int speed;

    public Event(int sid, String local, String date, int speed){
        this.sid = sid;
        this.local = local;
        this.date = date;
        this.speed = speed;
    }

    public int getSid(){
        return this.sid;
    }

    public String getLocal() {
        return this.local;
    }

    public String getDate() {
        return this.date;
    }

    public int getSpeed() {
        return this.speed;
    }
}
