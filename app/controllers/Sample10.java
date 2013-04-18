//###<i>This sample will show how to use <b>ShareDocument</b> tmethod from Doc Api to share a document to other users</i>
package controllers;
//Import of necessary libraries
import java.util.List;
import java.util.Map;

import models.Credentials;

import org.apache.commons.lang3.StringUtils;

import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import scala.actors.threadpool.Arrays;

import com.groupdocs.sdk.api.DocApi;
import com.groupdocs.sdk.common.ApiException;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.GetDocumentInfoResponse;
import com.groupdocs.sdk.model.SharedUsersResponse;
import com.groupdocs.sdk.model.SharedUsersResult;

public class Sample10 extends Controller {
	//###Set variables
	static String title = "GroupDocs Java SDK Samples";
	static Form<Credentials> form = form(Credentials.class);
	
	public static Result index() {
		
		SharedUsersResult result = null;
		Form<Credentials> filledForm;
		String sample = "Sample10";
		Status status;
		//Check POST parameters
		if(request().method().equalsIgnoreCase("POST")){
			filledForm = form.bindFromRequest();
			if(filledForm.hasErrors()){
				//If filledForm have errors return to template
				status = badRequest(views.html.sample10.render(title, sample, result, filledForm));
			} else {
				//If filledForm have no errors get all parameters
				Credentials credentials = filledForm.get();
				session().put("client_id", credentials.client_id);
				session().put("private_key", credentials.private_key);
				session().put("baseurl", credentials.baseurl);
				
				Map<String, String[]> formData = request().body().asFormUrlEncoded();
				String fileId = formData.get("fileId") != null ? formData.get("fileId")[0] : null;
				fileId = StringUtils.isBlank(fileId) ? null : fileId.trim();
				String email = formData.get("email") != null ? formData.get("email")[0] : null;
				email = StringUtils.isBlank(email) ? null : email.trim();
				
				try {
					//### Check fileId amd email
					if(fileId == null || email == null){
						throw new Exception();
					}
					//###Create ApiInvoker, DocApi objects

		            //Create ApiInvoker object
					ApiInvoker.getInstance().setRequestSigner(
							new GroupDocsRequestSigner(credentials.private_key));
					//Create DocApi obect
					DocApi api = new DocApi();
					api.setBasePath(credentials.baseurl);
					//###Make a request to DocApi to get document metadeta for entered fileId
					GetDocumentInfoResponse metadata = new DocApi().GetDocumentMetadata(credentials.client_id, fileId);
					String file_Id = null;
					//Check request result
					if(metadata != null && metadata.getStatus().trim().equalsIgnoreCase("Ok")){
						file_Id = metadata.getResult().getId().toString();
					} else {
						throw new Exception("Not Found");
					}
					//###Make a request to DocApi to share document
					SharedUsersResponse response = api.ShareDocument(credentials.client_id, file_Id, Arrays.asList(new String[]{email}));
					//Check request result
					if(response != null && metadata.getStatus().trim().equalsIgnoreCase("Ok")){
						result = response.getResult();
					} else {
						throw new Exception("User identified by " + " not Found");
					}
					//If request was successfull - set result variable for template
					status = ok(views.html.sample10.render(title, sample, result, filledForm));
				//###Definition of Api errors and conclusion of the corresponding message
				} catch (ApiException e) {
					if(e.getCode() == 401){
						List<Object> args = Arrays.asList(new Object[]{"https://apps.groupdocs.com/My/Manage", "Production Server"});
						filledForm.reject("Wrong Credentials. Please make sure to use credentials from", args);
					} else {
						filledForm.reject("Failed to access API: " + e.getMessage());
					}
					status = badRequest(views.html.sample10.render(title, sample, result, filledForm));
				//###Definition of filledForm errors and conclusion of the corresponding message
				} catch (Exception e) {
					e.printStackTrace();
					if(fileId == null){
						filledForm.reject("fileId", "This field is required");
					} else if(email == null){
						filledForm.reject("email", "This field is required");
					} else {
						filledForm.reject("fileId", "Something wrong with your file: " + e.getMessage());
					}
					status = badRequest(views.html.sample10.render(title, sample, result, filledForm));
				} 
			}
		} else {
			filledForm = form.bind(session());
			session().put("baseurl", "https://api.groupdocs.com/v2.0");
			status = ok(views.html.sample10.render(title, sample, result, filledForm));
		}
		//Process template
		return status;
	}
	
}