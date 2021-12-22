package plugins.applications;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.WriteConcern;
import controllers.MongoConnection;
import models.Event;
import models.User;
import models.applications.Application;
import models.forms.RawForm;
import org.bson.types.ObjectId;
import play.Logger;
import plugins.Plugin;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RfiResponseForm {

    public static final String CAN_NOT_CHECK_SIGNATURE = "not enough information to check signature";
    //https://lib.rfibank.ru/pages/viewpage.action?pageId=885370
    protected String tid;
    protected String name;
    protected String comment;
    protected String partner_id;
    protected String service_id;
    protected String order_id;
    protected String type;
    protected String currency;
    protected String partner_income;
    protected String system_income;
    protected String test = "";
    protected String check;

    private Event event;
    private User user;
    private Applications apps;
    private String applicationName;
    private Application application;
    private RawForm rawForm;
    private String method;
    private String host;
    private String path;

    private BasicDBObject formAsBSON() {
        BasicDBObject o = new BasicDBObject();

        o.put("tid", tid);
        o.put("name", name);
        o.put("comment", comment);
        o.put("partner_id", partner_id);
        o.put("service_id", service_id);
        o.put("order_id", order_id);
        o.put("type", type);
        o.put("currency", currency);
        o.put("partner_income", partner_income);
        o.put("system_income", system_income);
        o.put("test", test);
        o.put("check", check);

        return o;
    }

    public void serialize() {
        DBCollection col = MongoConnection.getRfiLogCollection();
        col.insert(formAsBSON(), WriteConcern.MAJORITY); //TODO write concern
    }

    public String toString() {
        return String.format("RfiResponseForm[%s;%s;%s;%s;%s]",
                formAsBSON(),
                rawForm,
                method,
                host,
                path
        );
    }

    public void parseOrderInformation() {
        if (comment == null)
            throw new IllegalArgumentException("no order information specified");

        String[] parts = comment.split("::");
        if (parts.length != 3)
            throw new IllegalArgumentException("order id does not have 3 parts: " + order_id);
        String userId = parts[0];
        String ref = parts[1];
        applicationName = parts[2];

        user = User.getUserById(new ObjectId(userId));
        if (user == null)
            throw new IllegalArgumentException("unknown user id " + userId);

        event = user.getEvent();

        Plugin plugin = event.getPlugin(ref);
        if (plugin == null)
            throw new IllegalArgumentException("unknown plugin ref " + ref);
        if (!(plugin instanceof Applications))
            throw new IllegalArgumentException("plugin has a wrong type " + plugin.getClass());

        apps = (Applications) plugin;
        application = apps.getApplicationByName(applicationName, user);
        if (application == null)
            throw new IllegalArgumentException("unknown application name");
    }

    public void checkSignature() {
        boolean oldCheckOk = true;
        try {
            checkSignatureRfi();
        } catch (IllegalArgumentException e) {
            oldCheckOk = false;
        }
        boolean newCheckOk = true;
        try {
            checkSignatureLifePay();
        } catch (IllegalArgumentException e) {
            newCheckOk = false;
        }
        Logger.info("checking request, old: " + oldCheckOk + ", new: " + newCheckOk + ": " + this);
    }

    public void checkSignatureLifePay() {
        //search for RFI payment type
        if (apps == null)
            throw new IllegalArgumentException(CAN_NOT_CHECK_SIGNATURE);
        List<PaymentType> paymentTypes = apps.getPaymentTypes();
        RfiPaymentType pay = null;
        for (PaymentType paymentType : paymentTypes)
            if (paymentType instanceof RfiPaymentType) {
                pay = (RfiPaymentType) paymentType;
                break;
            }
        if (pay == null)
            throw new IllegalArgumentException(CAN_NOT_CHECK_SIGNATURE);

        try {
            List<String> keys = new ArrayList<>(rawForm.keys());
            Collections.sort(keys);

            StringBuilder sb = new StringBuilder();
            for (String key : keys) {
                if (sb.length() > 0) {
                    sb.append("&");
                }

                final String encoded = URLEncoder
                        .encode(rawForm.get(key), "UTF-8")
                        .replace("+", "%20");
                sb.append(String.format("%s=%s", key, encoded));
            }
            String urlParameters = sb.toString();
            String data = method.toUpperCase() + "\n" +
                    host + "\n" +
                    path + "\n" +
                    urlParameters;

            Mac hmacInstance = Mac.getInstance("HmacSHA256");
            Charset charSet = StandardCharsets.UTF_8;
            SecretKeySpec keySpec = new javax.crypto.spec.SecretKeySpec(charSet.encode(pay.getSecretKey()).array(), "HmacSHA256");
            hmacInstance.init(keySpec);

            String md5 = DatatypeConverter.printBase64Binary(hmacInstance.doFinal(data.getBytes(charSet)));

            if (!md5.equalsIgnoreCase(check)) {
                throw new IllegalArgumentException("wrong check");
            }
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException | InvalidKeyException e) {
            //impossible
        }
    }

    public void checkSignatureRfi() {
        String concat = getTid() + getName() + getComment()
                + getPartner_id() + getService_id() + getOrder_id() + getType()
                + getPartner_income() + getSystem_income() + getTest();

        //search for RFI payment type
        if (apps == null)
            throw new IllegalArgumentException(CAN_NOT_CHECK_SIGNATURE);
        List<PaymentType> paymentTypes = apps.getPaymentTypes();
        RfiPaymentType pay = null;
        for (PaymentType paymentType : paymentTypes)
            if (paymentType instanceof RfiPaymentType) {
                pay = (RfiPaymentType) paymentType;
                break;
            }

        if (pay == null)
            throw new IllegalArgumentException(CAN_NOT_CHECK_SIGNATURE);

        concat += pay.getSecretKey();

        String md5;
        try {
            MessageDigest md5digester = MessageDigest.getInstance("MD5");
            byte[] md5digest = md5digester.digest(concat.getBytes("UTF8"));
            md5 = DatatypeConverter.printHexBinary(md5digest);
        } catch (Exception e) {
            throw new IllegalArgumentException("no such algorithm MD5 or no such encoding UTF8");
        }

        if (!md5.equalsIgnoreCase(check)) {
//            Logger.info(String.format("wrong check: md5(%s) = %s != %s", concat, md5, check));
            throw new IllegalArgumentException("wrong check");
        }
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getPartner_id() {
        return partner_id;
    }

    public void setPartner_id(String partner_id) {
        this.partner_id = partner_id;
    }

    public String getService_id() {
        return service_id;
    }

    public void setService_id(String serviceId) {
        this.service_id = serviceId;
    }

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String orderId) {
        this.order_id = orderId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPartner_income() {
        return partner_income;
    }

    public void setPartner_income(String partnerIncome) {
        this.partner_income = partnerIncome;
    }

    public String getSystem_income() {
        return system_income;
    }

    public void setSystem_income(String systemIncome) {
        this.system_income = systemIncome;
    }

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

    public String getCheck() {
        return check;
    }

    public void setCheck(String check) {
        this.check = check;
    }

    // ---------------------------- evaluated fields

    public Event getEvent() {
        return event;
    }

    public User getUser() {
        return user;
    }

    public Applications getApps() {
        return apps;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public Application getApplication() {
        return application;
    }

    public void updateWithRequestInfo(RawForm rawForm, String method, String host, String path) {
        this.rawForm = rawForm;
        this.method = method;
        this.host = host;
        this.path = path;
    }

    public RawForm getRawForm() {
        return rawForm;
    }
}
