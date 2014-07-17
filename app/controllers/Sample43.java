package controllers;

import com.groupdocs.sdk.api.AsyncApi;
import com.groupdocs.sdk.api.StorageApi;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.FileStream;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.*;
import common.Utils;
import models.Credentials;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: liosha
 * Date: 25.10.13
 * Time: 11:52
 * To change this template use File | Settings | File Templates.
 */
public class Sample43 extends Controller {
    //
    protected static Form<Credentials> form = Form.form(Credentials.class);

    public static Result index() {

        if (Utils.isPOST(request())) {
            form = Form.form(Credentials.class).bindFromRequest();
            // Check errors
            if (form.hasErrors()) {
                return badRequest(views.html.sample43.render(false, null, form));
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
                    UploadResponse uploadResponse = storageApi.Upload(credentials.getClientId(), file.getFilename(), "uploaded", "", new FileStream(is));
                    // Check response status
                    uploadResponse = Utils.assertResponse(uploadResponse);
                    guid = uploadResponse.getResult().getGuid();
                }
                guid = Utils.assertNotNull(guid);


                // Create Api objects
                StorageApi storageApi = new StorageApi();
                AsyncApi asyncApi = new AsyncApi();
                // Set base path
                storageApi.setBasePath(credentials.getBasePath());
                asyncApi.setBasePath(credentials.getBasePath());

                // Create job info object
                JobInfo jobInfo = new JobInfo();
                jobInfo.setActions("convert, numberlines");
                jobInfo.setOut_formats(Arrays.asList("doc"));
                jobInfo.setName("Sample");
                // Create new job
                CreateJobResponse createJobResponse = asyncApi.CreateJob(credentials.getClientId(), jobInfo);
                createJobResponse = Utils.assertResponse(createJobResponse);
                String jobId = Double.toString(createJobResponse.getResult().getJob_id());
                // Add uploaded documents to job

                AddJobDocumentResponse addJobDocumentResponse = asyncApi.AddJobDocument(credentials.getClientId(), jobId, guid, false, null);
                addJobDocumentResponse = Utils.assertResponse(addJobDocumentResponse);
                // Change job status
                jobInfo.setStatus("pending");
                // Update job with new status
                UpdateJobResponse updateJobResponse = asyncApi.UpdateJob(credentials.getClientId(), jobId, jobInfo);
                updateJobResponse = Utils.assertResponse(updateJobResponse);

                // Get result document guid from job
                GetJobDocumentsResponse getJobDocumentsResponse = null;
                int count = 10;
                do {
                    Thread.sleep(5000);
                    getJobDocumentsResponse = asyncApi.GetJobDocuments(credentials.getClientId(), jobId, null);
                    getJobDocumentsResponse = Utils.assertResponse(getJobDocumentsResponse);
                } while ("Inprogress".equalsIgnoreCase(getJobDocumentsResponse.getResult().getJob_status()) && (count--) > 0);
                //
                String server = credentials.getBasePath().substring(0, credentials.getBasePath().indexOf(".com") + 4).replace("api", "apps");
                String iframeUrl = server + "/document-viewer/embed/" + getJobDocumentsResponse.getResult().getInputs().get(0).getOutputs().get(0).getGuid();
                //
                GroupDocsRequestSigner groupDocsRequestSigner = new GroupDocsRequestSigner(credentials.getPrivateKey());
                String signedIframeUrl = groupDocsRequestSigner.signUrl(iframeUrl);

                // Render view
                return ok(views.html.sample43.render(true, signedIframeUrl, form));
            } catch (Exception e) {
                return badRequest(views.html.sample43.render(false, null, form));
            }
        } else if (Utils.isGET(request())) {
            form = form.bind(session());
        }
        return ok(views.html.sample43.render(false, null, form));
    }
}
