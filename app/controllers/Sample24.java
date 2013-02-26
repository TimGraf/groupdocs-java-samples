//###<i>This sample will show how to use <b>Upload</b> method from Storage Api to upload file to GroupDocs Storage </i>
package controllers;
//Import of necessary libraries
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import models.Credentials;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import scala.actors.threadpool.Arrays;

import com.groupdocs.sdk.api.StorageApi;
import com.groupdocs.sdk.common.ApiException;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.FileStream;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.UploadRequestResult;
import com.groupdocs.sdk.model.UploadResponse;

public class Sample24 extends Controller {
	//###Set variables
	static String title = "GroupDocs Java SDK Samples";
	static Form<Credentials> form = form(Credentials.class);
	
	public static Result index() {
		UploadRequestResult file = null;
		Form<Credentials> filledForm;
		String sample = "Sample24";
		Status status;
		//Check POST parameters
		if(request().method().equalsIgnoreCase("POST")){
			filledForm = form.bindFromRequest();
			if(filledForm.hasErrors()){
				status = badRequest(views.html.sample24.render(title, sample, file, filledForm));
			} else {
				//Get POST data
				Credentials credentials = filledForm.get();
				session().put("client_id", credentials.client_id);
				session().put("private_key", credentials.private_key);
				
				Map<String, String[]> formData = request().body().asFormUrlEncoded();
				String fileurl = formData.get("url") != null ? formData.get("url")[0] : null;
				fileurl = StringUtils.isBlank(fileurl) ? null : fileurl.trim();
		       
		        //###Create ApiInvoker, Storage Api and FileInputStream objects
				try {
					//Create ApiInvoker object
					ApiInvoker.getInstance().setRequestSigner(
							new GroupDocsRequestSigner(credentials.private_key));
					//Create Storage object
					StorageApi api = new StorageApi();
					//###Make a request to Storage API using clientId
					
					//Upload file to current user storage
					UploadResponse response = api.UploadWeb(credentials.client_id, fileurl);
					
					//Check request result
					if(response != null && response.getStatus().trim().equalsIgnoreCase("Ok")){
						file = response.getResult();
					}
					//If request was successfull - set file variable for template
					status = ok(views.html.sample24.render(title, sample, file, filledForm));
			    //###Definition of Api errors and conclusion of the corresponding message
				} catch (ApiException e) {
					if(e.getCode() == 401){
						List<Object> args = Arrays.asList(new Object[]{"https://apps.groupdocs.com/My/Manage", "Production Server"});
						filledForm.reject("Wrong Credentials. Please make sure to use credentials from", args);
					} else {
						filledForm.reject("Failed to access API: " + e.getMessage());
					}
					status = badRequest(views.html.sample24.render(title, sample, file, filledForm));
				//###Definition of filledForm errors and conclusion of the corresponding message
				} catch (Exception e) {
					if(fileurl == null){
						filledForm.reject("file url", "This field is required");
					} else {
						filledForm.reject("file", "Something wrong with your file: " + e.getMessage());
					}
					status = badRequest(views.html.sample24.render(title, sample, file, filledForm));
				}
			}
		} else {
			filledForm = form.bind(session());
			status = ok(views.html.sample24.render(title, sample, file, filledForm));
		}
		//Process template
		return status;
	}
	
}