package models.data;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 22.09.13
 * Time: 23:50
 */
public class WrappedFeatureValue {

    private Object value;
    private Object outputValue;

    public WrappedFeatureValue(Object value, Object outputValue) {
        this.value = value;
        this.outputValue = outputValue;
    }

    public Object getValue() {
        return value;
    }

    public Object getOutputValue() {
        return outputValue;
    }
}
