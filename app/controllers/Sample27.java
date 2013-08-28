//###<i>This sample will show how to use <b>Upload</b> method from Storage Api to upload file to GroupDocs Storage </i>
package controllers;
//Import of necessary libraries

import com.groupdocs.sdk.api.*;
import com.groupdocs.sdk.common.ApiException;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.FileStream;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.*;
import common.Utils;
import models.Credentials;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import scala.actors.threadpool.Arrays;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Sample27 extends Controller {
    static String title = "GroupDocs Java SDK Samples";
    static String sample = "Sample27";
    static Form<Credentials> form = form(Credentials.class);

    public static Result index() {
        HashMap<String, String> data = new HashMap<String, String>();
        Http.Request request = request();
        Form<Credentials> filledForm = form.bind(session());

        if ("GET".equalsIgnoreCase(request.method())) {
            session().put("server_type", "https://api.groupdocs.com/v2.0");
            return ok(views.html.sample27.render(sample, data, filledForm));
        }
        if ("POST".equalsIgnoreCase(request.method())) {
            filledForm = form.bindFromRequest();
            Credentials credentials = filledForm.get();

            if (StringUtils.isNotEmpty(credentials.getClient_id()) || StringUtils.isNotEmpty(credentials.getPrivate_key())) {
                session().put("client_id", credentials.getClient_id());
                session().put("private_key", credentials.getPrivate_key());
                session().put("server_type", credentials.getServer_type());
            }

            Http.MultipartFormData multipartFormData = request.body().asMultipartFormData();
            Map<String, String[]> formUrlEncodedData = multipartFormData.asFormUrlEncoded();

            String sourse = Utils.getFormValue(formUrlEncodedData, "sourse");
            String convert_type = Utils.getFormValue(formUrlEncodedData, "convert_type");
            String callbackUrl = Utils.getFormValue(formUrlEncodedData, "callbackUrl");
            callbackUrl = (callbackUrl == null) ? "" : callbackUrl;
            String name = Utils.getFormValue(formUrlEncodedData, "name");
            String sex = Utils.getFormValue(formUrlEncodedData, "sex");
            String age = Utils.getFormValue(formUrlEncodedData, "age");
            String sunrise = Utils.getFormValue(formUrlEncodedData, "sunrise");
            String type = Utils.getFormValue(formUrlEncodedData, "type");

            String guid = null;
            if ("guid".equalsIgnoreCase(sourse)) {
                guid = Utils.getFormValue(formUrlEncodedData, "fileId");
            } else if ("url".equalsIgnoreCase(sourse)) {
                try {
                    String url = Utils.getFormValue(formUrlEncodedData, "url");
                    guid = Utils.getGuidByUrl(credentials.getClient_id(), credentials.getPrivate_key(), credentials.getServer_type(), url);
                } catch (Exception e) {
                    filledForm.reject(e.getMessage());
                    e.printStackTrace();
                    return ok(views.html.sample27.render(sample, data, filledForm));
                }
            } else if ("local".equalsIgnoreCase(sourse)) {
                try {
                    Http.MultipartFormData.FilePart file = multipartFormData.getFile("file");
                    guid = Utils.getGuidByFile(credentials.getClient_id(), credentials.getPrivate_key(), credentials.getServer_type(), file.getFilename(), new FileStream(new FileInputStream(file.getFile())));
                } catch (Exception e) {
                    filledForm.reject(e.getMessage());
                    e.printStackTrace();
                    return ok(views.html.sample27.render(sample, data, filledForm));
                }
            }
            if (StringUtils.isEmpty(guid)) {
                filledForm.reject("GUID is empty or null!");
                return ok(views.html.sample27.render(sample, data, filledForm));
            }

            try {

                ApiInvoker.getInstance().setRequestSigner(new GroupDocsRequestSigner(credentials.getPrivate_key()));
                MergeApi mergeApi = new MergeApi();
                mergeApi.setBasePath(credentials.getServer_type());

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

                AddDatasourceResponse datasourceResponse = mergeApi.AddDataSource(credentials.getClient_id(), datasource);
                datasourceResponse = Utils.assertResponse(datasourceResponse);

                MergeTemplateResponse mergeTemplateResponse = mergeApi.MergeDatasource(credentials.getClient_id(), guid, Double.toString(datasourceResponse.getResult().getDatasource_id()), type, null);
                mergeTemplateResponse = Utils.assertResponse(mergeTemplateResponse);

                Thread.sleep(8000);

                AsyncApi asyncApi = new AsyncApi();
                asyncApi.setBasePath(credentials.getServer_type());

                GetJobDocumentsResponse jobDocumentsResponse = asyncApi.GetJobDocuments(credentials.getClient_id(), Double.toString(mergeTemplateResponse.getResult().getJob_id()), null);
                jobDocumentsResponse = Utils.assertResponse(jobDocumentsResponse);

                if ("Postponed".equalsIgnoreCase(jobDocumentsResponse.getResult().getJob_status())){
                    throw new Exception("Job is failed");
                }

                if ("Pending".equalsIgnoreCase(jobDocumentsResponse.getResult().getJob_status())){
                    throw new Exception("Job is pending");
                }

                String resultGuid = jobDocumentsResponse.getResult().getInputs().get(0).getOutputs().get(0).getGuid();
                String resultName = jobDocumentsResponse.getResult().getInputs().get(0).getOutputs().get(0).getName();

                // Download filr
                SharedApi api = new SharedApi();
                api.setBasePath(credentials.getServer_type());
                //###Make a request to Storage API using clientId

                //Get file from storage
                FileStream resp = api.Download(resultGuid, resultName, false);
                //Check request result
                if(resp == null || resp.getInputStream() == null){
                    throw new Exception("Not Found");
                }

                String separator = System.getProperty("file.separator");
                String path = new File(".").getAbsolutePath();
                String downloadPath = path + separator + "public" + separator + "images" + separator;
                FileOutputStream newFile = new FileOutputStream(downloadPath + resultName);
                // Write file to local folder
                IOUtils.copy(resp.getInputStream(), newFile);
                IOUtils.closeQuietly(resp.getInputStream());

                newFile.flush();
                newFile.close();

                data.put("filePath", downloadPath + resultName);
                data.put("fileGuid", resultGuid);

            } catch (Exception e) {
                filledForm.reject(e.getMessage());
                return ok(views.html.sample27.render(sample, data, filledForm));
            }
        }
        return ok(views.html.sample27.render(sample, data, filledForm));
    }
}