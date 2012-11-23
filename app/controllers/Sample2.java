package controllers;

import java.util.List;

import models.Credentials;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import scala.actors.threadpool.Arrays;

import com.groupdocs.sdk.api.StorageApi;
import com.groupdocs.sdk.common.ApiException;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.FileSystemDocument;
import com.groupdocs.sdk.model.ListEntitiesResponse;

public class Sample2 extends Controller {

	static String title = "GroupDocs Java SDK Samples";
	static Form<Credentials> form = form(Credentials.class);
	
	public static Result index() {
		List<FileSystemDocument> files = null;
		Form<Credentials> filledForm;
		String sample = "Sample2";
		Status status;
		if(request().method().equalsIgnoreCase("POST")){
			filledForm = form.bindFromRequest();
			if(filledForm.hasErrors()){
				status = badRequest(views.html.sample2.render(title, sample, files, filledForm));
			} else {
				Credentials credentials = filledForm.get();
				session().put("clientId", credentials.clientId);
				session().put("privateKey", credentials.privateKey);
				try {
					ApiInvoker.getInstance().setRequestSigner(
							new GroupDocsRequestSigner(credentials.privateKey));
					StorageApi api = new StorageApi();
					ListEntitiesResponse response = api.ListEntities(credentials.clientId, "", null, null, null, null, null, null, null);
					if(response != null && response.getStatus().trim().equalsIgnoreCase("Ok")){
						files = response.getResult().getFiles();
					}
					status = ok(views.html.sample2.render(title, sample, files, filledForm));
				} catch (ApiException e) {
					if(e.getCode() == 401){
						List<Object> args = Arrays.asList(new Object[]{"https://apps.groupdocs.com/My/Manage", "Production Server"});
						filledForm.reject("Wrong Credentials. Please make sure to use credentials from", args);
					} else {
						filledForm.reject("Failed to access API: " + e.getMessage());
					}
					status = badRequest(views.html.sample2.render(title, sample, files, filledForm));
				}
			}
		} else {
			filledForm = form.bind(session());
			status = ok(views.html.sample2.render(title, sample, files, filledForm));
		}
		return status;
	}

}