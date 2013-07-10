//###<i>This sample will show how to use <b>GuId</b> of file to insert Assembly questionary into webpage</i>
package controllers;
//Import of necessary libraries
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.groupdocs.sdk.api.StorageApi;
import com.groupdocs.sdk.common.FileStream;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.UploadResponse;
import common.Utils;
import models.Credentials;

import org.apache.commons.lang3.StringUtils;

import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Http.Request;
import scala.actors.threadpool.Arrays;

import com.groupdocs.sdk.api.DocApi;
import com.groupdocs.sdk.common.ApiException;
import com.groupdocs.sdk.common.ApiInvoker;
//import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.GetDocumentInfoResponse;
import com.groupdocs.sdk.model.GetDocumentInfoResult;

public class Sample16 extends Controller {
	//###Set variables
	static String title = "GroupDocs Java SDK Samples";
    static String sample = "Sample16";
	static Form<Credentials> form = form(Credentials.class);
	
	public static Result index() {
		Form<Credentials> filledForm = form.bind(session());
        HashMap<String, String> data = new HashMap<String, String>();
        Request request = request();

        if ("GET".equalsIgnoreCase(request.method())){
            session().put("server_type", "https://api.groupdocs.com/v2.0");
            return ok(views.html.sample16.render(title, sample, data, filledForm));
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
                    return ok(views.html.sample16.render(title, sample, data, filledForm));
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
                    return ok(views.html.sample16.render(title, sample, data, filledForm));
                }
            }
            if (StringUtils.isEmpty(guid)) {
                filledForm.reject("GUID is empty or null!");
                return ok(views.html.sample16.render(title, sample, data, filledForm));
            }
            data.put("guid", guid);
        }
		return ok(views.html.sample16.render(title, sample, data, filledForm));
	}
}