//###<i>This sample will show how to use <b>Signer object</b> to be authorized at GroupDocs and how to get GroupDocs user infromation using PHP SDK</i>

package controllers;
//Import of necessary libraries
import java.util.List;

import models.Credentials;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import scala.actors.threadpool.Arrays;

import com.groupdocs.sdk.api.MgmtApi;
import com.groupdocs.sdk.common.ApiException;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.UserInfo;
import com.groupdocs.sdk.model.UserInfoResponse;

public class Sample01 extends Controller {
	//###Set variables
	static String title = "GroupDocs Java SDK Samples";
	static Form<Credentials> form = form(Credentials.class);
	
	public static Result index() {
		
		UserInfo userInfo = null;
		Form<Credentials> filledForm;
		String sample = "Sample01";
		Status status;
		//Check POST parameters
		if(request().method().equalsIgnoreCase("POST")){
			filledForm = form.bindFromRequest();
			//If filledForm have errors return to template
			if(filledForm.hasErrors()){
				status = badRequest(views.html.sample01.render(title, sample, userInfo, filledForm));
			} else {
				//If filledForm have no errors get all parameters
				Credentials credentials = filledForm.get();
				session().put("client_id", credentials.client_id);
				session().put("private_key", credentials.private_key);
				session().put("server_type", credentials.server_type);
				//###Create ApiInvoker and Management Api objects
	            
	            //Create ApiInvoker object
				ApiInvoker.getInstance().setRequestSigner(
						new GroupDocsRequestSigner(credentials.private_key));
				//Create Management Api object
				MgmtApi api = new MgmtApi();
				api.setBasePath(credentials.server_type);
				
				//###Make a request to Management API using clientId
				try {
					UserInfoResponse response = api.GetUserProfile(credentials.client_id);
					//Check the result of the request
					if(response != null && response.getStatus().trim().equalsIgnoreCase("Ok")){
						userInfo = response.getResult().getUser();
					}
					//If request was successfull - set userInfo variable for template
					status = ok(views.html.sample01.render(title, sample, userInfo, filledForm));
				//###Definition of Api errors and conclusion of the corresponding message
				} catch (ApiException e) {
					if(e.getCode() == 401){
						List<Object> args = Arrays.asList(new Object[]{"https://apps.groupdocs.com/My/Manage", "Production Server"});
						filledForm.reject("Wrong Credentials. Please make sure to use credentials from", args);
					} else {
						filledForm.reject("Failed to access API: " + e.getMessage());
					}
					status = badRequest(views.html.sample01.render(title, sample, userInfo, filledForm));
				}
			}
		} else {
			//Process template
			filledForm = form.bind(session());
			session().put("server_type", "https://api.groupdocs.com/v2.0");
			status = ok(views.html.sample01.render(title, sample, userInfo, filledForm));
		}
		return status;
	}
	
}