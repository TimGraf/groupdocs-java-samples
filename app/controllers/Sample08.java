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

public class Sample08 extends Controller {
    //
    protected static Form<Credentials> form = Form.form(Credentials.class);

    public static Result index() {

        if (Utils.isPOST(request())) {
            form = Form.form(Credentials.class).bindFromRequest();
            // Check errors
            if (form.hasErrors()) {
                return badRequest(views.html.sample08.render(false, null, form));
            }
            // Save credentials to session
            Credentials credentials = form.get();
            session().put("clientId", credentials.getClientId());
            session().put("privateKey", credentials.getPrivateKey());
            session().put("basePath", credentials.getBasePath());
            credentials.normalizeBasePath("https://api.groupdocs.com/v2.0");
            // Get request parameters
            Http.MultipartFormData body = request().body().asMultipartFormData();
            String sourse = Utils.getFormValue(body, "sourse");
            int pageNumber = Integer.parseInt(Utils.getFormValue(body, "pageNumber"));
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
                    storageApi.setBasePath(credentials.getBasePath());
                    UploadResponse response = storageApi.UploadWeb(credentials.getClientId(), url);
                    response = Utils.assertResponse(response);
                    guid = response.getResult().getGuid();
                } else if ("local".equals(sourse)) { // Upload local file
                    Http.MultipartFormData.FilePart file = body.getFile("file");
                    StorageApi storageApi = new StorageApi();
                    storageApi.setBasePath(credentials.getBasePath());
                    FileInputStream is = new FileInputStream(file.getFile());
                    UploadResponse response = storageApi.Upload(credentials.getClientId(), file.getFilename(), "uploaded", "", 1, new FileStream(is));
                    response = Utils.assertResponse(response);
                    guid = response.getResult().getGuid();
                }
                guid = Utils.assertNotNull(guid);
                //
                DocApi api = new DocApi();
                // Initialize API with base path
                api.setBasePath(credentials.getBasePath());
                // Call sample method
                GetImageUrlsResponse imageUrlsResponse = api.GetDocumentPagesImageUrls(credentials.getClientId(), guid, pageNumber, 1, "250x350", null, null, null);
                imageUrlsResponse = Utils.assertResponse(imageUrlsResponse);
                // Render view
                return ok(views.html.sample08.render(true, imageUrlsResponse.getResult().getUrl(), form));
            } catch (Exception e) {
                return badRequest(views.html.sample08.render(false, null, form));
            }
        } else if (Utils.isGET(request())) {
            form = form.bind(session());
        }
        return ok(views.html.sample08.render(false, null, form));
    }
}