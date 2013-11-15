//###<i>This sample will show how to use <b>Upload</b> method from Storage Api to upload file to GroupDocs Storage </i>
package controllers;
//Import of necessary libraries

import com.groupdocs.sdk.api.StorageApi;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.FileStream;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.UploadResponse;
import common.Utils;
import models.Credentials;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;

import java.io.FileInputStream;

public class Sample03 extends Controller {
    //
    protected static Form<Credentials> form = Form.form(Credentials.class);

    public static Result index() {

        if (Utils.isPOST(request())) {
            form = Form.form(Credentials.class).bindFromRequest();
            // Check errors
            if (form.hasErrors()) {
                return badRequest(views.html.sample03.render(false, null, form));
            }
            // Save credentials to session
            Credentials credentials = form.get();
            session().put("clientId", credentials.getClientId());
            session().put("privateKey", credentials.getPrivateKey());
            session().put("basePath", credentials.getBasePath());
            credentials.normalizeBasePath("https://api.groupdocs.com/v2.0");
            // Get request parameters
            MultipartFormData body = request().body().asMultipartFormData();
            String sourse = Utils.getFormValue(body.asFormUrlEncoded(), "sourse");
            String folderPath = Utils.getFormValue(body.asFormUrlEncoded(), "folderPath");
            // Initialize SDK with private key
            ApiInvoker.getInstance().setRequestSigner(
                    new GroupDocsRequestSigner(credentials.getPrivateKey()));

            try {
                //
                StorageApi storageApi = new StorageApi();
                // Initialize API with base path
                storageApi.setBasePath(credentials.getBasePath());
                //
                UploadResponse uploadResponse = null;
                if ("local".equals(sourse)) {
                    FilePart filePart = body.getFile("file");
                    FileInputStream is = new FileInputStream(filePart.getFile());
                    String callbackUrl = Utils.getFormValue(body.asFormUrlEncoded(), "callbackUrl");
                    // Upload file to current user storage from local computer
                    if (folderPath.startsWith("\\") || folderPath.startsWith("/")){
                        folderPath = folderPath.substring(1);
                    }
                    if (!folderPath.endsWith("\\") && !folderPath.endsWith("/")) {
                        folderPath += "/";
                    }
                    uploadResponse = storageApi.Upload(credentials.getClientId(), folderPath + filePart.getFilename(), "uploaded", callbackUrl, new FileStream(is));
                } else if ("url".equals(sourse)) {
                    String url = Utils.getFormValue(body.asFormUrlEncoded(), "url");
                    // Upload file to current user storage from url
                    uploadResponse = storageApi.UploadWeb(credentials.getClientId(), url);
                }
                // Check response status
                uploadResponse = Utils.assertResponse(uploadResponse);
                // Render view
                return ok(views.html.sample03.render(true, uploadResponse.getResult(), form));
            } catch (Exception e) {
                return badRequest(views.html.sample03.render(false, null, form));
            }
        } else if (Utils.isGET(request())) {
            form = form.bind(session());
        }
        return ok(views.html.sample03.render(false, null, form));
    }
}