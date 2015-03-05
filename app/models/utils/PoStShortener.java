package models.utils;

import com.fasterxml.jackson.databind.JsonNode;
import play.Logger;
import play.Play;
import play.cache.Cache;
import play.libs.WS;

import java.util.concurrent.Callable;

public class PoStShortener {

    public static String shorten(String url) {

        final String filteredUrl = filter(url);

        try {
            return Cache.getOrElse("po-st-short-" + filteredUrl, new Callable<String>() {
                @Override
                public String call() throws Exception {
                    String apiKey = Play.application().configuration().getString("po.st.api_key");
                    if (apiKey == null || apiKey.isEmpty())
                        return filteredUrl;

                    WS.Response response = WS.url("http://po.st/api/shorten")
                            .setQueryParameter("apiKey", apiKey)
                            .setQueryParameter("longUrl", filteredUrl)
                            .get().get(2000);

                    JsonNode json = response.asJson();
                    String status = json.path("status_txt").asText();
                    if (!status.equals("OK"))
                        throw new Exception("PoSt shortening status " + status);

                    return json.path("short_url").asText();
                }
            }, 0);
        } catch (Exception e) {
            Logger.warn("Failed to shorten link at po.st: " + filteredUrl, e);
            return "Ошибка сокращения";
        }
    }

    private static String filter(String url) {
        return url.replaceAll("//localhost(:\\d+)?", "//bebras.ru");
    }

}
