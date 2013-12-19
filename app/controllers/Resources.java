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
import java.util.concurrent.Callable;

@DcesController(allowCache = true)
public class Resources extends Controller {

    @DcesController(allowCache = true)
    public static Result returnFile(String file) throws IOException {
//        String s = "abc";
//        for (int i = 0; i < 30000000; i++)
//            s = s.substring(1) + "a";

        String cacheKey = "resource-file-" + file;

        final String decodedFile = URLDecoder.decode(file, "UTF-8");

        byte[] content = null;
        try {
            content = Cache.getOrElse(cacheKey, new Callable<byte[]>() {
                @Override
                public byte[] call() throws Exception {
                    File resource = ServerConfiguration.getInstance().getResource(decodedFile);
                    if (!resource.exists())
                        return null;

                    //read file to byte array
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024 * 8];
                    try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(resource))) {
                        int read;
                        while ((read = in.read(buffer)) > 0)
                            baos.write(buffer, 0, read);
                    } catch (Exception e) {
                        Logger.warn("Failed to read resource " + decodedFile, e);
                        return null;
                    }

                    return baos.toByteArray();
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
        file = URLDecoder.decode(file, "UTF-8");

        File content = new File(Event.current().getEventDataFolder().getAbsolutePath() + "/" + file);

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
