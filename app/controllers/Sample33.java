package controllers;

import com.groupdocs.sdk.api.AsyncApi;
import com.groupdocs.sdk.api.StorageApi;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.*;
import common.Utils;
import models.Credentials;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

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
public class Sample33 extends Controller {
    //
    protected static Form<Credentials> form = Form.form(Credentials.class);

    public static Result index() {

        if (Utils.isPOST(request())) {
            form = Form.form(Credentials.class).bindFromRequest();
            // Check errors
            if (form.hasErrors()) {
                return badRequest(views.html.sample33.render(false, null, form));
            }
            // Save credentials to session
            Credentials credentials = form.get();
            session().put("clientId", credentials.getClientId());
            session().put("privateKey", credentials.getPrivateKey());
            session().put("basePath", credentials.getBasePath());
            credentials.normalizeBasePath("https://api.groupdocs.com/v2.0");
            // Get request parameters
            Http.MultipartFormData body = request().body().asMultipartFormData();
            String url1 = Utils.getFormValue(body, "url1");
            String url2 = Utils.getFormValue(body, "url2");
            String url3 = Utils.getFormValue(body, "url3");
            // Initialize SDK with private key
            ApiInvoker.getInstance().setRequestSigner(
                    new GroupDocsRequestSigner(credentials.getPrivateKey()));

            try {
                // Create Api objects
                StorageApi storageApi = new StorageApi();
                AsyncApi asyncApi = new AsyncApi();
                // Set base path
                storageApi.setBasePath(credentials.getBasePath());
                asyncApi.setBasePath(credentials.getBasePath());

                List<String> guids = new ArrayList<String>();
                for (String url : Arrays.asList(url1, url2, url3)) {
                    UploadResponse uploadResponse = storageApi.UploadWeb(credentials.getClientId(), url);
                    uploadResponse = Utils.assertResponse(uploadResponse);
                    guids.add(uploadResponse.getResult().getGuid());
                }

                // Create job info object
                JobInfo jobInfo = new JobInfo();
                jobInfo.setActions("convert, combine");
                jobInfo.setOut_formats(Arrays.asList("pdf"));
                jobInfo.setStatus("-1");
                jobInfo.setEmail_results(true);
                jobInfo.setName("Test Job " + Long.toString(org.joda.time.DateTime.now().getMillis()));
                // Create new job
                CreateJobResponse createJobResponse = asyncApi.CreateJob(credentials.getClientId(), jobInfo);
                createJobResponse = Utils.assertResponse(createJobResponse);
                String jobId = Double.toString(createJobResponse.getResult().getJob_id());
                // Add uploaded documents to job
                for (String guid : guids) {
                    // Add uploaded documents to job
                    AddJobDocumentResponse addJobDocumentResponse = asyncApi.AddJobDocument(credentials.getClientId(), jobId, guid, false, null);
                    addJobDocumentResponse = Utils.assertResponse(addJobDocumentResponse);
                }
                // Change job status
                jobInfo.setStatus("0");
                // Update job with new status
                UpdateJobResponse updateJobResponse = asyncApi.UpdateJob(credentials.getClientId(), jobId, jobInfo);
                updateJobResponse = Utils.assertResponse(updateJobResponse);
                // Delay for server proccesing
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
                String iframeUrl = server + "/document-viewer/embed/" + getJobDocumentsResponse.getResult().getOutputs().get(0).getGuid();
                //
                GroupDocsRequestSigner groupDocsRequestSigner = new GroupDocsRequestSigner(credentials.getPrivateKey());
                String signedIframeUrl = groupDocsRequestSigner.signUrl(iframeUrl);

                // Render view
                return ok(views.html.sample33.render(true, signedIframeUrl, form));
            } catch (Exception e) {
                return badRequest(views.html.sample33.render(false, null, form));
            }
        } else if (Utils.isGET(request())) {
            form = form.bind(session());
        }
        return ok(views.html.sample33.render(false, null, form));
    }
}
