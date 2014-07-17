package controllers;

import com.groupdocs.sdk.api.AsyncApi;
import com.groupdocs.sdk.api.SharedApi;
import com.groupdocs.sdk.api.StorageApi;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.FileStream;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.*;
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
public class Sample42 extends Controller {
    //
    protected static Form<Credentials> form = Form.form(Credentials.class);

    public static Result index() {

        if (Utils.isPOST(request())) {
            form = Form.form(Credentials.class).bindFromRequest();
            // Check errors
            if (form.hasErrors()) {
                return badRequest(views.html.sample42.render(false, null, null, form));
            }
            // Save credentials to session
            Credentials credentials = form.get();
            session().put("clientId", credentials.getClientId());
            session().put("privateKey", credentials.getPrivateKey());
            session().put("basePath", credentials.getBasePath());
            credentials.normalizeBasePath("https://api.groupdocs.com/v2.0");
            // Get request parameters
            Http.MultipartFormData body = request().body().asMultipartFormData();
            String fileId = Utils.getFormValue(body, "fileId");
            // Initialize SDK with private key
            ApiInvoker.getInstance().setRequestSigner(
                    new GroupDocsRequestSigner(credentials.getPrivateKey()));

            try {

                // Create Api objects
                AsyncApi asyncApi = new AsyncApi();
                // Set base path
                asyncApi.setBasePath(credentials.getBasePath());

                // Create job info object
                JobInfo jobInfo = new JobInfo();
                jobInfo.setActions("512");
                jobInfo.setName("Sample");
                // Create new job
                CreateJobResponse createJobResponse = asyncApi.CreateJob(credentials.getClientId(), jobInfo);
                createJobResponse = Utils.assertResponse(createJobResponse);
                String jobId = Double.toString(createJobResponse.getResult().getJob_id());
                // Add uploaded documents to job
                AddJobDocumentResponse addJobDocumentResponse = asyncApi.AddJobDocument(credentials.getClientId(), jobId, fileId, false, "pdf");
                addJobDocumentResponse = Utils.assertResponse(addJobDocumentResponse);
                // Change job status
                jobInfo.setStatus("0");
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
                // Download file
                SharedApi api = new SharedApi();
                // Initialize API with base path
                api.setBasePath(credentials.getBasePath());
                // Get file from storage
                FileStream fileStream = api.Download(getJobDocumentsResponse.getResult().getInputs().get(0).getOutputs().get(0).getGuid(), getJobDocumentsResponse.getResult().getInputs().get(0).getOutputs().get(0).getName(), false);
                // Check request result
                fileStream = Utils.assertNotNull(fileStream);
                Utils.assertNotNull(fileStream.getInputStream());

                String separator = System.getProperty("file.separator");
                String path = new File(".").getAbsolutePath();
                String downloadPath = path + separator + "public" + separator + "images" + separator;
                FileOutputStream newFile = new FileOutputStream(downloadPath + getJobDocumentsResponse.getResult().getInputs().get(0).getOutputs().get(0).getName());
                // Write file to local folder
                IOUtils.copy(fileStream.getInputStream(), newFile);
                IOUtils.closeQuietly(fileStream.getInputStream());

                newFile.flush();
                newFile.close();
                String filePath = downloadPath + getJobDocumentsResponse.getResult().getInputs().get(0).getOutputs().get(0).getName();
                //
                String server = credentials.getBasePath().substring(0, credentials.getBasePath().indexOf(".com") + 4).replace("api", "apps");
                String iframeUrl = server + "/document-annotation/embed/" + fileId;
                //
                GroupDocsRequestSigner groupDocsRequestSigner = new GroupDocsRequestSigner(credentials.getPrivateKey());
                String signedIframeUrl = groupDocsRequestSigner.signUrl(iframeUrl);

                // Render view
                return ok(views.html.sample42.render(true, signedIframeUrl, filePath, form));
            } catch (Exception e) {
                return badRequest(views.html.sample42.render(false, null, null, form));
            }
        } else if (Utils.isGET(request())) {
            form = form.bind(session());
        }
        return ok(views.html.sample42.render(false, null, null, form));
    }
}
