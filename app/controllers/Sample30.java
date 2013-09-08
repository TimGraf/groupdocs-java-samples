package controllers;

import com.groupdocs.sdk.api.StorageApi;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.FileStream;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.DeleteResponse;
import com.groupdocs.sdk.model.UploadResponse;
import common.Utils;
import models.Credentials;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.io.FileInputStream;

/**
 * Created with IntelliJ IDEA.
 * User: liosha
 * Date: 07.09.13
 * Time: 15:41
 * This sample will show how delete file from GroupDocs Storage using Java SDK
 */
public class Sample30 extends Controller {
    //
    protected static Form<Credentials> form = form(Credentials.class);

    public static Result index() {

        if (Utils.isPOST(request())) {
            form = form(Credentials.class).bindFromRequest();
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
            String sourse = Utils.getFormValue(body, "sourse");
            // Initialize SDK with private key
            ApiInvoker.getInstance().setRequestSigner(
                    new GroupDocsRequestSigner(credentials.getPrivate_key()));

            try {
                //
                String guid = null;
                //
                if ("guid".equals(sourse)) { // File GUID
                    guid = Utils.getFormValue(body, "fileId");
                } else if ("url".equals(sourse)) { // Upload file fron URL
                    String url = Utils.getFormValue(body, "url");
                    StorageApi storageApi = new StorageApi();
                    // Initialize API with base path
                    storageApi.setBasePath(credentials.getServer_type());
                    UploadResponse uploadResponse = storageApi.UploadWeb(credentials.getClient_id(), url);
                    // Check response status
                    uploadResponse = Utils.assertResponse(uploadResponse);
                    guid = uploadResponse.getResult().getGuid();
                } else if ("local".equals(sourse)) { // Upload local file
                    Http.MultipartFormData.FilePart file = body.getFile("file");
                    StorageApi storageApi = new StorageApi();
                    // Initialize API with base path
                    storageApi.setBasePath(credentials.getServer_type());
                    FileInputStream is = new FileInputStream(file.getFile());
                    UploadResponse uploadResponse = storageApi.Upload(credentials.getClient_id(), file.getFilename(), "uploaded", "", new FileStream(is));
                    // Check response status
                    uploadResponse = Utils.assertResponse(uploadResponse);
                    guid = uploadResponse.getResult().getGuid();
                }
                guid = Utils.assertNotNull(guid);

                StorageApi storageApi = new StorageApi();
                storageApi.setBasePath(credentials.getServer_type());
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
