//###<i>This sample will show how to use <b>SetAnnotationCollaborators</b> method from Annotation Api to set collaborator for document</i>
package controllers;
//Import of necessary libraries

import com.groupdocs.sdk.api.AsyncApi;
import com.groupdocs.sdk.api.StorageApi;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.FileStream;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.ConvertResponse;
import com.groupdocs.sdk.model.GetJobDocumentsResponse;
import com.groupdocs.sdk.model.UploadResponse;
import common.Utils;
import models.Credentials;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class Sample18 extends Controller {
    public static String USER_INFO_FILE = "UserInfo_sample18.tmp";
    //
    protected static Form<Credentials> form = Form.form(Credentials.class);

    public static Result index() {

        if (Utils.isPOST(request())) {
            form = Form.form(Credentials.class).bindFromRequest();
            // Check errors
            if (form.hasErrors()) {
                return badRequest(views.html.sample18.render(false, null, form));
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
            String convertType = Utils.getFormValue(body, "convertType");
            String callbackUrl = Utils.getFormValue(body, "callbackUrl");
            callbackUrl = (callbackUrl == null) ? "" : callbackUrl;
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
                    UploadResponse uploadResponse = storageApi.Upload(credentials.getClientId(), file.getFilename(), "uploaded", "", false, new FileStream(is));
                    // Check response status
                    uploadResponse = Utils.assertResponse(uploadResponse);
                    guid = uploadResponse.getResult().getGuid();
                }
                guid = Utils.assertNotNull(guid);
                // Render view
                AsyncApi api = new AsyncApi();
                // Initialize API with base path
                api.setBasePath(credentials.getBasePath());
                ConvertResponse response = api.Convert(credentials.getClientId(), guid, "", "description", false, callbackUrl, convertType);
                // Check response status
                response = Utils.assertResponse(response);
                Double jobId = response.getResult().getJob_id();
                Thread.sleep(5000);
                GetJobDocumentsResponse jobDocumentsResponse = api.GetJobDocuments(credentials.getClientId(), jobId.toString(), "");
                jobDocumentsResponse = Utils.assertResponse(jobDocumentsResponse);

                String resultGuid = jobDocumentsResponse.getResult().getInputs().get(0).getOutputs().get(0).getGuid();

                FileOutputStream fileOutputStream = new FileOutputStream(USER_INFO_FILE);
                DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream);

                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(credentials.getClientId());
                stringBuilder.append("|");
                stringBuilder.append(credentials.getPrivateKey());
                stringBuilder.append("|");
                stringBuilder.append(credentials.getBasePath());
                dataOutputStream.writeUTF(stringBuilder.toString());
                dataOutputStream.flush();
                fileOutputStream.close();

                return ok(views.html.sample18.render(true, resultGuid, form));
            } catch (Exception e) {
                return badRequest(views.html.sample18.render(false, null, form));
            }
        } else if (Utils.isGET(request())) {
            form = form.bind(session());
        }
        return ok(views.html.sample18.render(false, null, form));
    }
}