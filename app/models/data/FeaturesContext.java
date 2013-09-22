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
    private boolean screen;
    private Call currentCall;

    public FeaturesContext(Event event, boolean screen, Call currentCall) {
        this.event = event;
        this.screen = screen;
        this.currentCall = currentCall;
    }

    public Event getEvent() {
        return event;
    }

    public boolean isScreen() {
        return screen;
    }

    public Call getCurrentCall() {
        return currentCall;
    }
}
