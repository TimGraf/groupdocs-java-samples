//###<i>This sample will show how to use <b>ListEntities</b> method from Storage Api to create a list of thumbnails for a document</i>
package controllers;
//Import of necessary libraries

import com.groupdocs.sdk.api.StorageApi;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.FileSystemDocument;
import com.groupdocs.sdk.model.ListEntitiesResponse;
import common.Utils;
import models.Credentials;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.List;

public class Sample07 extends Controller {
    //
    protected static Form<Credentials> form = Form.form(Credentials.class);

    public static Result index() {

        if (Utils.isPOST(request())) {
            form = Form.form(Credentials.class).bindFromRequest();
            // Check errors
            if (form.hasErrors()) {
                return badRequest(views.html.sample07.render(false, null, form));
            }
            // Save credentials to session
            Credentials credentials = form.get();
            session().put("clientId", credentials.getClientId());
            session().put("privateKey", credentials.getPrivateKey());
            session().put("basePath", credentials.getBasePath());
            credentials.normalizeBasePath("https://api.groupdocs.com/v2.0");
            // Initialize SDK with private key
            ApiInvoker.getInstance().setRequestSigner(
                    new GroupDocsRequestSigner(credentials.getPrivateKey()));

            try {
                //
                StorageApi storageApi = new StorageApi();
                // Initialize API with base path
                storageApi.setBasePath(credentials.getBasePath());
                // Get all Entities with thumbnails from current user
                ListEntitiesResponse response = storageApi.ListEntities(credentials.getClientId(), "", null, null, null, null, null, null, true);
                // Check request status
                response = Utils.assertResponse(response);
                // Get all files
                List<FileSystemDocument> documents = response.getResult().getFiles();
                // Render view
                return ok(views.html.sample07.render(true, documents, form));
            } catch (Exception e) {
                return badRequest(views.html.sample07.render(false, null, form));
            }
        } else if (Utils.isGET(request())) {
            form = form.bind(session());
        }
        return ok(views.html.sample07.render(false, null, form));
    }
}