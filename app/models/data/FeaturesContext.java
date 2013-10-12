package models.data;

import models.Event;
import play.mvc.Call;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 20.09.13
 * Time: 22:40
 */
public class FeaturesContext {

    private Event event;
    private FeaturesContestType type;
    private Call currentCall;

    public FeaturesContext(Event event, FeaturesContestType type, Call currentCall) {
        this.event = event;
        this.type = type;
        this.currentCall = currentCall;
    }

    public Event getEvent() {
        return event;
    }

    public FeaturesContestType getType() {
        return type;
    }

    public Call getCurrentCall() {
        return currentCall;
    }
}
