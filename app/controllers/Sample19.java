 //###<i>This sample will show how to use <b>GetFile</b> method from Storage Api to download a file from GroupDocs Storage</i>
package controllers;
//Import of necessary libraries
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.groupdocs.sdk.api.MgmtApi;
import com.groupdocs.sdk.common.FileStream;
import com.groupdocs.sdk.model.GetUserEmbedKeyResponse;
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
import com.groupdocs.sdk.model.CompareResponse;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.api.ComparisonApi;
import com.groupdocs.sdk.api.AsyncApi;
import com.groupdocs.sdk.model.GetJobDocumentsResponse;

public class Sample19 extends Controller {
    public static String USER_INFO_FILE = "UserInfo_sample19.tmp";
    static String title = "GroupDocs Java SDK Samples";
    static String sample = "Sample19";
    static Form<Credentials> form = form(Credentials.class);

    public static Result index() {
        Form<Credentials> filledForm = form.bind(session());
        HashMap<String, String> data = new HashMap<String, String>();
        Http.Request request = request();

        if ("GET".equalsIgnoreCase(request.method())){
            session().put("server_type", "https://api.groupdocs.com/v2.0");
            return ok(views.html.sample19.render(title, sample, data, filledForm));
        }
        if ("POST".equalsIgnoreCase(request.method())){
            filledForm = form.bindFromRequest();
            Credentials credentials = filledForm.get();
            if (StringUtils.isNotEmpty(credentials.client_id) || StringUtils.isNotEmpty(credentials.private_key)){
                session().put("client_id", credentials.client_id);
                session().put("private_key", credentials.private_key);
                session().put("server_type", credentials.server_type);
            }
            Http.MultipartFormData multipartFormData = request.body().asMultipartFormData();
            Map<String, String[]> formUrlEncodedData = multipartFormData.asFormUrlEncoded();

            String callbackUrl = Utils.getFormValue(formUrlEncodedData, "callbackUrl");
            String sourse = Utils.getFormValue(formUrlEncodedData, "sourse");
            String target = Utils.getFormValue(formUrlEncodedData, "target");
            String sourseGuid = null;
            String targetGuid = null;
            String resultGuid = null;
            String compareKey = null;
            // For sourse
            if ("guid".equalsIgnoreCase(sourse)){
                sourseGuid = Utils.getFormValue(formUrlEncodedData, "fileId");
            }
            else if ("url".equalsIgnoreCase(sourse)) {
                try {
                    String url = Utils.getFormValue(formUrlEncodedData, "url");
                    sourseGuid = Utils.getGuidByUrl(credentials.client_id, credentials.private_key, credentials.server_type, url);
                }
                catch (Exception e) {
                    filledForm.reject(e.getMessage());
                    e.printStackTrace();
                    return ok(views.html.sample19.render(title, sample, data, filledForm));
                }
            }
            else if ("local".equalsIgnoreCase(sourse)) {
                try {
                    Http.MultipartFormData.FilePart local = multipartFormData.getFile("local");
                    sourseGuid = Utils.getGuidByFile(credentials.client_id, credentials.private_key, credentials.server_type, local.getFilename(), new FileStream(new FileInputStream(local.getFile())));
                }
                catch (Exception e) {
                    filledForm.reject(e.getMessage());
                    e.printStackTrace();
                    return ok(views.html.sample19.render(title, sample, data, filledForm));
                }
            }
            // For target
            if ("guid".equalsIgnoreCase(target)){
                targetGuid = Utils.getFormValue(formUrlEncodedData, "target_fileId");
            }
            else if ("target_url".equalsIgnoreCase(target)) {
                try {
                    String url = Utils.getFormValue(formUrlEncodedData, "target_url");
                    targetGuid = Utils.getGuidByUrl(credentials.client_id, credentials.private_key, credentials.server_type, url);
                }
                catch (Exception e) {
                    filledForm.reject(e.getMessage());
                    e.printStackTrace();
                    return ok(views.html.sample19.render(title, sample, data, filledForm));
                }
            }
            else if ("local".equalsIgnoreCase(target)) {
                try {
                    Http.MultipartFormData.FilePart local = multipartFormData.getFile("target_local");
                    targetGuid = Utils.getGuidByFile(credentials.client_id, credentials.private_key, credentials.server_type, local.getFilename(), new FileStream(new FileInputStream(local.getFile())));
                }
                catch (Exception e) {
                    filledForm.reject(e.getMessage());
                    e.printStackTrace();
                    return ok(views.html.sample19.render(title, sample, data, filledForm));
                }
            }

            if (StringUtils.isEmpty(sourseGuid) || StringUtils.isEmpty(targetGuid)) {
                filledForm.reject("GUID is empty or null!");
                return ok(views.html.sample19.render(title, sample, data, filledForm));
            }
            // Compare functional
            try {
                ApiInvoker.getInstance().setRequestSigner(new GroupDocsRequestSigner(credentials.private_key));
                ComparisonApi api = new ComparisonApi();
                api.setBasePath(credentials.server_type);
                if(callbackUrl == null){
                    callbackUrl = "";
                }
                CompareResponse compareResponse = api.Compare(credentials.client_id, sourseGuid, targetGuid, callbackUrl);
                compareResponse = Utils.assertResponse(compareResponse);
                Thread.sleep(5000);

                AsyncApi asyncApi = new AsyncApi();
                asyncApi.setBasePath(credentials.server_type);
                GetJobDocumentsResponse job_info = asyncApi.GetJobDocuments(credentials.client_id, compareResponse.getResult().getJob_id().toString(), "");
                Utils.assertResponse(job_info);
                resultGuid = job_info.getResult().getOutputs().get(0).getGuid();

                MgmtApi mgmtApi = new MgmtApi();
                mgmtApi.setBasePath(credentials.server_type);
                GetUserEmbedKeyResponse userEmbedKeyResponse = mgmtApi.GetUserEmbedKey(credentials.client_id, "comparison");
                Utils.assertResponse(userEmbedKeyResponse);
                compareKey = userEmbedKeyResponse.getResult().getKey().getGuid();

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
            }
            catch (Exception e){
                filledForm.reject(e.getMessage());
                e.printStackTrace();
                return ok(views.html.sample19.render(title, sample, data, filledForm));
            }
            data.put("guid", resultGuid);
            data.put("compareKey", compareKey);
            data.put("server", credentials.server_type.substring(0, credentials.server_type.indexOf(".com") + 4).replace("api", "apps"));
        }
        return ok(views.html.sample19.render(title, sample, data, filledForm));
    }
}