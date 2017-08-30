package plugins.applications;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.WriteConcern;
import controllers.MongoConnection;
import models.Event;
import models.User;
import models.applications.Application;
import org.bson.types.ObjectId;
import play.Logger;
import plugins.Plugin;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.util.List;

public class RfiResponseForm {

    public static final String CAN_NOT_CHECK_SIGANTURE = "not enough information to check signature";
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
        return formAsBSON().toString();
    }

    public void parseOrderInformation() {
        if (comment == null)
            throw new IllegalArgumentException("noto order information specified");

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
        String concat = getTid() + getName() + getComment()
                + getPartner_id() + getService_id() + getOrder_id() + getType()
                + getPartner_income() + getSystem_income() + getTest();

        //search for RFI payment type
        if (apps == null)
            throw new IllegalArgumentException(CAN_NOT_CHECK_SIGANTURE);
        List<PaymentType> paymentTypes = apps.getPaymentTypes();
        RfiPaymentType pay = null;
        for (PaymentType paymentType : paymentTypes)
            if (paymentType instanceof RfiPaymentType) {
                pay = (RfiPaymentType) paymentType;
                break;
            }

        if (pay == null)
            throw new IllegalArgumentException(CAN_NOT_CHECK_SIGANTURE);

        concat += pay.getSecretKey();

        String md5;
        try {
            MessageDigest md5digester = MessageDigest.getInstance("MD5");
            byte[] md5digest = md5digester.digest(concat.getBytes("UTF8"));
            md5 = DatatypeConverter.printHexBinary(md5digest);
        } catch (Exception e) {
            throw new IllegalArgumentException("no such algorithm MD5 or no such encoding UTF8");
        }

        if (!md5.equals(check)) {
            Logger.info(String.format("wrong check: md5(%s) = %s != %s", concat, md5, check));
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
}
