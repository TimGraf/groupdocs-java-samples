//###<i>This sample will show how to use <b>GuId</b> of file to generate an embedded Viewer URL for a Document</i>
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
import scala.actors.threadpool.Arrays;

import com.groupdocs.sdk.api.DocApi;
import com.groupdocs.sdk.common.ApiException;
import com.groupdocs.sdk.common.ApiInvoker;
//import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.GetDocumentInfoResponse;
import com.groupdocs.sdk.model.GetDocumentInfoResult;

public class Sample09 extends Controller {
	//###Set variables
	static String title = "GroupDocs Java SDK Samples";
	static Form<Credentials> form = form(Credentials.class);
	
	public static Result index() {
		HashMap<String, String> data = null;
		Form<Credentials> filledForm;
		String sample = "Sample9";
		Status status;
		//Check POST parameters
		if(request().method().equalsIgnoreCase("POST")){
			filledForm = form.bindFromRequest();
            String fileId = null;
            String width = null;
            String height = null;

            try {
            /////////////////////////////////////// -- //////////////////////////////////////
            Http.MultipartFormData formData = request().body().asMultipartFormData();
            Map<String, String[]> fieldsData = formData.asFormUrlEncoded();

            String sourse = Utils.getFormValue(fieldsData, "sourse");
            if ("guid".equals(sourse)) { // File GUID
                fileId = Utils.getFormValue(fieldsData, "fileId");
            }
            else {
                //Get POST data
                Credentials credentials = filledForm.get();
                session().put("client_id", credentials.getClient_id());
                session().put("private_key", credentials.getPrivate_key());
                session().put("server_type", credentials.getServer_type());
                if (StringUtils.isEmpty(credentials.getClient_id()) || StringUtils.isEmpty(credentials.getPrivate_key()) || StringUtils.isEmpty(credentials.getServer_type())) {
                    throw  new Exception();
                }
                if ("url".equals(sourse)) { // Upload file fron URL
                    String url = Utils.getFormValue(fieldsData, "url");
                    ApiInvoker.getInstance().setRequestSigner(
                            new GroupDocsRequestSigner(credentials.getPrivate_key()));
                    StorageApi storageApi = new StorageApi();
                    storageApi.setBasePath(credentials.getServer_type());
                    UploadResponse response = storageApi.UploadWeb(credentials.getClient_id(), url);
                    if(response != null && response.getStatus().trim().equalsIgnoreCase("Ok")){
                        fileId = response.getResult().getGuid();
                    }
                }
                else if ("local".equals(sourse)) { // Upload local file
                    Http.MultipartFormData.FilePart file = formData.getFile("file");
                    ApiInvoker.getInstance().setRequestSigner(
                            new GroupDocsRequestSigner(credentials.getPrivate_key()));
                    StorageApi storageApi = new StorageApi();
                    storageApi.setBasePath(credentials.getServer_type());
                    FileInputStream is = new FileInputStream(file.getFile());
                    UploadResponse response = storageApi.Upload(credentials.getClient_id(), file.getFilename(), "uploaded", "", new FileStream(is));
                    if(response != null && response.getStatus().trim().equalsIgnoreCase("Ok")){
                        fileId = response.getResult().getGuid();
                    }
                }
            }
            /////////////////////////////////////// -- //////////////////////////////////////
            status = badRequest(views.html.sample09.render(title, sample, data, filledForm));
            //###Definition of filledForm errors and conclusion of the corresponding message
            // Sample:

                width = Utils.getFormValue(fieldsData, "width");
                height = Utils.getFormValue(fieldsData, "height");
                //CHeck fileId, width and height
                if(fileId != null || width != null || height != null){
                    //Put entered parameters to data HashMap
                    data = new HashMap<String, String>();
                    data.put("guid", fileId);
                    data.put("width", width);
                    data.put("height", height);

                } else {
                    throw new Exception("Not Found");
                    }
                //If request was successfull - set  data variable for template
                status = ok(views.html.sample09.render(title, sample, data, filledForm));
            //###Definition of filledForm errors and conclusion of the corresponding message
            } catch (Exception e) {
                e.printStackTrace();
                if(fileId == null){
                    filledForm.reject("fileId", "This field is required");
                } else if(width == null){
                    filledForm.reject("width", "This field is required");
                } else if(height == null){
                    filledForm.reject("height", "This field is required");
                } else {
                    filledForm.reject("fileId", "Something wrong with your file: " + e.getMessage());
                }
                status = badRequest(views.html.sample09.render(title, sample, data, filledForm));
            }
		} else {
			filledForm = form.bind(session());
			status = ok(views.html.sample09.render(title, sample, data, filledForm));
		}
		//Process template
		return status;
	}
	
}