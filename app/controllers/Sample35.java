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
import play.data.Form;
import play.mvc.Http;
import play.mvc.Result;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Sample35 extends BaseController {
    //
    protected static Form<Credentials> form = Form.form(Credentials.class);

    public static Result index() {

        if (Utils.isPOST(request())) {
            Http.MultipartFormData body = request().body().asMultipartFormData();
            if (Utils.getFormValue(body, "merge")  == null) {
                form = Form.form(Credentials.class).bindFromRequest();
                // Check errors
                if (form.hasErrors()) {
                    return badRequest(views.html.sample35.render(false, null, null, form, null, null));
                }
                // Save credentials to session
                Credentials credentials = form.get();
                session().put("clientId", credentials.getClientId());
                session().put("privateKey", credentials.getPrivateKey());
                session().put("basePath", credentials.getBasePath());
                credentials.normalizeBasePath("https://api.groupdocs.com/v2.0");
                // Get request parameters
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
                        UploadResponse uploadResponse = storageApi.Upload(credentials.getClientId(), file.getFilename(), "uploaded", "", false, new FileStream(is));
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
                    List<TemplateField> fields = templateFieldsResponse.getResult().getFields();

                    return ok(views.html.sample35.render(true, "show:form", fields, form, guid, null));
                } catch (Exception e) {
                    return badRequest(views.html.sample35.render(false, null, null, form, null, null));
                }
            } else {
                String guid = Utils.getFormValue(body, "guid");
                Map<String, String[]> inputData = body.asFormUrlEncoded();
                Datasource datasource = new Datasource();
                for (String key : inputData.keySet()) {
                    if (!"guid".equalsIgnoreCase(key) && !"merge".equalsIgnoreCase(key)) {
                        String[] values = inputData.get(key);
                        if (values.length > 0) {
                            DatasourceField datasourceField = new DatasourceField();
                            datasourceField.setName(key);
                            //datasourceField.setType();
                            datasourceField.setValues(Arrays.asList(values));
                            datasource.getFields().add(datasourceField);
                        }
                    }
                }
                String clientId = session().get("clientId");
                String privateKey = session().get("privateKey");
                String basePath = session().get("basePath");
                MergeApi mergeApi = new MergeApi();
                AsyncApi asyncApi = new AsyncApi();
                if (!basePath.isEmpty()) {
                    mergeApi.setBasePath(basePath);
                    asyncApi.setBasePath(basePath);
                }
                try {
                    AddDatasourceResponse addDatasourceResponse = mergeApi.AddDataSource(clientId, datasource);
                    Utils.assertResponse(addDatasourceResponse);
                    Double dataSourceId = addDatasourceResponse.getResult().getDatasource_id();
                    MergeTemplateResponse mergeTemplateResponse = mergeApi.MergeDatasource(clientId, guid, Double.toString(dataSourceId), "pdf", null);
                    Utils.assertResponse(mergeTemplateResponse);
                    String jobId = Double.toString(mergeTemplateResponse.getResult().getJob_id());

                    GetJobDocumentsResponse getJobDocumentsResponse = null;
                    for (int ind : new Integer[]{1, 2, 3, 4, 5}){
                        Thread.sleep(5000);
                        getJobDocumentsResponse = asyncApi.GetJobDocuments(clientId, jobId, null);
                        Utils.assertResponse(getJobDocumentsResponse);
                        String jobStatus = getJobDocumentsResponse.getResult().getJob_status();
                        if ("Completed".equalsIgnoreCase(jobStatus) || "Archived".equalsIgnoreCase(jobStatus)){
                            break;
                        } else if ("Postponed".equalsIgnoreCase(jobStatus)){
                            return badRequest(views.html.sample35.render(false, null, null, form, null, null));
                        }
                    }
                    Utils.assertNotNull(getJobDocumentsResponse);
                    String resGuid = getJobDocumentsResponse.getResult().getInputs().get(0).getOutputs().get(0).getGuid();
                    Utils.assertNotNull(resGuid);

                    String viewUrl = apiPath2framePath(mergeApi.getBasePath(), "/document-viewer/embed/" + resGuid);
                    String signedUrl = signUrl(privateKey, viewUrl);

                    return ok(views.html.sample35.render(true, "show:doc", null, form, null, signedUrl));
                } catch (Exception e) {
                    return badRequest(views.html.sample35.render(false, null, null, form, null, null));
                }
            }
        } else if (Utils.isGET(request())) {
            form = form.bind(session());
        }
        return ok(views.html.sample35.render(false, null, null, form, null, null));
    }
}