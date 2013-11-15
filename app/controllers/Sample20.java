//###<i>This sample will show how to Get Compare Change list for document</i>
package controllers;
//Import of necessary libraries

import com.groupdocs.sdk.api.ComparisonApi;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.ChangesResponse;
import common.Utils;
import models.Credentials;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

public class Sample20 extends Controller {
    //
    protected static Form<Credentials> form = Form.form(Credentials.class);

    public static Result index() {

        if (Utils.isPOST(request())) {
            form = Form.form(Credentials.class).bindFromRequest();
            // Check errors
            if (form.hasErrors()) {
                return badRequest(views.html.sample20.render(false, null, form));
            }
            // Save credentials to session
            Credentials credentials = form.get();
            session().put("clientId", credentials.getClientId());
            session().put("privateKey", credentials.getPrivateKey());
            session().put("basePath", credentials.getBasePath());
            credentials.normalizeBasePath("https://api.groupdocs.com/v2.0");
            // Get request parameters
            Http.MultipartFormData body = request().body().asMultipartFormData();
            String fileId = Utils.getFormValue(body, "resultFileId");
            // Initialize SDK with private key
            ApiInvoker.getInstance().setRequestSigner(
                    new GroupDocsRequestSigner(credentials.getPrivateKey()));

            try {
                //
                fileId = Utils.assertNotNull(fileId);
                // Create StorageApi object
                ComparisonApi api = new ComparisonApi();
                // Initialize API with base path
                api.setBasePath(credentials.getBasePath());
                ChangesResponse changesResponse = api.GetChanges(credentials.getClientId(), fileId);
                // Check response status
                changesResponse = Utils.assertResponse(changesResponse);
                // Render view
                return ok(views.html.sample20.render(true, changesResponse.getResult(), form));
            } catch (Exception e) {
                return badRequest(views.html.sample20.render(false, null, form));
            }
        } else if (Utils.isGET(request())) {
            form = form.bind(session());
        }
        return ok(views.html.sample20.render(false, null, form));
    }
}