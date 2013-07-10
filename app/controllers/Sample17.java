//###<i>This sample will show how to use <b>Upload and Compress</b> method's from Storage Api to upload and compress to zip file to GroupDocs Storage </i>
package controllers;
//Import of necessary libraries
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.Utils;
import models.Credentials;
import org.apache.commons.lang3.StringUtils;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import scala.actors.threadpool.Arrays;

import com.groupdocs.sdk.api.StorageApi;
import com.groupdocs.sdk.common.ApiException;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.FileStream;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.CompressRequestResult;
import com.groupdocs.sdk.model.UploadResponse;
import com.groupdocs.sdk.model.CompressResponse;

public class Sample17 extends Controller {
    //###Set variables
    static String title = "GroupDocs Java SDK Samples";
    static String sample = "Sample17";
    static Form<Credentials> form = form(Credentials.class);

    public static Result index() {
        Form<Credentials> filledForm = form.bind(session());
        HashMap<String, String> data = new HashMap<String, String>();
        Http.Request request = request();

        if ("GET".equalsIgnoreCase(request.method())){
            session().put("server_type", "https://api.groupdocs.com/v2.0");
            return ok(views.html.sample17.render(title, sample, data, filledForm));
        }
        if ("POST".equalsIgnoreCase(request.method())){
            Credentials credentials = filledForm.get();
            if (StringUtils.isNotEmpty(credentials.client_id) || StringUtils.isNotEmpty(credentials.private_key)){
                session().put("client_id", credentials.client_id);
                session().put("private_key", credentials.private_key);
                session().put("server_type", credentials.server_type);
            }
            Http.MultipartFormData multipartFormData = request.body().asMultipartFormData();
            Map<String, String[]> formUrlEncodedData = multipartFormData.asFormUrlEncoded();

            String sourse = Utils.getFormValue(formUrlEncodedData, "sourse");
            String guid = null;
            if ("guid".equalsIgnoreCase(sourse)){
                guid = Utils.getFormValue(formUrlEncodedData, "fileId");
            }
            else if ("url".equalsIgnoreCase(sourse)) {
                try {
                    String url = Utils.getFormValue(formUrlEncodedData, "url");
                    guid = Utils.getGuidByUrl(credentials.client_id, credentials.private_key, credentials.server_type, url);
                }
                catch (Exception e) {
                    filledForm.reject(e.getMessage());
                    e.printStackTrace();
                    return ok(views.html.sample17.render(title, sample, data, filledForm));
                }
            }
            else if ("local".equalsIgnoreCase(sourse)) {
                try {
                    Http.MultipartFormData.FilePart local = multipartFormData.getFile("local");
                    guid = Utils.getGuidByFile(credentials.client_id, credentials.private_key, credentials.server_type, local.getFilename(), new FileStream(new FileInputStream(local.getFile())));
                }
                catch (Exception e) {
                    filledForm.reject(e.getMessage());
                    e.printStackTrace();
                    return ok(views.html.sample17.render(title, sample, data, filledForm));
                }
            }
            if (StringUtils.isEmpty(guid)) {
                filledForm.reject("GUID is empty or null!");
                return ok(views.html.sample17.render(title, sample, data, filledForm));
            }

            try {
                Double fileID = Utils.getFileIdByGuid(credentials.client_id, credentials.private_key, credentials.server_type, guid);

                ApiInvoker.getInstance().setRequestSigner(new GroupDocsRequestSigner(credentials.private_key));
                StorageApi api = new StorageApi();
                api.setBasePath(credentials.server_type);
                CompressResponse compressResponse = api.Compress(credentials.client_id, Double.toString(fileID), "zip");

                if (compressResponse != null && "Ok".equalsIgnoreCase(compressResponse.getStatus())){
                    String fileName = Utils.getFileNameByGuid(credentials.client_id, credentials.private_key, credentials.server_type, guid);
                    data.put("gdFile", fileName.replaceAll("\\.[a-z]{3}", ".zip"));
                }
                else {
                    throw new Exception(compressResponse.getError_message());
                }
            }
            catch (Exception e) {
                filledForm.reject(e.getMessage());
                return ok(views.html.sample17.render(title, sample, data, filledForm));
            }
        }
        return ok(views.html.sample17.render(title, sample, data, filledForm));
    }
}