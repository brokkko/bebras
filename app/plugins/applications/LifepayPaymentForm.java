package plugins.applications;

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
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class LifepayPaymentForm {

    public static final String LIFEPAY_INPUT = "https://partner.life-pay.ru/alba/input/";

    private LifepayPaymentType payment;
    private Applications apps;
    private Application application;
    private User payingUser;
    private User applicationUser;

    public LifepayPaymentForm(LifepayPaymentType payment, Applications apps, Application application, User payingUser, User applicationUser) {
        this.payment = payment;
        this.apps = apps;
        this.application = application;
        this.payingUser = payingUser;
        this.applicationUser = applicationUser;
    }

    public Map<String, String> getFormFields() {
        Map<String, String> result = new HashMap<>();

        result.put("cost", String.valueOf(apps.getApplicationPrice(application)));
        result.put("name", String.format(
                "%s, заявка %s",
                apps.getTypeByName(application.getType()).getDescription(),
                application.getName())
        );
        if (payingUser != null  && payingUser.getEmail() != null && !payingUser.getEmail().endsWith("@autoregistered"))
            result.put("email", payingUser.getEmail());
        result.put("order_id", "0");
        result.put("comment", applicationUser.getId().toHexString() + "::" + apps.getRef() + "::" + application.getName());
        result.put("service_id", payment.getServiceId());
        result.put("version", "2.0");

        try {
            result.put("check", sign("POST", LIFEPAY_INPUT, result, payment.getSecretKey()));
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
        Charset charSet = StandardCharsets.UTF_8;
        SecretKeySpec keySpec = new SecretKeySpec(charSet.encode(secretKey).array(), "HmacSHA256");
        hmacInstance.init(keySpec);

        return DatatypeConverter.printBase64Binary(hmacInstance.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }
}
