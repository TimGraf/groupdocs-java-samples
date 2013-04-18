//###<i>This sample will show how to use <b>Upload and Compress</b> method's from Storage Api to upload and compress to zip file to GroupDocs Storage </i>
package controllers;
//Import of necessary libraries
import java.io.FileInputStream;
import java.util.List;

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
import com.groupdocs.sdk.model.CompressRequestResult;
import com.groupdocs.sdk.model.UploadResponse;
import com.groupdocs.sdk.model.CompressResponse;

public class Sample17 extends Controller {
	//###Set variables
	static String title = "GroupDocs Java SDK Samples";
	static Form<Credentials> form = form(Credentials.class);
	
	public static Result index() {
		String result = null;
		Form<Credentials> filledForm;
		String sample = "Sample17";
		Status status;
		String id = "";
		//Check POST parameters
		if(request().method().equalsIgnoreCase("POST")){
			filledForm = form.bindFromRequest();
			if(filledForm.hasErrors()){
				status = badRequest(views.html.sample17.render(title, sample, result, filledForm));
			} else {
				//Get POST data
				Credentials credentials = filledForm.get();
				session().put("client_id", credentials.client_id);
				session().put("private_key", credentials.private_key);
				session().put("baseurl", credentials.baseurl);
				
				MultipartFormData body = request().body().asMultipartFormData();
		        FilePart filePart = body.getFile("file");
		       
		        //###Create ApiInvoker, Storage Api and FileInputStream objects
				try {
					//Create ApiInvoker object
					ApiInvoker.getInstance().setRequestSigner(
							new GroupDocsRequestSigner(credentials.private_key));
					//Create Storage object
					StorageApi api = new StorageApi();
					api.setBasePath(credentials.baseurl);
					//Create FileInputStream object 
					FileInputStream is = new FileInputStream(filePart.getFile());
					//###Make a request to Storage API using clientId
					
					//Upload file to current user storage
					UploadResponse response = api.Upload(credentials.client_id, filePart.getFilename(), null, new FileStream(is));
					//Check request status
					if(response != null && response.getStatus().trim().equalsIgnoreCase("Ok")){
						//If status Ok get file Guid
						id = response.getResult().getId().toString();
					}
					//Compress uploaded file
					CompressResponse compress = api.Compress(credentials.client_id, id, "zip");
					//Check compress result
					if(compress != null && compress.getStatus().trim().equalsIgnoreCase("Ok")){
						//If status Ok get results
						result = filePart.getFilename().replaceAll("\\.[a-z]{3}", ".zip");
					
					} else {
						//If request failed throw exception massage   
						throw new Exception("It wasn't succeeded to compress uploaded file");
						
					}
					//If request was successful - set file variable for template
					status = ok(views.html.sample17.render(title, sample, result, filledForm));
			    //###Definition of Api errors and conclusion of the corresponding message
				} catch (ApiException e) {
					if(e.getCode() == 401){
						List<Object> args = Arrays.asList(new Object[]{"https://apps.groupdocs.com/My/Manage", "Production Server"});
						filledForm.reject("Wrong Credentials. Please make sure to use credentials from", args);
					} else {
						filledForm.reject("Failed to access API: " + e.getMessage());
					}
					status = badRequest(views.html.sample17.render(title, sample, result, filledForm));
				//###Definition of filledForm errors and conclusion of the corresponding message
				} catch (Exception e) {
					if(filePart == null){
						filledForm.reject("file", "This field is required");
					} else {
						filledForm.reject("file", "Something wrong with your file: " + e.getMessage());
					}
					status = badRequest(views.html.sample17.render(title, sample, result, filledForm));
				}
			}
		} else {
			filledForm = form.bind(session());
			session().put("baseurl", "https://api.groupdocs.com/v2.0");
			status = ok(views.html.sample17.render(title, sample, result, filledForm));
		}
		//Process template
		return status;
	}
	
}