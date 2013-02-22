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
import com.groupdocs.sdk.model.ViewDocumentResponse;

public class Sample23 extends Controller {
	//###Set variables
	static String title = "GroupDocs Java SDK Samples";
	static Form<Credentials> form = form(Credentials.class);
	
	public static Result index() {
		String result = "";
		Form<Credentials> filledForm;
		String sample = "Sample23";
		Status status;
		//Check POST parameters
		if(request().method().equalsIgnoreCase("POST")){
			filledForm = form.bindFromRequest();
			if(filledForm.hasErrors()){
				status = badRequest(views.html.sample23.render(title, sample, result, filledForm));
			} else {
				//Get POST data
				Credentials credentials = filledForm.get();
				session().put("client_id", credentials.client_id);
				session().put("private_key", credentials.private_key);
				
				Map<String, String[]> formData = request().body().asFormUrlEncoded();
				String fileGuid = formData.get("fileId") != null ? formData.get("fileId")[0] : null;
				fileGuid = StringUtils.isBlank(fileGuid) ? null : fileGuid.trim();
				String basePath = formData.get("server_type") != null ? formData.get("server_type")[0] : null;
				basePath = StringUtils.isBlank(basePath) ? null : basePath.trim();
				
				try {
					//Check fileGuid
					if(credentials.client_id == null || credentials.private_key == null || fileGuid == null){
						throw new Exception();
					}
					 //###Create ApiInvoker, DocApi objects

		            //Create ApiInvoker object
					ApiInvoker.getInstance().setRequestSigner(
							new GroupDocsRequestSigner(credentials.private_key));
					//Create DocApi object
					DocApi api = new DocApi();
					api.setBasePath(basePath);
					//Get document metadata
					ViewDocumentResponse response = api.ViewDocument(credentials.client_id, fileGuid, "0", "-1", "", "100", "");
					
					if (response.getStatus().equals("Ok")) {
						 //### If request was successfull
			               
		                //Generation of iframe URL using $pageImage->result->guid
		                //iframe to prodaction server
		                if (basePath.equals("https://api.groupdocs.com/v2.0")) {
		                    result = "https://apps.groupdocs.com/document-viewer/embed/" + response.getResult().getGuid() + "?frameborder=0 width=500 height=650";
		                //iframe to dev server
		                } else if(basePath.equals("https://dev-api.groupdocs.com/v2.0")) {
		                    result = "https://dev-apps.groupdocs.com/document-viewer/embed/" + response.getResult().getGuid() + "?frameborder=0 width=500 height=650";
		                //iframe to test server
		                } else if(basePath.equals("https://stage-api.groupdocs.com/v2.0")) {
		                    result = "https://stage-apps.groupdocs.com/document-viewer/embed/" + response.getResult().getGuid() + "?frameborder=0 width=500 height=650";
		                }
		                
					}
					//If request was successfull - set  thumbnailUrls variable for template
					status = ok(views.html.sample23.render(title, sample, result, filledForm));
				//###Definition of Api errors and conclusion of the corresponding message
				} catch (ApiException e) {
					if(e.getCode() == 401){
						List<Object> args = Arrays.asList(new Object[]{"https://apps.groupdocs.com/My/Manage", "Production Server"});
						filledForm.reject("Wrong Credentials. Please make sure to use credentials from", args);
					} else {
						filledForm.reject("Failed to access API: " + e.getMessage());
					}
					status = badRequest(views.html.sample23.render(title, sample, result, filledForm));
				//###Definition of filledForm errors and conclusion of the corresponding message
				} catch (Exception e) {
					e.printStackTrace();
					if(fileGuid == null){
						filledForm.reject("fileGuid", "This field is required");
					} else {
						filledForm.reject("fileGuid", "Something wrong with your file: " + e.getMessage());
					}
					status = badRequest(views.html.sample23.render(title, sample, result, filledForm));
				}
			}
		} else {
			filledForm = form.bind(session());
			status = ok(views.html.sample23.render(title, sample, result, filledForm));
		}
		//Process template
		return status;
	}
	
}