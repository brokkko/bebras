package models.data;

import models.Event;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 20.09.13
 * Time: 22:40
 */
public class FeaturesContext {

    private Event event;

    private boolean screen;

    public FeaturesContext(Event event, boolean screen) {
        this.event = event;
        this.screen = screen;
    }

    public Event getEvent() {
        return event;
    }

    public boolean isScreen() {
        return screen;
    }
}
