//###<i>This sample will show how to use <b>GetDocumentViews</b> method from Storage Api to get number of viewings for this user </i>
package controllers;
//Import of necessary libraries

import com.groupdocs.sdk.api.DocApi;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.DocumentViewsResponse;
import common.Utils;
import models.Credentials;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

public class Sample15 extends Controller {
    //
    protected static Form<Credentials> form = Form.form(Credentials.class);

    public static Result index() {

        if (Utils.isPOST(request())) {
            form = Form.form(Credentials.class).bindFromRequest();
            // Check errors
            if (form.hasErrors()) {
                return badRequest(views.html.sample15.render(false, null, form));
            }
            // Save credentials to session
            Credentials credentials = form.get();
            session().put("clientId", credentials.getClientId());
            session().put("privateKey", credentials.getPrivateKey());
            session().put("basePath", credentials.getBasePath());
            credentials.normalizeBasePath("https://api.groupdocs.com/v2.0");
            // Get request parameters
            // Initialize SDK with private key
            ApiInvoker.getInstance().setRequestSigner(
                    new GroupDocsRequestSigner(credentials.getPrivateKey()));

            try {
                // Create Doc Api object and get document metadata
                DocApi docApi = new DocApi();
                // Initialize API with base path
                docApi.setBasePath(credentials.getBasePath());

                // Make request to get documents views
                DocumentViewsResponse documentViewsResponse = docApi.GetDocumentViews(credentials.getClientId(), null, null);
                // Check request status
                documentViewsResponse = Utils.assertResponse(documentViewsResponse);

                // Render view
                return ok(views.html.sample15.render(true, documentViewsResponse.getResult().getViews(), form));
            } catch (Exception e) {
                return badRequest(views.html.sample15.render(false, null, form));
            }
        } else if (Utils.isGET(request())) {
            form = form.bind(session());
        }
        return ok(views.html.sample15.render(false, null, form));
    }
}