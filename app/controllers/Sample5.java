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
import com.groupdocs.sdk.api.StorageApi;
import com.groupdocs.sdk.common.ApiException;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.FileMoveResponse;
import com.groupdocs.sdk.model.FileMoveResult;
import com.groupdocs.sdk.model.GetDocumentInfoResponse;

public class Sample5 extends Controller {

	static String title = "GroupDocs Java SDK Samples";
	static Form<Credentials> form = form(Credentials.class);
	
	public static Result index() {
		FileMoveResult moveResult = null;
		Form<Credentials> filledForm;
		String sample = "Sample5";
		Status status;
		if(request().method().equalsIgnoreCase("POST")){
			filledForm = form.bindFromRequest();
			if(filledForm.hasErrors()){
				status = badRequest(views.html.sample5.render(title, sample, moveResult, filledForm));
			} else {
				Credentials credentials = filledForm.get();
				session().put("clientId", credentials.clientId);
				session().put("privateKey", credentials.privateKey);
				
				Map<String, String[]> formData = request().body().asFormUrlEncoded();
				String srcPath = formData.get("srcPath") != null ? formData.get("srcPath")[0] : null;
				srcPath = StringUtils.isBlank(srcPath) ? null : srcPath.trim();
				String destPath = formData.get("destPath") != null ? formData.get("destPath")[0] : null;
				destPath = StringUtils.isBlank(destPath) ? null : destPath.trim();
				boolean isCopy;
				if(formData.containsKey("move")){
					isCopy = false;
				} else {
					isCopy = true;
				}
		        
				try {
					if(srcPath == null || destPath == null){
						throw new Exception();
					}
					
					ApiInvoker.getInstance().setRequestSigner(
							new GroupDocsRequestSigner(credentials.privateKey));
					
					GetDocumentInfoResponse metadata = new DocApi().GetDocumentMetadataByPath(credentials.clientId, srcPath);
					Long fileId = null;
					if(metadata != null && metadata.getStatus().trim().equalsIgnoreCase("Ok")){
						fileId = metadata.getResult().getId().longValue();
					} else {
						throw new Exception("Not Found");
					}
					
					StorageApi api = new StorageApi();
					FileMoveResponse response;
					if(isCopy){
						response = api.MoveFile(credentials.clientId, destPath, null, fileId.toString(), null);
					} else {
						response = api.MoveFile(credentials.clientId, destPath, null, null, fileId.toString());
					}
					if(response != null && response.getStatus().trim().equalsIgnoreCase("Ok")){
						moveResult = response.getResult();
					}
					status = ok(views.html.sample5.render(title, sample, moveResult, filledForm));
				} catch (ApiException e) {
					if(e.getCode() == 401){
						List<Object> args = Arrays.asList(new Object[]{"https://apps.groupdocs.com/My/Manage", "Production Server"});
						filledForm.reject("Wrong Credentials. Please make sure to use credentials from", args);
					} else {
						filledForm.reject("Failed to access API: " + e.getMessage());
					}
					status = badRequest(views.html.sample5.render(title, sample, moveResult, filledForm));
				} catch (Exception e) {
					if(srcPath == null){
						filledForm.reject("srcPath", "This field is required");
					} else if(destPath == null){
						filledForm.reject("destPath", "This field is required");
					} else {
						filledForm.reject("srcPath", "Something wrong with your file: " + e.getMessage());
					}
					status = badRequest(views.html.sample5.render(title, sample, moveResult, filledForm));
				}
			}
		} else {
			filledForm = form.bind(session());
			status = ok(views.html.sample5.render(title, sample, moveResult, filledForm));
		}
		return status;
	}
	
}