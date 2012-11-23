package controllers;

import java.io.FileInputStream;
import java.util.List;

import models.Credentials;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import scala.actors.threadpool.Arrays;

import com.groupdocs.sdk.api.StorageApi;
import com.groupdocs.sdk.common.ApiException;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.FileStream;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.UploadRequestResult;
import com.groupdocs.sdk.model.UploadResponse;

public class Sample3 extends Controller {

	static String title = "GroupDocs Java SDK Samples";
	static Form<Credentials> form = form(Credentials.class);
	
	public static Result index() {
		UploadRequestResult file = null;
		Form<Credentials> filledForm;
		String sample = "Sample3";
		Status status;
		if(request().method().equalsIgnoreCase("POST")){
			filledForm = form.bindFromRequest();
			if(filledForm.hasErrors()){
				status = badRequest(views.html.sample3.render(title, sample, file, filledForm));
			} else {
				Credentials credentials = filledForm.get();
				session().put("clientId", credentials.clientId);
				session().put("privateKey", credentials.privateKey);
				
				MultipartFormData body = request().body().asMultipartFormData();
		        FilePart resourceFile = body.getFile("resourceFile");
		        
				try {
					ApiInvoker.getInstance().setRequestSigner(
							new GroupDocsRequestSigner(credentials.privateKey));
					StorageApi api = new StorageApi();
					FileInputStream is = new FileInputStream(resourceFile.getFile());
					UploadResponse response = api.Upload(credentials.clientId, resourceFile.getFilename(), null, new FileStream(is));
					if(response != null && response.getStatus().trim().equalsIgnoreCase("Ok")){
						file = response.getResult();
					}
					status = ok(views.html.sample3.render(title, sample, file, filledForm));
				} catch (ApiException e) {
					if(e.getCode() == 401){
						List<Object> args = Arrays.asList(new Object[]{"https://apps.groupdocs.com/My/Manage", "Production Server"});
						filledForm.reject("Wrong Credentials. Please make sure to use credentials from", args);
					} else {
						filledForm.reject("Failed to access API: " + e.getMessage());
					}
					status = badRequest(views.html.sample3.render(title, sample, file, filledForm));
				} catch (Exception e) {
					if(resourceFile == null){
						filledForm.reject("resourceFile", "This field is required");
					} else {
						filledForm.reject("resourceFile", "Something wrong with your file: " + e.getMessage());
					}
					status = badRequest(views.html.sample3.render(title, sample, file, filledForm));
				}
			}
		} else {
			filledForm = form.bind(session());
			status = ok(views.html.sample3.render(title, sample, file, filledForm));
		}
		return status;
	}
	
}