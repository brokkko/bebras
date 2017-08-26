package plugins.applications;

import models.newserialization.SerializableTreeSerializationType;

public class PaymentTypeSerializationType extends SerializableTreeSerializationType<PaymentType> {

    public PaymentTypeSerializationType() {
        registerClass("self-confirm", SelfConfirmPaymentType.class);
        registerClass("kvit", KvitBankTransferPaymentType.class);
        registerClass("rfi", RfiPaymentType.class);
    }
}
