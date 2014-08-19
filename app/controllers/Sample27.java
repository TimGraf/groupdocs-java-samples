//###<i>This sample will show how to use <b>Upload</b> method from Storage Api to upload file to GroupDocs Storage </i>
package controllers;
//Import of necessary libraries

import com.groupdocs.sdk.api.AsyncApi;
import com.groupdocs.sdk.api.MergeApi;
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

public class Sample27 extends Controller {
    //
    protected static Form<Credentials> form = Form.form(Credentials.class);

    public static Result index() {

        if (Utils.isPOST(request())) {
            form = Form.form(Credentials.class).bindFromRequest();
            // Check errors
            if (form.hasErrors()) {
                return badRequest(views.html.sample27.render(false, null, null, form));
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
            String name = Utils.getFormValue(body, "name");
            String sex = Utils.getFormValue(body, "sex");
            String age = Utils.getFormValue(body, "age");
            String sunrise = Utils.getFormValue(body, "sunrise");
            String type = Utils.getFormValue(body, "type");

            callbackUrl = (callbackUrl == null) ? "" : callbackUrl;

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
                //
                MergeApi mergeApi = new MergeApi();
                // Initialize API with base path
                mergeApi.setBasePath(credentials.getBasePath());

                Datasource datasource = new Datasource();
                datasource.setFields(new ArrayList<DatasourceField>());

                DatasourceField datasourceField = null;

                datasourceField = new DatasourceField();
                datasourceField.setName("sex");
                datasourceField.setType("text");
                datasourceField.setValues(new ArrayList<String>());
                datasourceField.getValues().add(sex);
                datasource.getFields().add(datasourceField);

                datasourceField = new DatasourceField();
                datasourceField.setName("age");
                datasourceField.setType("text");
                datasourceField.setValues(new ArrayList<String>());
                datasourceField.getValues().add(age);
                datasource.getFields().add(datasourceField);

                datasourceField = new DatasourceField();
                datasourceField.setName("sunrise");
                datasourceField.setType("text");
                datasourceField.setValues(new ArrayList<String>());
                datasourceField.getValues().add(sunrise);
                datasource.getFields().add(datasourceField);

                datasourceField = new DatasourceField();
                datasourceField.setName("name");
                datasourceField.setType("text");
                datasourceField.setValues(new ArrayList<String>());
                datasourceField.getValues().add(name);
                datasource.getFields().add(datasourceField);

                AddDatasourceResponse datasourceResponse = mergeApi.AddDataSource(credentials.getClientId(), datasource);
                // Check response status
                datasourceResponse = Utils.assertResponse(datasourceResponse);

                MergeTemplateResponse mergeTemplateResponse = mergeApi.MergeDatasource(credentials.getClientId(), guid, Double.toString(datasourceResponse.getResult().getDatasource_id()), type, null);
                // Check response status
                mergeTemplateResponse = Utils.assertResponse(mergeTemplateResponse);

                Thread.sleep(8000);

                AsyncApi asyncApi = new AsyncApi();
                // Initialize API with base path
                asyncApi.setBasePath(credentials.getBasePath());

                GetJobDocumentsResponse jobDocumentsResponse = asyncApi.GetJobDocuments(credentials.getClientId(), Double.toString(mergeTemplateResponse.getResult().getJob_id()), null);
                // Check response status
                jobDocumentsResponse = Utils.assertResponse(jobDocumentsResponse);

                if ("Postponed".equalsIgnoreCase(jobDocumentsResponse.getResult().getJob_status())) {
                    throw new Exception("Job is failed");
                }

                if ("Pending".equalsIgnoreCase(jobDocumentsResponse.getResult().getJob_status())) {
                    throw new Exception("Job is pending");
                }

                String resultGuid = jobDocumentsResponse.getResult().getInputs().get(0).getOutputs().get(0).getGuid();
                String resultName = jobDocumentsResponse.getResult().getInputs().get(0).getOutputs().get(0).getName();

                // Download file
                SharedApi api = new SharedApi();
                // Initialize API with base path
                api.setBasePath(credentials.getBasePath());
                // Get file from storage
                FileStream fileStream = api.Download(resultGuid, resultName, false);
                // Check request result
                fileStream = Utils.assertNotNull(fileStream);
                Utils.assertNotNull(fileStream.getInputStream());

                String separator = System.getProperty("file.separator");
                String path = new File(".").getAbsolutePath();
                String downloadPath = path + separator + "public" + separator + "images" + separator;
                FileOutputStream newFile = new FileOutputStream(downloadPath + resultName);
                // Write file to local folder
                IOUtils.copy(fileStream.getInputStream(), newFile);
                IOUtils.closeQuietly(fileStream.getInputStream());

                newFile.flush();
                newFile.close();

                // Render view
                return ok(views.html.sample27.render(true, resultGuid, downloadPath + resultName, form));
            } catch (Exception e) {
                return badRequest(views.html.sample27.render(false, null, null, form));
            }
        } else if (Utils.isGET(request())) {
            form = form.bind(session());
        }
        return ok(views.html.sample27.render(false, null, null, form));
    }
}