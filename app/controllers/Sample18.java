//###<i>This sample will show how to use <b>SetAnnotationCollaborators</b> method from Annotation Api to set collaborator for document</i>
package controllers;
//Import of necessary libraries

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import com.groupdocs.sdk.common.FileStream;
import com.groupdocs.sdk.model.*;
import common.Utils;
import models.Credentials;

import org.apache.commons.lang3.StringUtils;

import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import scala.actors.threadpool.Arrays;

import com.groupdocs.sdk.common.ApiException;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.api.AsyncApi;
import com.groupdocs.sdk.api.StorageApi;

public class Sample18 extends Controller {
    public static String USER_INFO_FILE = "UserInfo_sample18.tmp";
    static String title = "GroupDocs Java SDK Samples";
    static String sample = "Sample18";
    static Form<Credentials> form = form(Credentials.class);

    public static Result index() {
        HashMap<String, String> data = new HashMap<String, String>();
        Http.Request request = request();
        Form<Credentials> filledForm = form.bind(session());

        if ("GET".equalsIgnoreCase(request.method())) {
            session().put("server_type", "https://api.groupdocs.com/v2.0");
            return ok(views.html.sample18.render(title, sample, data, filledForm));
        }
        if ("POST".equalsIgnoreCase(request.method())) {
            filledForm = form.bindFromRequest();
            Credentials credentials = filledForm.get();

            if (StringUtils.isNotEmpty(credentials.client_id) || StringUtils.isNotEmpty(credentials.private_key)) {
                session().put("client_id", credentials.client_id);
                session().put("private_key", credentials.private_key);
                session().put("server_type", credentials.server_type);
            }

            Http.MultipartFormData multipartFormData = request.body().asMultipartFormData();
            Map<String, String[]> formUrlEncodedData = multipartFormData.asFormUrlEncoded();

            String sourse = Utils.getFormValue(formUrlEncodedData, "sourse");
            String convert_type = Utils.getFormValue(formUrlEncodedData, "convert_type");
            String callbackUrl = Utils.getFormValue(formUrlEncodedData, "callbackUrl");
            callbackUrl = (callbackUrl == null) ? "" : callbackUrl;
            String guid = null;
            if ("guid".equalsIgnoreCase(sourse)) {
                guid = Utils.getFormValue(formUrlEncodedData, "fileId");
            } else if ("url".equalsIgnoreCase(sourse)) {
                try {
                    String url = Utils.getFormValue(formUrlEncodedData, "url");
                    guid = Utils.getGuidByUrl(credentials.client_id, credentials.private_key, credentials.server_type, url);
                } catch (Exception e) {
                    filledForm.reject(e.getMessage());
                    e.printStackTrace();
                    return ok(views.html.sample18.render(title, sample, data, filledForm));
                }
            } else if ("local".equalsIgnoreCase(sourse)) {
                try {
                    Http.MultipartFormData.FilePart local = multipartFormData.getFile("local");
                    guid = Utils.getGuidByFile(credentials.client_id, credentials.private_key, credentials.server_type, local.getFilename(), new FileStream(new FileInputStream(local.getFile())));
                } catch (Exception e) {
                    filledForm.reject(e.getMessage());
                    e.printStackTrace();
                    return ok(views.html.sample18.render(title, sample, data, filledForm));
                }
            }
            if (StringUtils.isEmpty(guid)) {
                filledForm.reject("GUID is empty or null!");
                return ok(views.html.sample18.render(title, sample, data, filledForm));
            }

            try {

                ApiInvoker.getInstance().setRequestSigner(new GroupDocsRequestSigner(credentials.private_key));
                AsyncApi api = new AsyncApi();
                api.setBasePath(credentials.server_type);
                ConvertResponse response = api.Convert(credentials.client_id, guid, "", "description", false, callbackUrl, convert_type);
                response = Utils.assertResponse(response);
                Double jobId = response.getResult().getJob_id();
                Thread.sleep(5000);
                GetJobDocumentsResponse jobDocumentsResponse = api.GetJobDocuments(credentials.client_id, jobId.toString(), "");
                jobDocumentsResponse = Utils.assertResponse(jobDocumentsResponse);

                String resultGuid = jobDocumentsResponse.getResult().getInputs().get(0).getOutputs().get(0).getGuid();
                data.put("data", resultGuid);

                if (!StringUtils.isEmpty(callbackUrl)) {
                    FileOutputStream fileOutputStream = new FileOutputStream(USER_INFO_FILE);
                    DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream);

                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(credentials.client_id);
                    stringBuilder.append("|");
                    stringBuilder.append(credentials.private_key);
                    stringBuilder.append("|");
                    stringBuilder.append(credentials.server_type);

                    dataOutputStream.writeUTF(stringBuilder.toString());

                    dataOutputStream.flush();
                    fileOutputStream.close();
                }
            } catch (Exception e) {
                filledForm.reject(e.getMessage());
                return ok(views.html.sample18.render(title, sample, data, filledForm));
            }
        }
        return ok(views.html.sample18.render(title, sample, data, filledForm));
    }
}