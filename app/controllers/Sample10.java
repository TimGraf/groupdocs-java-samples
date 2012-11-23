package controllers;

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

	static String title = "GroupDocs Java SDK Samples";
	static Form<Credentials> form = form(Credentials.class);
	
	public static Result index() {
		SharedUsersResult result = null;
		Form<Credentials> filledForm;
		String sample = "Sample10";
		Status status;
		if(request().method().equalsIgnoreCase("POST")){
			filledForm = form.bindFromRequest();
			if(filledForm.hasErrors()){
				status = badRequest(views.html.sample10.render(title, sample, result, filledForm));
			} else {
				Credentials credentials = filledForm.get();
				session().put("clientId", credentials.clientId);
				session().put("privateKey", credentials.privateKey);
				
				Map<String, String[]> formData = request().body().asFormUrlEncoded();
				String fileGuid = formData.get("fileGuid") != null ? formData.get("fileGuid")[0] : null;
				fileGuid = StringUtils.isBlank(fileGuid) ? null : fileGuid.trim();
				String email = formData.get("email") != null ? formData.get("email")[0] : null;
				email = StringUtils.isBlank(email) ? null : email.trim();
				
				try {
					if(fileGuid == null || email == null){
						throw new Exception();
					}
					ApiInvoker.getInstance().setRequestSigner(
							new GroupDocsRequestSigner(credentials.privateKey));
					
					DocApi api = new DocApi();
					GetDocumentInfoResponse metadata = new DocApi().GetDocumentMetadata(credentials.clientId, fileGuid);
					String fileId = null;
					if(metadata != null && metadata.getStatus().trim().equalsIgnoreCase("Ok")){
						fileId = metadata.getResult().getId().toString();
					} else {
						throw new Exception("Not Found");
					}
					
					SharedUsersResponse response = api.ShareDocument(credentials.clientId, fileId, Arrays.asList(new String[]{email}));
					if(response != null && metadata.getStatus().trim().equalsIgnoreCase("Ok")){
						result = response.getResult();
					} else {
						throw new Exception("User identified by " + " not Found");
					}
					status = ok(views.html.sample10.render(title, sample, result, filledForm));
				} catch (ApiException e) {
					if(e.getCode() == 401){
						List<Object> args = Arrays.asList(new Object[]{"https://apps.groupdocs.com/My/Manage", "Production Server"});
						filledForm.reject("Wrong Credentials. Please make sure to use credentials from", args);
					} else {
						filledForm.reject("Failed to access API: " + e.getMessage());
					}
					status = badRequest(views.html.sample10.render(title, sample, result, filledForm));
				} catch (Exception e) {
					e.printStackTrace();
					if(fileGuid == null){
						filledForm.reject("fileGuid", "This field is required");
					} else if(email == null){
						filledForm.reject("email", "This field is required");
					} else {
						filledForm.reject("fileGuid", "Something wrong with your file: " + e.getMessage());
					}
					status = badRequest(views.html.sample10.render(title, sample, result, filledForm));
				} 
			}
		} else {
			filledForm = form.bind(session());
			status = ok(views.html.sample10.render(title, sample, result, filledForm));
		}
		return status;
	}
	
}