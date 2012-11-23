package controllers;

import java.util.HashMap;
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
import com.groupdocs.sdk.model.GetDocumentInfoResult;

public class Sample9 extends Controller {

	static String title = "GroupDocs Java SDK Samples";
	static Form<Credentials> form = form(Credentials.class);
	
	public static Result index() {
		HashMap<String, String> data = null;
		Form<Credentials> filledForm;
		String sample = "Sample9";
		Status status;
		if(request().method().equalsIgnoreCase("POST")){
			filledForm = form.bindFromRequest();
			if(filledForm.hasErrors()){
				status = badRequest(views.html.sample9.render(title, sample, data, filledForm));
			} else {
				Credentials credentials = filledForm.get();
				session().put("clientId", credentials.clientId);
				session().put("privateKey", credentials.privateKey);
				
				Map<String, String[]> formData = request().body().asFormUrlEncoded();
				String fileGuid = formData.get("fileGuid") != null ? formData.get("fileGuid")[0] : null;
				fileGuid = StringUtils.isBlank(fileGuid) ? null : fileGuid.trim();
				String width = formData.get("width") != null ? formData.get("width")[0] : null;
				width = StringUtils.isBlank(width) ? null : width.trim();
				String height = formData.get("height") != null ? formData.get("height")[0] : null;
				height = StringUtils.isBlank(height) ? null : height.trim();
				
				try {
					if(fileGuid == null || width == null || height == null){
						throw new Exception();
					}
				
					ApiInvoker.getInstance().setRequestSigner(
							new GroupDocsRequestSigner(credentials.privateKey));
					
					DocApi api = new DocApi();
					GetDocumentInfoResponse response = api.GetDocumentMetadata(credentials.clientId, fileGuid);
					if(response != null && response.getStatus().trim().equalsIgnoreCase("Ok")){
						GetDocumentInfoResult result = response.getResult();
						data = new HashMap<String, String>();
						data.put("guid", fileGuid);
						data.put("id", result.getId().toString());
						data.put("width", width);
						data.put("height", height);
						data.put("pages", result.getPage_count().toString());
						data.put("views", result.getViews_count().toString() + " times");
					} else {
						throw new Exception("Not Found");
					}
					
					status = ok(views.html.sample9.render(title, sample, data, filledForm));
				} catch (ApiException e) {
					if(e.getCode() == 401){
						List<Object> args = Arrays.asList(new Object[]{"https://apps.groupdocs.com/My/Manage", "Production Server"});
						filledForm.reject("Wrong Credentials. Please make sure to use credentials from", args);
					} else {
						filledForm.reject("Failed to access API: " + e.getMessage());
					}
					status = badRequest(views.html.sample9.render(title, sample, data, filledForm));
				} catch (Exception e) {
					e.printStackTrace();
					if(fileGuid == null){
						filledForm.reject("fileGuid", "This field is required");
					} else if(width == null){
						filledForm.reject("width", "This field is required");
					} else if(height == null){
						filledForm.reject("height", "This field is required");
					} else {
						filledForm.reject("fileGuid", "Something wrong with your file: " + e.getMessage());
					}
					status = badRequest(views.html.sample9.render(title, sample, data, filledForm));
				}
			}
		} else {
			filledForm = form.bind(session());
			status = ok(views.html.sample9.render(title, sample, data, filledForm));
		}
		return status;
	}
	
}