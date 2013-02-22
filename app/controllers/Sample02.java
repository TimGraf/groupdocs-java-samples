//###This sample will show how to use <b>ListEntities</b> method from Storage  API  to list files within GroupDocs Storage
package controllers;
//Import of necessary libraries
import java.util.List;

import models.Credentials;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import scala.actors.threadpool.Arrays;

import com.groupdocs.sdk.api.StorageApi;
import com.groupdocs.sdk.common.ApiException;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.FileSystemDocument;
import com.groupdocs.sdk.model.ListEntitiesResponse;

public class Sample02 extends Controller {
	//###Set variables
	static String title = "GroupDocs Java SDK Samples";
	static Form<Credentials> form = form(Credentials.class);
	
	public static Result index() {
		
		List<FileSystemDocument> files = null;
		Form<Credentials> filledForm;
		String sample = "Sample2";
		Status status;
		//Check POST parameters
		if(request().method().equalsIgnoreCase("POST")){
			filledForm = form.bindFromRequest();
			if(filledForm.hasErrors()){
				status = badRequest(views.html.sample02.render(title, sample, files, filledForm));
			} else {
				//Get POST data
				Credentials credentials = filledForm.get();
				session().put("client_id", credentials.client_id);
				session().put("private_key", credentials.private_key);
				
				//###Create ApiInvoker, Storage Api objects
				try {
					 //Create ApiInvoker object
					ApiInvoker.getInstance().setRequestSigner(
							new GroupDocsRequestSigner(credentials.private_key));
					 //Create Storage api object
					StorageApi api = new StorageApi();
					//###Make a request to Storage API using clientId
		            
		            //Obtaining all Entities from current user
					ListEntitiesResponse response = api.ListEntities(credentials.client_id, "", null, null, null, null, null, null, null);
					//Check request result
					if(response != null && response.getStatus().trim().equalsIgnoreCase("Ok")){
						files = response.getResult().getFiles();
					}
					//If request was successfull - set files variable for template
					status = ok(views.html.sample02.render(title, sample, files, filledForm));
				//###Definition of Api errors and conclusion of the corresponding message
				} catch (ApiException e) {
					if(e.getCode() == 401){
						List<Object> args = Arrays.asList(new Object[]{"https://apps.groupdocs.com/My/Manage", "Production Server"});
						filledForm.reject("Wrong Credentials. Please make sure to use credentials from", args);
					} else {
						filledForm.reject("Failed to access API: " + e.getMessage());
					}
					status = badRequest(views.html.sample02.render(title, sample, files, filledForm));
				}
			}
		} else {
			filledForm = form.bind(session());
			status = ok(views.html.sample02.render(title, sample, files, filledForm));
		}
		//Process template
		return status;
	}

}