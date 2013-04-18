//###<i>This sample will show how to use <b>GetDocumentPagesImageUrls</b> method from Doc Api to return a URL representing a single page of a Document</i>
package controllers;
//Import of necessary libraries
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

public class Sample08 extends Controller {
	//###Set variables
	static String title = "GroupDocs Java SDK Samples";
	static Form<Credentials> form = form(Credentials.class);
	
	public static Result index() {
		List<String> thumbnailUrls = null;
		Form<Credentials> filledForm;
		String sample = "Sample8";
		Status status;
		//Check POST parameters
		if(request().method().equalsIgnoreCase("POST")){
			filledForm = form.bindFromRequest();
			if(filledForm.hasErrors()){
				status = badRequest(views.html.sample08.render(title, sample, thumbnailUrls, filledForm));
			} else {
				//Get POST data
				Credentials credentials = filledForm.get();
				session().put("client_id", credentials.client_id);
				session().put("private_key", credentials.private_key);
				session().put("baseurl", credentials.baseurl);
				
				Map<String, String[]> formData = request().body().asFormUrlEncoded();
				String fileGuid = formData.get("fileId") != null ? formData.get("fileId")[0] : null;
				fileGuid = StringUtils.isBlank(fileGuid) ? null : fileGuid.trim();
				String pageNumber = formData.get("pageNumber") != null ? formData.get("pageNumber")[0] : null;
				pageNumber = StringUtils.isBlank(pageNumber) ? null : pageNumber.trim();
				
				try {
					//Check fileGuid
					if(fileGuid == null){
						throw new Exception();
					}
					 //###Create ApiInvoker, DocApi objects

		            //Create ApiInvoker object
					ApiInvoker.getInstance().setRequestSigner(
							new GroupDocsRequestSigner(credentials.private_key));
					//Create DocApi object
					DocApi api = new DocApi();
					api.setBasePath(credentials.baseurl);
					//Get document metadata
					GetDocumentInfoResponse response = api.GetDocumentMetadata(credentials.client_id, fileGuid);
					//Convert pageNumber from String to Integer
					int page = Integer.parseInt( pageNumber );
					//###Make request to DocApi using client_id
		            
		            //Obtaining URl of entered page 
					thumbnailUrls = api.GetDocumentPagesImageUrls(credentials.client_id, fileGuid, page, 1, "150x150", null, null, null).getResult().getUrl();
					//If request was successfull - set  thumbnailUrls variable for template
					status = ok(views.html.sample08.render(title, sample, thumbnailUrls, filledForm));
				//###Definition of Api errors and conclusion of the corresponding message
				} catch (ApiException e) {
					if(e.getCode() == 401){
						List<Object> args = Arrays.asList(new Object[]{"https://apps.groupdocs.com/My/Manage", "Production Server"});
						filledForm.reject("Wrong Credentials. Please make sure to use credentials from", args);
					} else {
						filledForm.reject("Failed to access API: " + e.getMessage());
					}
					status = badRequest(views.html.sample08.render(title, sample, thumbnailUrls, filledForm));
				//###Definition of filledForm errors and conclusion of the corresponding message
				} catch (Exception e) {
					e.printStackTrace();
					if(fileGuid == null){
						filledForm.reject("fileGuid", "This field is required");
					} else if(pageNumber == null){
						filledForm.reject("pageNumber", "This field is required");
					} else {
						filledForm.reject("fileGuid", "Something wrong with your file: " + e.getMessage());
					}
					status = badRequest(views.html.sample08.render(title, sample, thumbnailUrls, filledForm));
				}
			}
		} else {
			filledForm = form.bind(session());
			session().put("baseurl", "https://api.groupdocs.com/v2.0");
			status = ok(views.html.sample08.render(title, sample, thumbnailUrls, filledForm));
		}
		//Process template
		return status;
	}
	
}