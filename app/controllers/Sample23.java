//###<i>This sample will show how to use <b>GetDocumentPagesImageUrls</b> method from Doc Api to return a URL representing a single page of a Document</i>
package controllers;
//Import of necessary libraries

import com.groupdocs.sdk.api.DocApi;
import com.groupdocs.sdk.api.StorageApi;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.FileStream;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.GetImageUrlsResponse;
import com.groupdocs.sdk.model.UploadResponse;
import common.Utils;
import models.Credentials;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.io.FileInputStream;

public class Sample23 extends Controller {
    //
    protected static Form<Credentials> form = Form.form(Credentials.class);

    public static Result index() {

        if (Utils.isPOST(request())) {
            form = Form.form(Credentials.class).bindFromRequest();
            // Check errors
            if (form.hasErrors()) {
                return badRequest(views.html.sample23.render(false, null, form));
            }
            // Save credentials to session
            Credentials credentials = form.get();
            session().put("clientId", credentials.getClientId());
            session().put("privateKey", credentials.getPrivateKey());
            session().put("basePath", credentials.getBasePath());
            credentials.normalizeBasePath("https://api.groupdocs.com/v2.0");
            // Get request parameters
            Http.MultipartFormData body = request().body().asMultipartFormData();
            String width = Utils.getFormValue(body, "width");
            String height = Utils.getFormValue(body, "height");
            String sourse = Utils.getFormValue(body, "sourse");
            // Initialize SDK with private key
            ApiInvoker.getInstance().setRequestSigner(
                    new GroupDocsRequestSigner(credentials.getPrivateKey()));

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
                    storageApi.setBasePath(credentials.getBasePath());
                    UploadResponse uploadResponse = storageApi.UploadWeb(credentials.getClientId(), url);
                    // Check response status
                    uploadResponse = Utils.assertResponse(uploadResponse);
                    guid = uploadResponse.getResult().getGuid();
                } else if ("local".equals(sourse)) { // Upload local file
                    Http.MultipartFormData.FilePart file = body.getFile("file");
                    StorageApi storageApi = new StorageApi();
                    // Initialize API with base path
                    storageApi.setBasePath(credentials.getBasePath());
                    FileInputStream is = new FileInputStream(file.getFile());
                    UploadResponse uploadResponse = storageApi.Upload(credentials.getClientId(), file.getFilename(), "uploaded", "", 1, new FileStream(is));
                    // Check response status
                    uploadResponse = Utils.assertResponse(uploadResponse);
                    guid = uploadResponse.getResult().getGuid();
                }
                guid = Utils.assertNotNull(guid);
                // Create DocApi object
                DocApi docApi = new DocApi();
                // Initialize API with base path
                docApi.setBasePath(credentials.getBasePath());
                // Get document metadata
                GetImageUrlsResponse viewDocumentResponse = docApi.GetDocumentPagesImageUrls(credentials.getClientId(), guid, 0, 1, "650x500", null, null, null);
                // Check response status
                viewDocumentResponse = Utils.assertResponse(viewDocumentResponse);
                String image = "";
                for (String url : viewDocumentResponse.getResult().getUrl()) {
                    image += "<img src='" + url + "'></img><br/>";
                }
                // Render view
                return ok(views.html.sample23.render(true, image, form));
            } catch (Exception e) {
                return badRequest(views.html.sample23.render(false, null, form));
            }
        } else if (Utils.isGET(request())) {
            form = form.bind(session());
        }
        return ok(views.html.sample23.render(false, null, form));
    }
}