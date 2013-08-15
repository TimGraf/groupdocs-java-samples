//###<i>This sample will show how to use <b>GetDocumentViews</b> method from Storage Api to get number of viewings for this user </i>
package controllers;
//Import of necessary libraries

import java.util.List;

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
import com.groupdocs.sdk.model.DocumentViewsResponse;
import com.groupdocs.sdk.model.DocumentViewInfo;

public class Sample15 extends Controller {
	//###Set variables
	static String title = "GroupDocs Java SDK Samples";
	static Form<Credentials> form = form(Credentials.class);
	
	public static Result index() {
		List<DocumentViewInfo> result = null;
		Form<Credentials> filledForm;
		String sample = "Sample15";
		Status status;
		//Check POST parameters
		if(request().method().equalsIgnoreCase("POST")){
			filledForm = form.bindFromRequest();
			if(filledForm.hasErrors()){
				status = badRequest(views.html.sample15.render(title, sample, result, filledForm));
			} else {
				//Get POST data
				Credentials credentials = filledForm.get();
				session().put("client_id", credentials.getClient_id());
				session().put("private_key", credentials.getPrivate_key());
				session().put("server_type", credentials.getServer_type());
								
				try {
					//Check client id and private key 
					if(credentials.getClient_id() == null || credentials.getPrivate_key() == null){
						throw new Exception();
					}
					//###Create ApiInvoker, Doc Api objects
					
					//Create ApiInvoker object
					ApiInvoker.getInstance().setRequestSigner(
							new GroupDocsRequestSigner(credentials.getPrivate_key()));
					//Create Doc Api object and get document metadata
					DocApi metadata = new DocApi();
					metadata.setBasePath(credentials.getServer_type());
					
					//###Make request to Doc api to get documents views
					DocumentViewsResponse viewes = metadata.GetDocumentViews(credentials.getClient_id(), null, null);
					//Check request status
					if(viewes != null && viewes.getStatus().trim().equalsIgnoreCase("Ok")){
						//If requst status is Ok get views
						result = viewes.getResult().getViews();
					}
					//If request was successful - set moveResult variable for template
					status = ok(views.html.sample15.render(title, sample, result, filledForm));
				//###Definition of Api errors and conclusion of the corresponding message
				} catch (ApiException e) {
					if(e.getCode() == 401){
						List<Object> args = Arrays.asList(new Object[]{"https://apps.groupdocs.com/My/Manage", "Production Server"});
						filledForm.reject("Wrong Credentials. Please make sure to use credentials from", args);
					} else {
						filledForm.reject("Failed to access API: " + e.getMessage());
					}
					status = badRequest(views.html.sample15.render(title, sample, result, filledForm));
			    //###Definition of filledForm errors and conclusion of the corresponding message	
				} catch (Exception e) {
					if(e.hashCode() == 401){
						List<Object> args = Arrays.asList(new Object[]{"https://apps.groupdocs.com/My/Manage", "Production Server"});
						filledForm.reject("Wrong Credentials. Please make sure to use credentials from", args);
					} else {
						filledForm.reject("Failed to access API: " + e.getMessage());
					}
					status = badRequest(views.html.sample15.render(title, sample, result, filledForm));
				}
			}
		} else {
			filledForm = form.bind(session());
			session().put("server_type", "https://api.groupdocs.com/v2.0");
			status = ok(views.html.sample15.render(title, sample, result, filledForm));
		}
		//Process template
		return status;
	}
	
}