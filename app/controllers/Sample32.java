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
    protected static Form<Credentials> form = form(Credentials.class);

    public static Result index() {

        if (Utils.isPOST(request())) {
            form = form(Credentials.class).bindFromRequest();
            // Check errors
            if (form.hasErrors()) {
                return badRequest(views.html.sample32.render(false, null, form));
            }
            // Save credentials to session
            Credentials credentials = form.get();
            session().put("client_id", credentials.getClient_id());
            session().put("private_key", credentials.getPrivate_key());
            session().put("server_type", credentials.getServer_type());
            // Get request parameters
            Http.MultipartFormData body = request().body().asMultipartFormData();
            String callbackUrl = Utils.getFormValue(body, "callbackUrl");
            String form_guid = Utils.getFormValue(body, "form_guid");
            String template_guid = Utils.getFormValue(body, "template_guid");
            String email = Utils.getFormValue(body, "email");
            // Initialize SDK with private key
            ApiInvoker.getInstance().setRequestSigner(
                    new GroupDocsRequestSigner(credentials.getPrivate_key()));

            try {
                //


                SignatureApi signatureApi = new SignatureApi();
                // Set url to choose whot server to use
                signatureApi.setBasePath(credentials.getServer_type());
                // Create WebHook object
                WebhookInfo webhookInfo = new WebhookInfo();
                // Set callback url of webhook which will be triggered when form is signed.
                webhookInfo.setCallbackUrl(callbackUrl);
                //
                String server = credentials.getServer_type().substring(0, credentials.getServer_type().indexOf(".com") + 4).replace("api", "apps");
                String formUrl = null;

                if (!StringUtils.isEmpty(form_guid)) {
                    SignatureStatusResponse signatureStatusResponse = signatureApi.PublishSignatureForm(credentials.getClient_id(), form_guid, webhookInfo);
                    Utils.assertResponse(signatureStatusResponse);
                    formUrl = server + "/signature2/forms/signembed/" + form_guid;
                } else {
                    // Create Signature form settings object
                    SignatureFormSettingsInfo signatureFormSettingsInfo = new SignatureFormSettingsInfo();
                    // To send notification email to owner when form is signed set notifyOwnerOnSign property to true
                    signatureFormSettingsInfo.setNotifyOwnerOnSign(true);
                    // Generate rendon form name
                    String formName = "Test Form " + Long.toString(org.joda.time.DateTime.now().getMillis());
                    // Create signature form
                    SignatureFormResponse signatureFormResponse = signatureApi.CreateSignatureForm(credentials.getClient_id(), formName, template_guid, null, null, signatureFormSettingsInfo);
                    signatureFormResponse = Utils.assertResponse(signatureFormResponse);
                    //
                    SignatureStatusResponse signatureStatusResponse = signatureApi.PublishSignatureForm(credentials.getClient_id(), signatureFormResponse.getResult().getForm().getId(), webhookInfo);
                    Utils.assertResponse(signatureStatusResponse);
                    formUrl = server + "/signature2/forms/signembed/" + signatureFormResponse.getResult().getForm().getId();


                    //path to settings file - temporary save userId and apiKey like to property file
                    if (!StringUtils.isEmpty(callbackUrl)) {
                        FileOutputStream fileOutputStream = new FileOutputStream(USER_INFO_FILE);
                        DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream);

                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(credentials.getClient_id());
                        stringBuilder.append("|");
                        stringBuilder.append(credentials.getPrivate_key());
                        stringBuilder.append("|");
                        stringBuilder.append(credentials.getServer_type());
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
                return badRequest(views.html.sample32.render(false, null, form));
            }
        } else if (Utils.isGET(request())) {
            form = form.bind(session());
            session().put("server_type", "https://api.groupdocs.com/v2.0");
        }
        return ok(views.html.sample32.render(false, null, form));
    }
}
