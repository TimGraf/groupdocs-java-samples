//###<i>This sample will show how to use <b>GetFile</b> method from Storage Api to download a file from GroupDocs Storage</i>
package controllers;
//Import of necessary libraries

import com.groupdocs.sdk.api.SharedApi;
import com.groupdocs.sdk.api.StorageApi;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.FileStream;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.UploadResponse;
import common.Utils;
import models.Credentials;
import org.apache.commons.io.IOUtils;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class Sample04 extends Controller {
    //
    protected static Form<Credentials> form = form(Credentials.class);

    public static Result index() {

        if (Utils.isPOST(request())) {
            form = form(Credentials.class).bindFromRequest();
            // Check errors
            if (form.hasErrors()) {
                return badRequest(views.html.sample04.render(false, null, form));
            }
            // Save credentials to session
            Credentials credentials = form.get();
            session().put("client_id", credentials.getClient_id());
            session().put("private_key", credentials.getPrivate_key());
            session().put("server_type", credentials.getServer_type());
            // Get request parameters
            Http.MultipartFormData body = request().body().asMultipartFormData();
            String fileData = Utils.getFormValue(body.asFormUrlEncoded(), "fileData");
            // Initialize SDK with private key
            ApiInvoker.getInstance().setRequestSigner(
                    new GroupDocsRequestSigner(credentials.getPrivate_key()));

            try {
                //
                StorageApi storageApi = new StorageApi();
                // Initialize API with base path
                storageApi.setBasePath(credentials.getServer_type());
                //
                String guid = null;
                //
                if ("IDfileId".equals(fileData)) { // File GUID
                    guid = Utils.getFormValue(body.asFormUrlEncoded(), "fileId");
                } else if ("IDfileUrl".equals(fileData)) { // Upload file fron URL
                    String fileUrl = Utils.getFormValue(body.asFormUrlEncoded(), "fileUrl");
                    UploadResponse response = storageApi.UploadWeb(credentials.getClient_id(), fileUrl);
                    response = Utils.assertResponse(response);
                    guid = response.getResult().getGuid();
                } else if ("IDfilePart".equals(fileData)) { // Upload local file
                    Http.MultipartFormData.FilePart filePart = body.getFile("filePart");
                    FileInputStream is = new FileInputStream(filePart.getFile());
                    UploadResponse response = storageApi.Upload(credentials.getClient_id(), filePart.getFilename(), "uploaded", "", new FileStream(is));
                    response = Utils.assertResponse(response);
                    guid = response.getResult().getGuid();
                }
                guid = Utils.assertNotNull(guid);
                String fileName = Utils.getFileNameByGuid(credentials.getClient_id(), credentials.getPrivate_key(), credentials.getServer_type(), guid);
                //
                SharedApi sharedApi = new SharedApi();
                // Initialize API with base path
                sharedApi.setBasePath(credentials.getServer_type());
                // Download file from storage
                FileStream fileStream = sharedApi.Download(guid, fileName, false);
                fileStream = Utils.assertNotNull(fileStream);
                Utils.assertNotNull(fileStream.getInputStream());
                // Check file name
                if (fileStream.getFileName() == null) {
                    fileStream.setFileName(fileName.split("/")[fileName.split("/").length - 1]);
                }
                // Obtaining file stream of downloading file and definition of folder where to download file
                String separator = System.getProperty("file.separator");
                String path = new File(".").getAbsolutePath();
                String downloadPath = path + separator + "public" + separator + "images" + separator;
                FileOutputStream newFile = new FileOutputStream(downloadPath + fileStream.getFileName());
                // Write file to local folder
                IOUtils.copy(fileStream.getInputStream(), newFile);
                IOUtils.closeQuietly(fileStream.getInputStream());
                // Render view
                return ok(views.html.sample04.render(true, fileStream, form));
            } catch (Exception e) {
                return badRequest(views.html.sample04.render(false, null, form));
            }
        } else if (Utils.isGET(request())) {
            form = form.bind(session());
            session().put("server_type", "https://api.groupdocs.com/v2.0");
        }
        return ok(views.html.sample04.render(false, null, form));
    }
}