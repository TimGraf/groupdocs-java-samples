//###<i>This sample will show how to use <b>GetFile</b> method from Storage Api to download a file from GroupDocs Storage</i>
package controllers;
//Import of necessary libraries

import com.groupdocs.sdk.api.AsyncApi;
import com.groupdocs.sdk.api.ComparisonApi;
import com.groupdocs.sdk.api.MgmtApi;
import com.groupdocs.sdk.api.StorageApi;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.FileStream;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.CompareResponse;
import com.groupdocs.sdk.model.GetJobDocumentsResponse;
import com.groupdocs.sdk.model.GetUserEmbedKeyResponse;
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

public class Sample19 extends Controller {
    public static String USER_INFO_FILE = "UserInfo_sample19.tmp";
    //
    protected static Form<Credentials> form = Form.form(Credentials.class);

    public static Result index() {

        if (Utils.isPOST(request())) {
            form = Form.form(Credentials.class).bindFromRequest();
            // Check errors
            if (form.hasErrors()) {
                return badRequest(views.html.sample19.render(false, null, null, null, form));
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
            String callbackUrl = Utils.getFormValue(body, "callbackUrl");
            // Initialize SDK with private key
            ApiInvoker.getInstance().setRequestSigner(
                    new GroupDocsRequestSigner(credentials.getPrivateKey()));

            try {
                //
                String sourseGuid = null;
                //
                if ("guid".equals(sourse)) { // File GUID
                    sourseGuid = Utils.getFormValue(body, "sourceFileId");
                } else if ("url".equals(sourse)) { // Upload file fron URL
                    String url = Utils.getFormValue(body, "url");
                    StorageApi storageApi = new StorageApi();
                    // Initialize API with base path
                    storageApi.setBasePath(credentials.getBasePath());
                    UploadResponse uploadResponse = storageApi.UploadWeb(credentials.getClientId(), url);
                    // Check response status
                    uploadResponse = Utils.assertResponse(uploadResponse);
                    sourseGuid = uploadResponse.getResult().getGuid();
                } else if ("local".equals(sourse)) { // Upload local file
                    Http.MultipartFormData.FilePart file = body.getFile("local");
                    StorageApi storageApi = new StorageApi();
                    // Initialize API with base path
                    storageApi.setBasePath(credentials.getBasePath());
                    FileInputStream is = new FileInputStream(file.getFile());
                    UploadResponse uploadResponse = storageApi.Upload(credentials.getClientId(), file.getFilename(), "uploaded", "", 2, new FileStream(is));
                    // Check response status
                    uploadResponse = Utils.assertResponse(uploadResponse);
                    sourseGuid = uploadResponse.getResult().getGuid();
                }
                // Target
                String targetGuid = null;
                //
                if ("guid".equals(sourse)) { // File GUID
                    targetGuid = Utils.getFormValue(body, "targetFileId");
                } else if ("url".equals(sourse)) { // Upload file fron URL
                    String url = Utils.getFormValue(body, "targetUrl");
                    StorageApi storageApi = new StorageApi();
                    // Initialize API with base path
                    storageApi.setBasePath(credentials.getBasePath());
                    UploadResponse uploadResponse = storageApi.UploadWeb(credentials.getClientId(), url);
                    // Check response status
                    uploadResponse = Utils.assertResponse(uploadResponse);
                    targetGuid = uploadResponse.getResult().getGuid();
                } else if ("local".equals(sourse)) { // Upload local file
                    Http.MultipartFormData.FilePart file = body.getFile("target_local");
                    StorageApi storageApi = new StorageApi();
                    // Initialize API with base path
                    storageApi.setBasePath(credentials.getBasePath());
                    FileInputStream is = new FileInputStream(file.getFile());
                    UploadResponse uploadResponse = storageApi.Upload(credentials.getClientId(), file.getFilename(), "uploaded", "", 2, new FileStream(is));
                    // Check response status
                    uploadResponse = Utils.assertResponse(uploadResponse);
                    targetGuid = uploadResponse.getResult().getGuid();
                }
                sourseGuid = Utils.assertNotNull(sourseGuid);
                targetGuid = Utils.assertNotNull(targetGuid);

                ComparisonApi api = new ComparisonApi();
                // Initialize API with base path
                api.setBasePath(credentials.getBasePath());
                callbackUrl = callbackUrl == null ? "" : callbackUrl;

                CompareResponse compareResponse = api.Compare(credentials.getClientId(), sourseGuid, targetGuid, callbackUrl);
                compareResponse = Utils.assertResponse(compareResponse);

                AsyncApi asyncApi = new AsyncApi();
                // Initialize API with base path
                asyncApi.setBasePath(credentials.getBasePath());
                //
                GetJobDocumentsResponse jobDocumentsResponse = null;
                do {
                    Thread.sleep(5000);
                    jobDocumentsResponse = asyncApi.GetJobDocuments(credentials.getClientId(), compareResponse.getResult().getJob_id().toString(), "");
                    jobDocumentsResponse = Utils.assertResponse(jobDocumentsResponse);
                } while ("Inprogress".equalsIgnoreCase(jobDocumentsResponse.getResult().getJob_status()));
                String resultGuid = jobDocumentsResponse.getResult().getOutputs().get(0).getGuid();
                Utils.assertNotNull(resultGuid);

                MgmtApi mgmtApi = new MgmtApi();
                // Initialize API with base path
                mgmtApi.setBasePath(credentials.getBasePath());
                GetUserEmbedKeyResponse userEmbedKeyResponse = mgmtApi.GetUserEmbedKey(credentials.getClientId(), "comparison");
                Utils.assertResponse(userEmbedKeyResponse);
                String compareKey = userEmbedKeyResponse.getResult().getKey().getGuid();

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

                String server = credentials.getBasePath().substring(0, credentials.getBasePath().indexOf(".com") + 4).replace("api", "apps");

                // Render view
                return ok(views.html.sample19.render(true, resultGuid, compareKey, server, form));
            } catch (Exception e) {
                return badRequest(views.html.sample19.render(false, null, null, null, form));
            }
        } else if (Utils.isGET(request())) {
            form = form.bind(session());
        }
        return ok(views.html.sample19.render(false, null, null, null, form));
    }
}