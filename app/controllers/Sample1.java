package controllers;

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

public class Sample1 extends Controller {

	static String title = "GroupDocs Java SDK Samples";
	static Form<Credentials> form = form(Credentials.class);
	
	public static Result index() {
		UserInfo userInfo = null;
		Form<Credentials> filledForm;
		String sample = "Sample1";
		Status status;
		if(request().method().equalsIgnoreCase("POST")){
			filledForm = form.bindFromRequest();
			if(filledForm.hasErrors()){
				status = badRequest(views.html.sample1.render(title, sample, userInfo, filledForm));
			} else {
				Credentials credentials = filledForm.get();
				session().put("clientId", credentials.clientId);
				session().put("privateKey", credentials.privateKey);
				ApiInvoker.getInstance().setRequestSigner(
						new GroupDocsRequestSigner(credentials.privateKey));
				MgmtApi api = new MgmtApi();
				try {
					UserInfoResponse response = api.GetUserProfile(credentials.clientId);
					if(response != null && response.getStatus().trim().equalsIgnoreCase("Ok")){
						userInfo = response.getResult().getUser();
					}
					status = ok(views.html.sample1.render(title, sample, userInfo, filledForm));
				} catch (ApiException e) {
					if(e.getCode() == 401){
						List<Object> args = Arrays.asList(new Object[]{"https://apps.groupdocs.com/My/Manage", "Production Server"});
						filledForm.reject("Wrong Credentials. Please make sure to use credentials from", args);
					} else {
						filledForm.reject("Failed to access API: " + e.getMessage());
					}
					status = badRequest(views.html.sample1.render(title, sample, userInfo, filledForm));
				}
			}
		} else {
			filledForm = form.bind(session());
			status = ok(views.html.sample1.render(title, sample, userInfo, filledForm));
		}
		return status;
	}
	
	
}