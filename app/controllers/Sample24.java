//###<i>This sample will show how to use <b>Upload</b> method from Storage Api to upload file to GroupDocs Storage </i>
package controllers;
//Import of necessary libraries

import com.groupdocs.sdk.api.StorageApi;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.UploadResponse;
import common.Utils;
import models.Credentials;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

public class Sample24 extends Controller {
    //
    protected static Form<Credentials> form = Form.form(Credentials.class);

    public static Result index() {

        if (Utils.isPOST(request())) {
            form = Form.form(Credentials.class).bindFromRequest();
            // Check errors
            if (form.hasErrors()) {
                return badRequest(views.html.sample24.render(false, null, form));
            }
            // Save credentials to session
            Credentials credentials = form.get();
            session().put("clientId", credentials.getClientId());
            session().put("privateKey", credentials.getPrivateKey());
            session().put("basePath", credentials.getBasePath());
            credentials.normalizeBasePath("https://api.groupdocs.com/v2.0");
            // Get request parameters
            Http.MultipartFormData body = request().body().asMultipartFormData();
            String url = Utils.getFormValue(body, "url");
            // Initialize SDK with private key
            ApiInvoker.getInstance().setRequestSigner(
                    new GroupDocsRequestSigner(credentials.getPrivateKey()));

            try {
                //
                //Create Storage object
                StorageApi api = new StorageApi();
                // Initialize API with base path
                api.setBasePath(credentials.getBasePath());
                // Upload file to current user storage
                UploadResponse response = api.UploadWeb(credentials.getClientId(), url);
                // Check response status
                response = Utils.assertResponse(response);
                // Render view
                return ok(views.html.sample24.render(true, response.getResult(), form));
            } catch (Exception e) {
                return badRequest(views.html.sample24.render(false, null, form));
            }
        } else if (Utils.isGET(request())) {
            form = form.bind(session());
        }
        return ok(views.html.sample24.render(false, null, form));
    }
}