package controllers;

import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import models.Credentials;
import play.data.validation.Constraints;
import play.mvc.Controller;

import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * @author Aleksey Permyakov
 */
public class BaseController extends Controller {
    private static HashMap<Pattern, String> api2base = new HashMap<Pattern, String>(){{
        put(Pattern.compile("^(https|http)://api\\.groupdocs\\.com/v2\\.0.*"), "https://apps.groupdocs.com");
        put(Pattern.compile("^(https|http)://dev-api\\.groupdocs\\.com/v2\\.0.*"), "https://dev-apps.groupdocs.com");
        put(Pattern.compile("^(https|http)://stage-api\\.groupdocs\\.com/v2\\.0.*"), "https://stage-apps.groupdocs.com");
        put(Pattern.compile("^(https|http)://realtime-api\\.groupdocs\\.com.*"), "http://realtime-apps.groupdocs.com");
    }};
    public static String apiPath2framePath(String apiPath){
        return apiPath2framePath(apiPath, "");
    }

    public static String apiPath2framePath(String apiPath, String suffix){
        for (Pattern pattern : api2base.keySet()) {
            if (pattern.matcher(apiPath).matches()) {
                return api2base.get(pattern) + (suffix.startsWith("/") ? suffix : "/" + suffix);
            }
        }
        return null;
    }

    public static String signUrl(String pkey, String url) {
        GroupDocsRequestSigner groupDocsRequestSigner = new GroupDocsRequestSigner(pkey);
        return groupDocsRequestSigner.signUrl(url);
    }

    /**
     * Form class for sample 40
     */
    public static class Sample40Form extends Credentials {
        @Constraints.Required
        private String formGuid;
        private String callbackUrl;

        public String getFormGuid() {
            return formGuid;
        }

        public void setFormGuid(String formGuid) {
            this.formGuid = formGuid;
        }

        public String getCallbackUrl() {
            return callbackUrl == null ? "" : callbackUrl;
        }

        public void setCallbackUrl(String callbackUrl) {
            this.callbackUrl = callbackUrl;
        }
    }
}
