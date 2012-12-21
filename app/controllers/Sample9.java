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
//import com.groupdocs.sdk.common.GroupDocsRequestSigner;
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
//			if(filledForm.hasErrors()){
//				
//				status = badRequest(views.html.sample9.render(title, sample, data, filledForm));
//			} else {
//				Credentials credentials = filledForm.get();
//				session().put("clientId", credentials.clientId);
//				session().put("privateKey", credentials.privateKey);
			
				Map<String, String[]> formData = request().body().asFormUrlEncoded();
				String fileId = formData.get("fileId") != null ? formData.get("fileId")[0] : null;
				fileId = StringUtils.isBlank(fileId) ? null : fileId.trim();
				String width = formData.get("width") != null ? formData.get("width")[0] : null;
				width = StringUtils.isBlank(width) ? null : width.trim();
				String height = formData.get("height") != null ? formData.get("height")[0] : null;
				height = StringUtils.isBlank(height) ? null : height.trim();
				
				try {
					if(fileId != null || width != null || height != null){

						data = new HashMap<String, String>();
						data.put("guid", fileId);
						data.put("width", width);
						data.put("height", height);
									
					} else {
						throw new Exception("Not Found");
						}
					
					status = ok(views.html.sample9.render(title, sample, data, filledForm));

				} catch (Exception e) {
					e.printStackTrace();
					if(fileId == null){
						filledForm.reject("fileId", "This field is required");
					} else if(width == null){
						filledForm.reject("width", "This field is required");
					} else if(height == null){
						filledForm.reject("height", "This field is required");
					} else {
						filledForm.reject("fileId", "Something wrong with your file: " + e.getMessage());
					}
					status = badRequest(views.html.sample9.render(title, sample, data, filledForm));
				}

		} else {
			filledForm = form.bind(session());
			status = ok(views.html.sample9.render(title, sample, data, filledForm));
		}
		return status;
	}
	
}