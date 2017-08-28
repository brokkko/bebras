package plugins.applications;

import models.Event;
import models.User;
import models.applications.Application;
import play.Logger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class RfiPaymentForm {

    public static final String RFI_INPUT = "https://partner.rficb.ru/alba/input/";

    private Event event;
    private RfiPaymentType payment;
    private Applications apps;
    private Application application;
    private User user;

    public RfiPaymentForm(Event event, RfiPaymentType payment, Applications apps, Application application, User user) {
        this.event = event;
        this.payment = payment;
        this.apps = apps;
        this.application = application;
        this.user = user;
    }

    public Map<String, String> getFormFields() {
        Map<String, String> result = new HashMap<>();

        result.put("cost", String.valueOf(apps.getApplicationPrice(application)));
        result.put("name", String.format(
                "Регистрационный взнос %s, заявка %s",
                apps.getTypeByName(application.getType()).getDescription(),
                application.getName())
        );
        result.put("email", user.getEmail());
        result.put("order_id", user.getId().toHexString() + "::" + apps.getRef() + "::" + application.getName());
        result.put("comment", "application " + application.getName());
        result.put("service_id", payment.getServiceId());
        result.put("version", "2.0");

        try {
            result.put("check", sign("POST", RFI_INPUT, result, payment.getSecretKey()));
            Logger.info("secret key = " + payment.getSecretKey());
        } catch (Exception e) {
            Logger.error("Failed to sign request: " + result, e);
        }

        return result;
    }

    //https://lib.rfibank.ru/pages/viewpage.action?pageId=885399
    private String sign(String method, String url, Map<String, String> params, String secretKey)
            throws URISyntaxException, UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {

        URI uri = new URI(url);

        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);

        StringBuilder sb = new StringBuilder();
        for (String key: keys) {
            if (sb.length() > 0) {
                sb.append("&");
            }

            sb.append(String.format("%s=%s",
                    key,
                    URLEncoder.encode(params.get(key), "UTF-8").replace("+", "%20")
            ));
        }
        String urlParameters = sb.toString();
        String data = method.toUpperCase() + "\n" +
                uri.getHost() + "\n" +
                uri.getPath() + "\n" +
                urlParameters;

        Mac hmacInstance = Mac.getInstance("HmacSHA256");
        Charset charSet = Charset.forName("UTF-8");
        SecretKeySpec keySpec = new javax.crypto.spec.SecretKeySpec(charSet.encode(secretKey).array(), "HmacSHA256");
        hmacInstance.init(keySpec);

        return DatatypeConverter.printBase64Binary(hmacInstance.doFinal(data.getBytes("UTF-8")));
    }
}
