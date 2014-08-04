//###<i>This sample will show how to use <b>Upload</b> method from Storage Api to upload file to GroupDocs Storage </i>
package controllers;
//Import of necessary libraries

import com.groupdocs.sdk.api.AntApi;
import com.groupdocs.sdk.api.MgmtApi;
import com.groupdocs.sdk.api.SignatureApi;
import com.groupdocs.sdk.api.StorageApi;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.FileStream;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.*;
import common.Utils;
import models.Credentials;
import org.apache.commons.lang3.StringUtils;
import play.data.Form;
import play.data.validation.Constraints;
import play.mvc.Http;
import play.mvc.Result;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;

public class Sample40 extends BaseController {
    public static String USER_INFO_FILE = "UserInfo_sample40.tmp";
    //
    protected static Form<Sample40Form> form = Form.form(Sample40Form.class);

    public static Result index() {

        if (Utils.isPOST(request())) {
            form = Form.form(Sample40Form.class).bindFromRequest();
            // Check errors
            if (form.hasErrors()) {
                return badRequest(views.html.sample40.render(false, null, form));
            }
            // Save credentials to session
            Sample40Form sform = form.get();
            session().put("clientId", sform.getClientId());
            session().put("privateKey", sform.getPrivateKey());
            session().put("basePath", sform.getBasePath());
            sform.normalizeBasePath("https://api.groupdocs.com/v2.0");
            // Initialize SDK with private key
            ApiInvoker.getInstance().setRequestSigner(new GroupDocsRequestSigner(sform.getPrivateKey()));

            try {

                //path to settings file - temporary save userId and apiKey like to property file
                if (!StringUtils.isEmpty(sform.getCallbackUrl())) {
                    FileOutputStream fileOutputStream = new FileOutputStream(USER_INFO_FILE);
                    DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream);

                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(sform.getClientId());
                    stringBuilder.append("|");
                    stringBuilder.append(sform.getPrivateKey());
                    stringBuilder.append("|");
                    stringBuilder.append(sform.getBasePath());

                    dataOutputStream.writeUTF(stringBuilder.toString());

                    dataOutputStream.flush();
                    fileOutputStream.close();
                }

                SignatureApi signatureApi = new SignatureApi();
                //Set url to choose whot server to use
                //Set base path
                signatureApi.setBasePath(sform.getBasePath());
                //Create WebHook object
                WebhookInfo webHook = new WebhookInfo();
                //Set callbackUrl url of webhook which will be triggered when form is signed.
                webHook.setCallbackUrl(sform.getCallbackUrl());
                    //Create signature form (it will be copy of original form if formGUID parameter is set)
                SignatureFormResponse signatureFormResponse = signatureApi.CreateSignatureForm(sform.getClientId(), "sampleForm_" + webHook.toString(), null, null, sform.getFormGuid(), null);
                Utils.assertResponse(signatureFormResponse);
                String formId = signatureFormResponse.getResult().getForm().getId();
                //Published new form that users can sign it and set callback URL to it
                SignatureStatusResponse signatureStatusResponse = signatureApi.PublishSignatureForm(sform.getClientId(), formId, webHook);
                //Check status
                Utils.assertResponse(signatureStatusResponse);
                //Generate iframe url
                String server = sform.getBasePath().substring(0, sform.getBasePath().indexOf(".com") + 4).replace("api", "apps");
                String url = server + "/signature2/forms/signembed/" + formId;
                url = new GroupDocsRequestSigner(sform.getPrivateKey()).signUrl(url);
                // Form is published successfully
                return ok(views.html.sample40.render(true, url, form));
            } catch (Exception e) {
                return badRequest(views.html.sample40.render(false, null, form));
            }

        } else if (Utils.isGET(request())) {
            form = form.bind(session());
        }
        return ok(views.html.sample40.render(false, null, form));
    }

}