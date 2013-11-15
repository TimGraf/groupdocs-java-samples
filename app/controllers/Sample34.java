package controllers;

import com.groupdocs.sdk.api.StorageApi;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.CreateFolderResponse;
import common.Utils;
import models.Credentials;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

/**
 * Created with IntelliJ IDEA.
 * User: liosha
 * Date: 06.11.13
 * Time: 17:27
 */
public class Sample34 extends Controller {
    //
    protected static Form<Credentials> form = Form.form(Credentials.class);

    public static Result index() {

        if (Utils.isPOST(request())) {
            form = Form.form(Credentials.class).bindFromRequest();
            // Check errors
            if (form.hasErrors()) {
                return badRequest(views.html.sample34.render(false, null, form));
            }
            // Save credentials to session
            Credentials credentials = form.get();
            session().put("clientId", credentials.getClientId());
            session().put("privateKey", credentials.getPrivateKey());
            session().put("basePath", credentials.getBasePath());
            credentials.normalizeBasePath("https://api.groupdocs.com/v2.0");
            // Get request parameters
            Http.MultipartFormData body = request().body().asMultipartFormData();
            String folder = Utils.getFormValue(body.asFormUrlEncoded(), "folder");
            // Initialize SDK with private key
            ApiInvoker.getInstance().setRequestSigner(
                    new GroupDocsRequestSigner(credentials.getPrivateKey()));

            try {
                //
                StorageApi storageApi = new StorageApi();
                // Initialize API with base path
                storageApi.setBasePath(credentials.getBasePath());
                //
                folder = folder.replaceAll("\\\\", "/");
                // Create folder with provided name
                CreateFolderResponse createFolderResponse = storageApi.Create(credentials.getClientId(), folder);
                // Check response status
                createFolderResponse = Utils.assertResponse(createFolderResponse);
                //
                String path = createFolderResponse.getResult().getPath();
                // Render view
                return ok(views.html.sample34.render(true, path, form));
            } catch (Exception e) {
                return badRequest(views.html.sample34.render(false, null, form));
            }
        } else if (Utils.isGET(request())) {
            form = form.bind(session());
        }
        return ok(views.html.sample34.render(false, null, form));
    }
}
