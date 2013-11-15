//###<i>This sample will show how to use <b>Upload</b> method from Storage Api to upload file to GroupDocs Storage </i>
package controllers;
//Import of necessary libraries

import com.groupdocs.sdk.api.AsyncApi;
import com.groupdocs.sdk.api.DocApi;
import com.groupdocs.sdk.api.MergeApi;
import com.groupdocs.sdk.api.StorageApi;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.FileStream;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.*;
import common.Utils;
import models.Credentials;
import org.apache.log4j.lf5.util.StreamUtils;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class Sample25 extends Controller {
    //
    protected static Form<Credentials> form = Form.form(Credentials.class);

    public static Result index() {

        if (Utils.isPOST(request())) {
            form = Form.form(Credentials.class).bindFromRequest();
            // Check errors
            if (form.hasErrors()) {
                return badRequest(views.html.sample25.render(false, null, null, form));
            }
            // Save credentials to session
            Credentials credentials = form.get();
            session().put("clientId", credentials.getClientId());
            session().put("privateKey", credentials.getPrivateKey());
            session().put("basePath", credentials.getBasePath());
            credentials.normalizeBasePath("https://api.groupdocs.com/v2.0");
            // Get request parameters
            Http.MultipartFormData body = request().body().asMultipartFormData();
            String width = Utils.getFormValue(body, "width");
            String height = Utils.getFormValue(body, "height");
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

                DocApi docApi = new DocApi();
                // Initialize API with base path
                docApi.setBasePath(credentials.getBasePath());
                // Get all fields from template
                TemplateFieldsResponse templateFieldsResponse = docApi.GetTemplateFields(credentials.getClientId(), guid, false);
                // Check response status
                templateFieldsResponse = Utils.assertResponse(templateFieldsResponse);
                // Create DataSource object
                Datasource dataSource = new Datasource();
                // Create empty list for DatasorceFiled
                List<DatasourceField> list = new ArrayList<DatasourceField>();
                // Create empty list for values
                List<String> values = new ArrayList<String>();
                // Put values to the list
                values.add("value1");
                values.add("value2");
                // Loop for fields creataion
                for (TemplateField templateField : templateFieldsResponse.getResult().getFields()) {
                    DatasourceField field = new DatasourceField();
                    field.setName(templateField.getName());
                    field.setType("text");
                    field.setValues(values);
                    list.add(field);
                }
                // Set fields list to the DataSource
                dataSource.setFields(list);

                MergeApi mergeApi = new MergeApi();
                // Initialize API with base path
                mergeApi.setBasePath(credentials.getBasePath());
                // Add DataSource to the GroupDocs
                AddDatasourceResponse addDataSource = mergeApi.AddDataSource(credentials.getClientId(), dataSource);
                // Check response status
                addDataSource = Utils.assertResponse(addDataSource);
                // Merge DataSource and convert to the pdf
                MergeTemplateResponse mergeTemplateResponse = mergeApi.MergeDatasource(credentials.getClientId(), guid, Double.toString(addDataSource.getResult().getDatasource_id()), "pdf", "");
                // Check response status
                mergeTemplateResponse = Utils.assertResponse(mergeTemplateResponse);

                Thread.sleep(2000);

                AsyncApi asyncApi = new AsyncApi();
                // Initialize API with base path
                asyncApi.setBasePath(credentials.getBasePath());
                // Check job status
                GetJobDocumentsResponse jobDocumentsResponse = null;
                for (int n = 0; n < 5; n++) {
                    jobDocumentsResponse = asyncApi.GetJobDocuments(credentials.getClientId(), Double.toString(mergeTemplateResponse.getResult().getJob_id()), "");
                    jobDocumentsResponse = Utils.assertResponse(jobDocumentsResponse);
                    String jobStatus = jobDocumentsResponse.getResult().getJob_status().trim();
                    if ("Completed".equalsIgnoreCase(jobStatus) || "Archived".equalsIgnoreCase(jobStatus)) {
                        break;
                    } else if ("Postponed".equalsIgnoreCase(jobStatus)) {
                        throw new Exception("Job is failed");
                    }
                    Thread.sleep(1000);
                }
                // Get result file guid and name
                guid = jobDocumentsResponse.getResult().getInputs().get(0).getOutputs().get(0).getGuid();
                String name = jobDocumentsResponse.getResult().getInputs().get(0).getOutputs().get(0).getName();
                // Get file from storage
                StorageApi storageApi = new StorageApi();
                // Initialize API with base path
                storageApi.setBasePath(credentials.getBasePath());
                FileStream fileStream = storageApi.GetFile(credentials.getClientId(), guid);
                // Check request result
                FileStream downloadedFile = Utils.assertNotNull(fileStream);
                Utils.assertNotNull(downloadedFile.getInputStream());
                // Check file name
                if (downloadedFile.getFileName() == null) {
                    downloadedFile.setFileName(name);
                }
                // Obtaining file stream of downloading file and definition of folder where to download file
                String separator = System.getProperty("file.separator");
                String path = new File("").getAbsolutePath();
                String downloadPath = path + separator + "public" + separator + "images" + separator;
                FileOutputStream newFile = new FileOutputStream(downloadPath + downloadedFile.getFileName());
                // Write file to local folder
                StreamUtils.copy(downloadedFile.getInputStream(), newFile);
                newFile.close();

                String server = credentials.getBasePath().substring(0, credentials.getBasePath().indexOf(".com") + 4).replace("api", "apps");
                String messages = "File was converted and downloaded to the " + downloadPath + downloadedFile.getFileName();
                // If request was successfull
                // Generation of iframe URL using $pageImage->result->guid
                // iframe to prodaction server
                String frameUrl = server + "/document-viewer/embed/" + guid;
                // Render view
                return ok(views.html.sample25.render(true, messages, frameUrl, form));
            } catch (Exception e) {
                return badRequest(views.html.sample25.render(false, null, null, form));
            }
        } else if (Utils.isGET(request())) {
            form = form.bind(session());
        }
        return ok(views.html.sample25.render(false, null, null, form));
    }
}