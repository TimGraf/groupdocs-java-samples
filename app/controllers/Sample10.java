//###<i>This sample will show how to use <b>ShareDocument</b> tmethod from Doc Api to share a document to other users</i>
package controllers;
//Import of necessary libraries

import com.groupdocs.sdk.api.DocApi;
import com.groupdocs.sdk.api.StorageApi;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.FileStream;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.GetDocumentInfoResponse;
import com.groupdocs.sdk.model.SharedUsersResponse;
import com.groupdocs.sdk.model.UploadResponse;
import common.Utils;
import models.Credentials;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import scala.actors.threadpool.Arrays;

import java.io.FileInputStream;

public class Sample10 extends Controller {
    //
    protected static Form<Credentials> form = form(Credentials.class);

    public static Result index() {

        if (Utils.isPOST(request())) {
            form = form.bindFromRequest();
            // Check errors
            if (form.hasErrors()) {
                return badRequest(views.html.sample10.render(false, null, form));
            }
            // Save credentials to session
            Credentials credentials = form.get();
            session().put("client_id", credentials.getClient_id());
            session().put("private_key", credentials.getPrivate_key());
            session().put("server_type", credentials.getServer_type());
            // Get request parameters
            Http.MultipartFormData body = request().body().asMultipartFormData();
            String email = Utils.getFormValue(body, "email");
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
                }
                else if ("url".equals(sourse)) { // Upload file fron URL
                    String url = Utils.getFormValue(body, "url");
                    ApiInvoker.getInstance().setRequestSigner(
                            new GroupDocsRequestSigner(credentials.getPrivate_key()));
                    StorageApi storageApi = new StorageApi();
                    // Initialize API with base path
                    storageApi.setBasePath(credentials.getServer_type());
                    UploadResponse uploadResponse = storageApi.UploadWeb(credentials.getClient_id(), url);
                    // Check response status
                    uploadResponse = Utils.assertResponse(uploadResponse);
                    guid = uploadResponse.getResult().getGuid();
                }
                else if ("local".equals(sourse)) { // Upload local file
                    Http.MultipartFormData.FilePart file = body.getFile("filePart");
                    ApiInvoker.getInstance().setRequestSigner(
                            new GroupDocsRequestSigner(credentials.getPrivate_key()));
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
                //
                DocApi docApi = new DocApi();
                // Initialize API with base path
                docApi.setBasePath(credentials.getServer_type());
                // Get document metadeta for entered fileId
                GetDocumentInfoResponse documentInfoResponse = docApi.GetDocumentMetadata(credentials.getClient_id(), guid);
                documentInfoResponse = Utils.assertResponse(documentInfoResponse);
                String file_Id = documentInfoResponse.getResult().getId().toString();

                // Make a request to DocApi to share document
                SharedUsersResponse sharedUsersResponse = docApi.ShareDocument(credentials.getClient_id(), file_Id, Arrays.asList(new String[]{email}));
                // Check request status
                sharedUsersResponse = Utils.assertResponse(sharedUsersResponse);
                // Render view
                return ok(views.html.sample10.render(true, sharedUsersResponse.getResult(), form));
            } catch (Exception e) {
                return badRequest(views.html.sample10.render(false, null, form));
            }
        } else if (Utils.isGET(request())) {
            form = form.bind(session());
            session().put("server_type", "https://api.groupdocs.com/v2.0");
        }
        return ok(views.html.sample10.render(false, null, form));
    }
}