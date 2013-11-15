package controllers;

import com.groupdocs.sdk.api.SignatureApi;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.SignatureFormResponse;
import com.groupdocs.sdk.model.SignatureFormSettingsInfo;
import com.groupdocs.sdk.model.SignatureStatusResponse;
import com.groupdocs.sdk.model.WebhookInfo;
import common.Utils;
import models.Credentials;
import org.apache.commons.lang3.StringUtils;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.io.DataOutputStream;
import java.io.FileOutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: liosha
 * Date: 25.10.13
 * Time: 1:14
 * To change this template use File | Settings | File Templates.
 */
public class Sample32 extends Controller {
    public static String USER_INFO_FILE = "UserInfo_sample32.tmp";
    //
    protected static Form<Credentials> form = Form.form(Credentials.class);

    public static Result index() {

        if (Utils.isPOST(request())) {
            form = Form.form(Credentials.class).bindFromRequest();
            // Check errors
            if (form.hasErrors()) {
                return badRequest(views.html.sample32.render(false, null, form));
            }
            // Save credentials to session
            Credentials credentials = form.get();
            session().put("clientId", credentials.getClientId());
            session().put("privateKey", credentials.getPrivateKey());
            session().put("basePath", credentials.getBasePath());
            credentials.normalizeBasePath("https://api.groupdocs.com/v2.0");
            // Get request parameters
            Http.MultipartFormData body = request().body().asMultipartFormData();
            String callbackUrl = Utils.getFormValue(body, "callbackUrl");
            String formGuid = Utils.getFormValue(body, "formGuid");
            String templateGuid = Utils.getFormValue(body, "templateGuid");
            String email = Utils.getFormValue(body, "email");
            // Initialize SDK with private key
            ApiInvoker.getInstance().setRequestSigner(
                    new GroupDocsRequestSigner(credentials.getPrivateKey()));

            try {
                //


                SignatureApi signatureApi = new SignatureApi();
                // Set url to choose whot server to use
                signatureApi.setBasePath(credentials.getBasePath());
                // Create WebHook object
                WebhookInfo webhookInfo = new WebhookInfo();
                // Set callback url of webhook which will be triggered when form is signed.
                webhookInfo.setCallbackUrl(callbackUrl);
                //
                String server = credentials.getBasePath().substring(0, credentials.getBasePath().indexOf(".com") + 4).replace("api", "apps");
                String formUrl = null;

                if (!StringUtils.isEmpty(formGuid)) {
                    SignatureStatusResponse signatureStatusResponse = signatureApi.PublishSignatureForm(credentials.getClientId(), formGuid, webhookInfo);
                    Utils.assertResponse(signatureStatusResponse);
                    formUrl = server + "/signature2/forms/signembed/" + formGuid;
                } else {
                    // Create Signature form settings object
                    SignatureFormSettingsInfo signatureFormSettingsInfo = new SignatureFormSettingsInfo();
                    // To send notification email to owner when form is signed set notifyOwnerOnSign property to true
                    signatureFormSettingsInfo.setNotifyOwnerOnSign(true);
                    // Generate rendon form name
                    String formName = "Test Form " + Long.toString(org.joda.time.DateTime.now().getMillis());
                    // Create signature form
                    SignatureFormResponse signatureFormResponse = signatureApi.CreateSignatureForm(credentials.getClientId(), formName, templateGuid, null, null, signatureFormSettingsInfo);
                    signatureFormResponse = Utils.assertResponse(signatureFormResponse);
                    //
                    SignatureStatusResponse signatureStatusResponse = signatureApi.PublishSignatureForm(credentials.getClientId(), signatureFormResponse.getResult().getForm().getId(), webhookInfo);
                    Utils.assertResponse(signatureStatusResponse);
                    formUrl = server + "/signature2/forms/signembed/" + signatureFormResponse.getResult().getForm().getId();


                    //path to settings file - temporary save userId and apiKey like to property file
                    if (!StringUtils.isEmpty(callbackUrl)) {
                        FileOutputStream fileOutputStream = new FileOutputStream(USER_INFO_FILE);
                        DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream);

                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(credentials.getClientId());
                        stringBuilder.append("|");
                        stringBuilder.append(credentials.getPrivateKey());
                        stringBuilder.append("|");
                        stringBuilder.append(credentials.getBasePath());
                        stringBuilder.append("|");
                        stringBuilder.append(email);

                        dataOutputStream.writeUTF(stringBuilder.toString());

                        dataOutputStream.flush();
                        fileOutputStream.close();
                    }
                }


                // Render view
                return ok(views.html.sample32.render(true, formUrl, form));
            } catch (Exception e) {
                System.err.println(e.getMessage());
                return badRequest(views.html.sample32.render(false, null, form));
            }
        } else if (Utils.isGET(request())) {
            form = form.bind(session());
        }
        return ok(views.html.sample32.render(false, null, form));
    }
}
