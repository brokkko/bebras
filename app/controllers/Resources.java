package controllers;

import controllers.actions.Authenticated;
import controllers.actions.DcesController;
import controllers.actions.LoadEvent;
import models.Event;
import models.ServerConfiguration;
import play.Logger;
import play.cache.Cache;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.*;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.util.concurrent.Callable;

public class Resources extends Controller {

    @DcesController(allowCache = true)
    public static Result returnFile(String file) throws IOException {
        String cacheKey = "resource-file-" + file;

        final String decodedFile = URLDecoder.decode(file, "UTF-8");

        byte[] content = null;
        try {
            content = Cache.getOrElse(cacheKey, new Callable<byte[]>() {
                @Override
                public byte[] call() throws Exception {
                    File resource = ServerConfiguration.getInstance().getResource(decodedFile);

                    try {
                        return Files.readAllBytes(resource.toPath());
                    } catch (Exception e) {
                        Logger.warn("Failed to read resource " + decodedFile, e);
                        return null;
                    }
                }
            }, 30 * 60);  //30 minutes
        } catch (Exception ignored) {
        }

        if (content == null)
            return notFound();

        String contentType = determineContentType(file);
        return ok(content).as(contentType);
    }

    @DcesController(allowCache = true)
    public static Result returnResource(final String file, final String base) throws IOException {
        InputStream resource = Application.class.getResourceAsStream(base + "/" + file);

        if (resource == null)
            return notFound();

        String content = determineContentType(file);

        return ok(resource).as(content);
    }

    @Authenticated(admin = true)
    @LoadEvent
    @DcesController(allowCache = false)
    public static Result returnDataFile(String eventId, String file) throws UnsupportedEncodingException {
        File content = convertUrlToFilePath(file);

        if (!content.exists())
            return notFound();

        return ok(content);
    }

    @Authenticated(admin = true)
    @LoadEvent
    @DcesController(allowCache = false)
    public static Result returnDataFileInline(String eventId, String file) throws UnsupportedEncodingException {
        File content = convertUrlToFilePath(file);

        if (!content.exists())
            return notFound();

        return ok(content, true);
    }

    private static File convertUrlToFilePath(String file) throws UnsupportedEncodingException {
        file = URLDecoder.decode(file, "UTF-8");

        return new File(Event.current().getEventDataFolder().getAbsolutePath() + "/" + file);
    }

    @DcesController(allowCache = true)
    public static Result returnPluginFile(String pluginName, String file) throws UnsupportedEncodingException {
        file = URLDecoder.decode(file, "UTF-8");
        if (!file.matches("[a-zA-Z0-9._/-]+"))
            return forbidden();

        File pluginFolder = ServerConfiguration.getInstance().getPluginFolder(pluginName);
        File content = new File(pluginFolder.getAbsolutePath() + '/' + file);

        if (!content.exists())
            return notFound();

        return ok(content);
    }

    private static String determineContentType(String fileName) {
        fileName = fileName.toLowerCase();

        String content = "text/plain";

        if (fileName.endsWith(".html"))
            content = "text/html";
        else if (fileName.endsWith(".css"))
            content = "text/css";
        else if (fileName.endsWith(".js"))
            content = "text/javascript";
        else if (fileName.endsWith(".png"))
            content = "image/png";
        else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg"))
            content = "image/jpeg";
        else if (fileName.endsWith(".doc"))
            content = "application/msword";
        else if (fileName.endsWith(".csv"))
            content = "text/csv";
        else if (fileName.endsWith(".pdf"))
            content = "application/pdf";
        else if (fileName.endsWith(".log") || fileName.endsWith(".txt"))
            content = "text/plain";

        return content;
    }

}
