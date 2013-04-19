package common;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: work
 * Date: 19.04.13
 * Time: 10:56
 * To change this template use File | Settings | File Templates.
 */
public abstract class Utils {
    public static String getFormValue(Map<String, String[]> fieldsData, String fieldName) {
        String[] fieldsValue = fieldsData.get(fieldName);
        return ((fieldsValue == null || fieldsValue.length == 0 || fieldsValue[0] == null) ? null : (StringUtils.isBlank(fieldsValue[0]) ? null : fieldsValue[0].trim()));
    }
}
