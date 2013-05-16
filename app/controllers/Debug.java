package controllers;

import play.Play;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 01.05.13
 * Time: 14:28
 */
public class Debug extends Controller {

    public static Result debug(String action) throws IOException {
        return ok(Play.application().getFile("asdf").getCanonicalPath());
    }
}