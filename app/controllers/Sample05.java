//###<i>This sample will show how to use <b>MoveFile</b> method from Storage Api to copy/move a file in GroupDocs Storage </i>
package controllers;
//Import of necessary libraries

import com.groupdocs.sdk.api.StorageApi;
import com.groupdocs.sdk.api.DocApi;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.FileStream;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.*;
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
    protected static Form<Credentials> form = Form.form(Credentials.class);

    public static Result index() {

        if (Utils.isPOST(request())) {
            form = Form.form(Credentials.class).bindFromRequest();
            // Check errors
            if (form.hasErrors()) {
                return badRequest(views.html.sample05.render(false, null, form, null));
            }
            // Save credentials to session
            Credentials credentials = form.get();
            session().put("clientId", credentials.getClientId());
            session().put("privateKey", credentials.getPrivateKey());
            session().put("basePath", credentials.getBasePath());
            credentials.normalizeBasePath("https://api.groupdocs.com/v2.0");
            // Get request parameters
            Http.MultipartFormData body = request().body().asMultipartFormData();
            String sourse = Utils.getFormValue(body.asFormUrlEncoded(), "sourse");
            String guid = Utils.getFormValue(body.asFormUrlEncoded(), "srcPath");
            // Initialize SDK with private key
            ApiInvoker.getInstance().setRequestSigner(
                    new GroupDocsRequestSigner(credentials.getPrivateKey()));

            try {
                //
                StorageApi storageApi = new StorageApi();
                // Initialize API with base path
                storageApi.setBasePath(credentials.getBasePath());
                //
                String uploadDir = "";
                String fileName = null;
                Double fileId = 0d;

                if ("local".equals(sourse)) {
                    Http.MultipartFormData.FilePart filePart = body.getFile("file");
                    FileInputStream is = new FileInputStream(filePart.getFile());
                    String callbackUrl = Utils.getFormValue(body.asFormUrlEncoded(), "callbackUrl");
                    // Upload file to current user storage from local computer
                    UploadResponse uploadResponse = storageApi.Upload(credentials.getClientId(), filePart.getFilename(), "uploaded", callbackUrl, 2, new FileStream(is));
                    // Check response status
                    uploadResponse = Utils.assertResponse(uploadResponse);
                    guid = uploadResponse.getResult().getGuid();
                    Thread.sleep(3000);
                } else if ("url".equals(sourse)) {
                    String url = Utils.getFormValue(body.asFormUrlEncoded(), "url");
                    UploadResponse uploadResponse = storageApi.UploadWeb(credentials.getClientId(), url);
                    // Check response status
                    uploadResponse = Utils.assertResponse(uploadResponse);
                    guid = uploadResponse.getResult().getGuid();
                    uploadDir = "My Web Documents";
                }
                String destPath = Utils.getFormValue(body.asFormUrlEncoded(), "destPath");
                if (destPath == null){
                    destPath = "";
                }

                if (StringUtils.isEmpty(guid)) {
                    return badRequest(views.html.sample05.render(false, null, form, null));
                }

                ListEntitiesResponse response = storageApi.ListEntities(credentials.getClientId(), uploadDir, 0, null, null, null, null, null, null);
                // Check response status
                response = Utils.assertResponse(response);
                // Get document name and document ID
                for (FileSystemDocument document : response.getResult().getFiles()) {
                    if (guid.equals(document.getGuid())) {
                        fileName = document.getName();
                        fileId = document.getId();
                        break;

                    } else {
                        DocApi docApi = new DocApi();
                        DocumentAccessInfoResponse response2 = docApi.GetDocumentAccessInfo(credentials.getClientId(), guid);
                        // Check response status
                        response2 = Utils.assertResponse(response2);
                        fileName = response2.getResult().getName();
                        fileId = response2.getResult().getId();
                    }
                }
                guid = Utils.assertNotNull(guid);
                fileName = Utils.assertNotNull(fileName);
                if (fileName.contains("http:")) {
                    fileName = fileName.split("/")[fileName.split("/").length - 1];
                }
                String copyToPath = null;
                if (destPath.equals("")){
                    copyToPath = (fileName).replaceAll("\\\\", "/").replaceAll("//", "/");
                }else{
                    copyToPath = (destPath + File.separator + fileName).replaceAll("\\\\", "/").replaceAll("//", "/");
                }


                String action = null;
                FileMoveResponse copyMoveResponse = null;

                if (Utils.getFormValue(body.asFormUrlEncoded(), "copy") != null) {
                    copyMoveResponse = storageApi.MoveFile(credentials.getClientId(), copyToPath, null, Double.toString(fileId), null);
                    action = "copy";
                } else if (Utils.getFormValue(body.asFormUrlEncoded(), "move") != null) {
                    copyMoveResponse = storageApi.MoveFile(credentials.getClientId(), copyToPath, null, null, Double.toString(fileId));
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
        }
        return ok(views.html.sample05.render(false, null, form, null));
    }
}