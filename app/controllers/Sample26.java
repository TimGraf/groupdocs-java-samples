//###<i>This sample will show how to use <b>Login</b> to be authorized at GroupDocs and how to get GroupDocs user infromation using PHP SDK</i>

package controllers;
//Import of necessary libraries
import java.util.List;
import java.util.Map;

import models.Credentials;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import scala.actors.threadpool.Arrays;

import com.groupdocs.sdk.api.SharedApi;
import com.groupdocs.sdk.common.ApiException;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.UserInfo;
import com.groupdocs.sdk.model.UserInfoResponse;

public class Sample26 extends Controller {
	//###Set variables
	static String title = "GroupDocs Java SDK Samples";
	static Form<Credentials> form = form(Credentials.class);
	
	public static Result index() {
		
		UserInfo userInfo = null;
		Form<Credentials> filledForm;
		String sample = "Sample26";
		Status status;
		//Check POST parameters
		if(request().method().equalsIgnoreCase("POST")){
			filledForm = form.bindFromRequest();
			//If filledForm have errors return to template
			//Get POST data
			
			Map<String, String[]> formData = request().body().asFormUrlEncoded();
			String login = formData.get("login") != null ? formData.get("login")[0] : null;
			String password = formData.get("password") != null ? formData.get("password")[0] : null;
			String basePath = formData.get("server_type") != null ? formData.get("server_type")[0] : null;
			if(login.equalsIgnoreCase("") || password.equalsIgnoreCase("")){
				status = badRequest(views.html.sample26.render(title, sample, userInfo, filledForm));
			} else {
				
				//###Create ApiInvoker and Management Api objects
	            
	            //Create ApiInvoker object
				ApiInvoker.getInstance().setRequestSigner(
						new GroupDocsRequestSigner("123"));
				//Create SharedApi object
				SharedApi api = new SharedApi();
				//Set base path
				if (basePath.equals("")) {
					basePath = "https://api.groupdocs.com/v2.0";
				}
				api.setBasePath(basePath);
				
				//###Make a request to SharedAPI using login and password
				try {
					//Login to the GroupDocs
					UserInfoResponse response = api.LoginUser(login, password);
					//Check the result of the request
					if(response != null && response.getStatus().trim().equalsIgnoreCase("Ok")){
						userInfo = response.getResult().getUser();
					}
					//If request was successfull - set userInfo variable for template
					status = ok(views.html.sample26.render(title, sample, userInfo, filledForm));
				//###Definition of Api errors and conclusion of the corresponding message
				} catch (ApiException e) {
					if(e.getCode() == 401){
						List<Object> args = Arrays.asList(new Object[]{"https://apps.groupdocs.com/My/Manage", "Production Server"});
						filledForm.reject("Wrong Credentials. Please make sure to use credentials from", args);
					} else {
						filledForm.reject("Failed to access API: " + e.getMessage());
					}
					status = badRequest(views.html.sample26.render(title, sample, userInfo, filledForm));
				}
			}
		} else {
			//Process template
			filledForm = form.bind(session());
			session().put("server_type", "https://api.groupdocs.com/v2.0");
			status = ok(views.html.sample26.render(title, sample, userInfo, filledForm));
		}
		return status;
	}
	
}