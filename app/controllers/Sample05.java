//###<i>This sample will show how to use <b>MoveFile</b> method from Storage Api to copy/move a file in GroupDocs Storage </i>
package controllers;
//Import of necessary libraries

import com.groupdocs.sdk.api.StorageApi;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.FileStream;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.FileMoveResponse;
import com.groupdocs.sdk.model.FileSystemDocument;
import com.groupdocs.sdk.model.ListEntitiesResponse;
import com.groupdocs.sdk.model.UploadResponse;
import common.Utils;
import models.Credentials;
import org.apache.commons.lang3.StringUtils;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.io.File;
import java.io.FileInputStream;

public class Sample05 extends Controller {
    //
    protected static Form<Credentials> form = form(Credentials.class);

    public static Result index() {

        if (Utils.isPOST(request())) {
            form = form.bindFromRequest();
            // Check errors
            if (form.hasErrors()) {
                return badRequest(views.html.sample05.render(false, null, form, null));
            }
            // Save credentials to session
            Credentials credentials = form.get();
            session().put("client_id", credentials.getClient_id());
            session().put("private_key", credentials.getPrivate_key());
            session().put("server_type", credentials.getServer_type());
            // Get request parameters
            Http.MultipartFormData body = request().body().asMultipartFormData();
            String sourse = Utils.getFormValue(body.asFormUrlEncoded(), "sourse");
            String guid = Utils.getFormValue(body.asFormUrlEncoded(), "srcPath");
            // Initialize SDK with private key
            ApiInvoker.getInstance().setRequestSigner(
                    new GroupDocsRequestSigner(credentials.getPrivate_key()));

            try {
                //
                StorageApi storageApi = new StorageApi();
                // Initialize API with base path
                storageApi.setBasePath(credentials.getServer_type());
                //
                String uploadDir = "";
                String fileName = null;
                Double fileId = 0d;

                if ("local".equals(sourse)){
                    Http.MultipartFormData.FilePart filePart = body.getFile("file");
                    FileInputStream is = new FileInputStream(filePart.getFile());
                    String callbackUrl = Utils.getFormValue(body.asFormUrlEncoded(), "callbackUrl");
                    // Upload file to current user storage from local computer
                    UploadResponse uploadResponse = storageApi.Upload(credentials.getClient_id(), filePart.getFilename(), "uploaded", callbackUrl, new FileStream(is));
                    // Check response status
                    uploadResponse = Utils.assertResponse(uploadResponse);
                    guid = uploadResponse.getResult().getGuid();
                }
                else if ("url".equals(sourse)){
                    String url = Utils.getFormValue(body.asFormUrlEncoded(), "url");
                    UploadResponse uploadResponse = storageApi.UploadWeb(credentials.getClient_id(), url);
                    // Check response status
                    uploadResponse = Utils.assertResponse(uploadResponse);
                    guid = uploadResponse.getResult().getGuid();
                }
                String destPath = Utils.getFormValue(body.asFormUrlEncoded(), "destPath");
                destPath = Utils.assertNotNull(destPath);

                if (StringUtils.isEmpty(guid)){
                    return badRequest(views.html.sample05.render(false, null, form, null));
                }

                ListEntitiesResponse response = storageApi.ListEntities(credentials.getClient_id(), uploadDir, 0, null, null, null, null, null, null);
                // Check response status
                response = Utils.assertResponse(response);
                // Get document name and document ID
                for (FileSystemDocument document : response.getResult().getFiles()){
                    if (guid.equals(document.getGuid())){
                        fileName = document.getName();
                        fileId = document.getId();
                        break;
                    }
                }
                guid = Utils.assertNotNull(guid);
                fileName = Utils.assertNotNull(fileName);
                String copyToPath = (destPath + File.separator + fileName).replaceAll("//", "/");

                String action = null;
                FileMoveResponse copyMoveResponse = null;

                if (Utils.getFormValue(body.asFormUrlEncoded(), "copy") != null){
                    copyMoveResponse = storageApi.MoveFile(credentials.getClient_id(), copyToPath, null, Double.toString(fileId), null);
                    action = "copy";
                }
                else if (Utils.getFormValue(body.asFormUrlEncoded(), "move") != null){
                    copyMoveResponse = storageApi.MoveFile(credentials.getClient_id(), copyToPath, null, null, Double.toString(fileId));
                    action = "move";
                }
                // Check response status
                copyMoveResponse = Utils.assertResponse(copyMoveResponse);
                // Render view
                return ok(views.html.sample05.render(true, copyMoveResponse.getResult(), form, action));
            } catch (Exception e) {
                return badRequest(views.html.sample05.render(false, null, form, null));
            }
        } else if (Utils.isGET(request())) {
            form = form.bind(session());
            session().put("server_type", "https://api.groupdocs.com/v2.0");
        }
        return ok(views.html.sample05.render(false, null, form, null));
    }
}