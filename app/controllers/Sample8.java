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

public class Sample8 extends Controller {

	static String title = "GroupDocs Java SDK Samples";
	static Form<Credentials> form = form(Credentials.class);
	
	public static Result index() {
		List<String> thumbnailUrls = null;
		Form<Credentials> filledForm;
		String sample = "Sample8";
		Status status;
		if(request().method().equalsIgnoreCase("POST")){
			filledForm = form.bindFromRequest();
			if(filledForm.hasErrors()){
				status = badRequest(views.html.sample8.render(title, sample, thumbnailUrls, filledForm));
			} else {
				Credentials credentials = filledForm.get();
				session().put("clientId", credentials.clientId);
				session().put("privateKey", credentials.privateKey);
				
				Map<String, String[]> formData = request().body().asFormUrlEncoded();
				String fileGuid = formData.get("fileGuid") != null ? formData.get("fileGuid")[0] : null;
				fileGuid = StringUtils.isBlank(fileGuid) ? null : fileGuid.trim();
				String dimension = formData.get("dimension") != null ? formData.get("dimension")[0] : null;
				dimension = StringUtils.isBlank(dimension) ? null : dimension.trim();
				
				try {
					if(fileGuid == null || dimension == null){
						throw new Exception();
					}
				
					ApiInvoker.getInstance().setRequestSigner(
							new GroupDocsRequestSigner(credentials.privateKey));
					
					DocApi api = new DocApi();
					GetDocumentInfoResponse response = api.GetDocumentMetadata(credentials.clientId, fileGuid);
					Integer pageCount = null;
					if(response != null && response.getStatus().trim().equalsIgnoreCase("Ok")){
						pageCount = response.getResult().getPage_count();
					} else {
						throw new Exception("Not Found");
					}
					thumbnailUrls = api.GetDocumentPagesImageUrls(credentials.clientId, fileGuid, 0, pageCount, dimension, null, null, null).getResult().getUrl();
					status = ok(views.html.sample8.render(title, sample, thumbnailUrls, filledForm));
				} catch (ApiException e) {
					if(e.getCode() == 401){
						List<Object> args = Arrays.asList(new Object[]{"https://apps.groupdocs.com/My/Manage", "Production Server"});
						filledForm.reject("Wrong Credentials. Please make sure to use credentials from", args);
					} else {
						filledForm.reject("Failed to access API: " + e.getMessage());
					}
					status = badRequest(views.html.sample8.render(title, sample, thumbnailUrls, filledForm));
				} catch (Exception e) {
					e.printStackTrace();
					if(fileGuid == null){
						filledForm.reject("fileGuid", "This field is required");
					} else if(dimension == null){
						filledForm.reject("dimension", "This field is required");
					} else {
						filledForm.reject("fileGuid", "Something wrong with your file: " + e.getMessage());
					}
					status = badRequest(views.html.sample8.render(title, sample, thumbnailUrls, filledForm));
				}
			}
		} else {
			filledForm = form.bind(session());
			status = ok(views.html.sample8.render(title, sample, thumbnailUrls, filledForm));
		}
		return status;
	}
	
}