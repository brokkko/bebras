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
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class RfiResponseForm {

    //https://lib.rfibank.ru/pages/viewpage.action?pageId=885370
    private String tid;
    private String name;
    private String comment;
    private String partner_id;
    private String service_id;
    private String order_id;
    private String type;
    private String currency;
    private String partner_income;
    private String system_income;
    private String test = "";
    private String check;
    
    private Event event;
    private User user;
    private Applications apps;
    private Application application;

    public void log() {
        DBCollection col = MongoConnection.getRfiLogCollection();
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

        col.insert(o, WriteConcern.MAJORITY); //TODO write concern
        Logger.info("rfi output: " + o.toString());
    }
    
    public void parseOrderId() {
        String[] parts = order_id.split("::");
        if (parts.length != 0)
            throw new IllegalArgumentException("checking rfi output signature: order id has not 3 parts");
        String userId = parts[0];
        String ref = parts[1];
        String appName = parts[2];

        user = User.getUserById(new ObjectId(userId));
        if (user == null)
            throw new IllegalArgumentException("checking rfi output signature: unknown user id " + userId);

        event = user.getEvent();

        Plugin plugin = event.getPlugin(ref);
        if (plugin == null)
            throw new IllegalArgumentException("checking rfi output signature: unknown plugin ref " + ref);
        if (!(plugin instanceof Applications))
            throw new IllegalArgumentException("checking rfi output signature: plugin has a wrong type");

        apps = (Applications) plugin;
        application = apps.getApplicationByName(appName, user);
    }

    public void checkSignature() {
        String concat = getTid() + getName() + getComment()
                + getPartner_id() + getService_id() + getOrder_id() + getType()
                + getPartner_income() + getSystem_income() + getTest();

        //search for RFI payment type
        List<PaymentType> paymentTypes = apps.getPaymentTypes();
        RfiPaymentType pay = null;
        for (PaymentType paymentType : paymentTypes)
            if (paymentType instanceof RfiPaymentType) {
                pay = (RfiPaymentType) paymentType;
                break;
            }

        if (pay == null)
            throw new IllegalArgumentException("checking rfi output signature: no rfi payment in apps plugin found");

        concat += pay.getSecretKey();

        String md5;
        try {
            MessageDigest md5digester = MessageDigest.getInstance("MD5");
            byte[] md5digest = md5digester.digest(concat.getBytes("UTF8"));
            md5 = DatatypeConverter.printHexBinary(md5digest);
        } catch (Exception e) {
            throw new IllegalArgumentException("checking rfi output signature: no such algorithm MD5 or no such encoding UTF8");
        }

        if (!md5.equals(check))
            throw new IllegalArgumentException("checking rfi output signature: wrong check, expected " + md5 + " but got " + check);
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

    public void setService_id(String service_id) {
        this.service_id = service_id;
    }

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
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

    public void setPartner_income(String partner_income) {
        this.partner_income = partner_income;
    }

    public String getSystem_income() {
        return system_income;
    }

    public void setSystem_income(String system_income) {
        this.system_income = system_income;
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

    public Event getEvent() {
        return event;
    }

    public User getUser() {
        return user;
    }

    public Applications getApps() {
        return apps;
    }

    public Application getApplication() {
        return application;
    }
}
