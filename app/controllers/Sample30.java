package controllers;

import com.groupdocs.sdk.api.StorageApi;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.DeleteResponse;
import com.groupdocs.sdk.model.FileSystemDocument;
import com.groupdocs.sdk.model.ListEntitiesResponse;
import common.Utils;
import models.Credentials;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

/**
 * Created with IntelliJ IDEA.
 * User: liosha
 * Date: 07.09.13
 * Time: 15:41
 * This sample will show how delete file from GroupDocs Storage using Java SDK
 */
public class Sample30 extends Controller {
    //
    protected static Form<Credentials> form = Form.form(Credentials.class);

    public static Result index() {

        if (Utils.isPOST(request())) {
            form = Form.form(Credentials.class).bindFromRequest();
            // Check errors
            if (form.hasErrors()) {
                return badRequest(views.html.sample30.render(false, form));
            }
            // Save credentials to session
            Credentials credentials = form.get();
            session().put("client_id", credentials.getClient_id());
            session().put("private_key", credentials.getPrivate_key());
            session().put("server_type", credentials.getServer_type());
            // Get request parameters
            Http.MultipartFormData body = request().body().asMultipartFormData();
            // Initialize SDK with private key
            ApiInvoker.getInstance().setRequestSigner(
                    new GroupDocsRequestSigner(credentials.getPrivate_key()));

            try {
                String fileName = Utils.getFormValue(body, "fileName");
                //
                String guid = null;
                //
               
                StorageApi storageApi = new StorageApi();
                storageApi.setBasePath(credentials.getServer_type());
                ListEntitiesResponse allFiles = storageApi.ListEntities(credentials.getClient_id(), "", null, null, null, null, null, null, false);
                allFiles = Utils.assertResponse(allFiles);
                for (FileSystemDocument document : allFiles.getResult().getFiles()) {
                    if (fileName.equals(document.getName())) {
                        guid = document.getGuid();
                        break;
                    }
                }
                guid = Utils.assertNotNull(guid);
                DeleteResponse deleteResponse = storageApi.Delete(credentials.getClient_id(), guid);
                deleteResponse = Utils.assertResponse(deleteResponse);
                // Render view
                return ok(views.html.sample30.render(true, form));
            } catch (Exception e) {
                return badRequest(views.html.sample30.render(false, form));
            }
        } else if (Utils.isGET(request())) {
            form = form.bind(session());
            session().put("server_type", "https://api.groupdocs.com/v2.0");
        }
        return ok(views.html.sample30.render(false, form));
    }
}
