//###<i>This sample will show how to use <b>MoveFile</b> method from Storage Api to copy/move a file in GroupDocs Storage </i>
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
import com.groupdocs.sdk.api.StorageApi;
import com.groupdocs.sdk.common.ApiException;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.FileMoveResponse;
import com.groupdocs.sdk.model.FileMoveResult;
import com.groupdocs.sdk.model.GetDocumentInfoResponse;

public class Sample05 extends Controller {
	//###Set variables
	static String title = "GroupDocs Java SDK Samples";
	static Form<Credentials> form = form(Credentials.class);
	
	public static Result index() {
		FileMoveResult moveResult = null;
		Form<Credentials> filledForm;
		String sample = "Sample5";
		Status status;
		//Check POST parameters
		if(request().method().equalsIgnoreCase("POST")){
			filledForm = form.bindFromRequest();
			if(filledForm.hasErrors()){
				status = badRequest(views.html.sample05.render(title, sample, moveResult, filledForm));
			} else {
				//Get POST data
				Credentials credentials = filledForm.get();
				session().put("client_id", credentials.client_id);
				session().put("private_key", credentials.private_key);
				session().put("server_type", credentials.server_type);
				
				Map<String, String[]> formData = request().body().asFormUrlEncoded();
				String srcPath = formData.get("srcPath") != null ? formData.get("srcPath")[0] : null;
				srcPath = StringUtils.isBlank(srcPath) ? null : srcPath.trim();
				String destPath = formData.get("destPath") != null ? formData.get("destPath")[0] : null;
				destPath = StringUtils.isBlank(destPath) ? null : destPath.trim();
				boolean isCopy;
				//Check what button was pressed copy or move
				if(formData.containsKey("move")){
					isCopy = false;
				} else {
					isCopy = true;
				}
		        
				try {
					//Check source and destination path 
					if(srcPath == null || destPath == null){
						throw new Exception();
					}
					//###Create ApiInvoker, Doc and Storage Api objects
					
					//Create ApiInvoker object
					ApiInvoker.getInstance().setRequestSigner(
							new GroupDocsRequestSigner(credentials.private_key));
					//Create Doc Api object and get document metadata
					GetDocumentInfoResponse metadata = new DocApi().GetDocumentMetadataByPath(credentials.client_id, srcPath);
					Long fileId = null;
					//Check request result
					if(metadata != null && metadata.getStatus().trim().equalsIgnoreCase("Ok")){
						fileId = metadata.getResult().getId().longValue();
					} else {
						throw new Exception("Not Found");
					}
					//Create Storage object
					StorageApi api = new StorageApi();
					api.setBasePath(credentials.server_type);
					FileMoveResponse response;
					//###Make a request to Storage API using clientId
					if(isCopy){
						response = api.MoveFile(credentials.client_id, destPath, null, fileId.toString(), null);
					} else {
						response = api.MoveFile(credentials.client_id, destPath, null, null, fileId.toString());
					}
					//Check request result
					if(response != null && response.getStatus().trim().equalsIgnoreCase("Ok")){
						moveResult = response.getResult();
					}
					//If request was successfull - set moveResult variable for template
					status = ok(views.html.sample05.render(title, sample, moveResult, filledForm));
				//###Definition of Api errors and conclusion of the corresponding message
				} catch (ApiException e) {
					if(e.getCode() == 401){
						List<Object> args = Arrays.asList(new Object[]{"https://apps.groupdocs.com/My/Manage", "Production Server"});
						filledForm.reject("Wrong Credentials. Please make sure to use credentials from", args);
					} else {
						filledForm.reject("Failed to access API: " + e.getMessage());
					}
					status = badRequest(views.html.sample05.render(title, sample, moveResult, filledForm));
			    //###Definition of filledForm errors and conclusion of the corresponding message	
				} catch (Exception e) {
					if(srcPath == null){
						filledForm.reject("srcPath", "This field is required");
					} else if(destPath == null){
						filledForm.reject("destPath", "This field is required");
					} else {
						filledForm.reject("srcPath", "Something wrong with your file: " + e.getMessage());
					}
					status = badRequest(views.html.sample05.render(title, sample, moveResult, filledForm));
				}
			}
		} else {
			filledForm = form.bind(session());
			session().put("server_type", "https://api.groupdocs.com/v2.0");
			status = ok(views.html.sample05.render(title, sample, moveResult, filledForm));
		}
		//Process template
		return status;
	}
	
}