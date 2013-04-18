//###<i>This sample will show how to use <b>SetAnnotationCollaborators</b> method from Annotation Api to set collaborator for document</i>
package controllers;
//Import of necessary libraries
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import models.Credentials;

import org.apache.commons.lang3.StringUtils;

import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import scala.actors.threadpool.Arrays;

import com.groupdocs.sdk.common.ApiException;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.ConvertResponse;
import com.groupdocs.sdk.model.ConvertRequestResult;
import com.groupdocs.sdk.model.FileSystemDocument;
import com.groupdocs.sdk.model.GetJobDocumentsResponse;
import com.groupdocs.sdk.model.GetJobDocumentsResult;
import com.groupdocs.sdk.model.GetJobResponse;
import com.groupdocs.sdk.model.GetJobResult;
import com.groupdocs.sdk.model.ListEntitiesResponse;
import com.groupdocs.sdk.model.JobInputDocument;
import com.groupdocs.sdk.api.AsyncApi;
import com.groupdocs.sdk.api.StorageApi;

public class Sample18 extends Controller {
	//###Set variables
	static String title = "GroupDocs Java SDK Samples";
	static Form<Credentials> form = form(Credentials.class);
	
	public static Result index() {
		
		String result = null;
		GetJobDocumentsResponse jobInfo = null;
		
		Double jobId = null;
		Form<Credentials> filledForm;
		String sample = "Sample18";
		Status status;
		//Check POST parameters
		if(request().method().equalsIgnoreCase("POST")){
			filledForm = form.bindFromRequest();
			if(filledForm.hasErrors()){
				//If filledForm have errors return to template
				status = badRequest(views.html.sample18.render(title, sample, result, filledForm));
			} else {
				//If filledForm have no errors get all parameters
				Credentials credentials = filledForm.get();
				session().put("client_id", credentials.client_id);
				session().put("private_key", credentials.private_key);
				session().put("baseurl", credentials.baseurl);
				
				Map<String, String[]> formData = request().body().asFormUrlEncoded();
				String fileId = formData.get("fileId") != null ? formData.get("fileId")[0] : null;
				fileId = StringUtils.isBlank(fileId) ? null : fileId.trim();
				String convert_type = formData.get("convert_type") != null ? formData.get("convert_type")[0] : null;
				convert_type = StringUtils.isBlank(convert_type) ? null : convert_type.trim();
				
				try {
					//### Check client_id, private_key, fileId and email
					if(credentials.client_id == null || credentials.private_key == null || fileId == null || convert_type == null){
						throw new Exception();
					}
					//###Create ApiInvoker, AntApi objects

		            //Create ApiInvoker object
					ApiInvoker.getInstance().setRequestSigner(
							new GroupDocsRequestSigner(credentials.private_key));
					//Create AntApi object
					AsyncApi api = new AsyncApi();
					api.setBasePath(credentials.baseurl);
		
					//###Make request to Annotation api for setting collaborator for document  
					ConvertResponse response = api.Convert(credentials.client_id, fileId, convert_type, null, null, null, null, null);
					//Check request result
					if(response != null && response.getStatus().trim().equalsIgnoreCase("Ok")){
						//If request status Ok get results
						
						jobId = response.getResult().getJob_id();
						Thread.sleep(5000);
						jobInfo = api.GetJobDocuments(credentials.client_id, jobId.toString(), "");
						
						if(jobInfo != null && jobInfo.getStatus().trim().equalsIgnoreCase("Ok")){
							
							result = jobInfo.getResult().getInputs().get(0).getOutputs().get(0).getGuid();
							
						}
					
					} else {
						//If status error throw exception massage
						throw new Exception("User identified by " + " not Found");
					}
					//If request was successful - set result variable for template
					status = ok(views.html.sample18.render(title, sample, result, filledForm));
				//###Definition of Api errors and conclusion of the corresponding message
				} catch (ApiException e) {
					if(e.getCode() == 401){
						List<Object> args = Arrays.asList(new Object[]{"https://apps.groupdocs.com/My/Manage", "Production Server"});
						filledForm.reject("Wrong Credentials. Please make sure to use credentials from", args);
					} else {
						filledForm.reject("Failed to access API: " + e.getMessage());
					}
					status = badRequest(views.html.sample18.render(title, sample, result, filledForm));
				//###Definition of filledForm errors and conclusion of the corresponding message
				} catch (Exception e) {
					e.printStackTrace();
					if(fileId == null){
						filledForm.reject("fileId", "This field is required");
					} else {
						filledForm.reject("fileId", "Something wrong with your file: " + e.getMessage());
					}
					status = badRequest(views.html.sample18.render(title, sample, result, filledForm));
				} 
			}
		} else {
			filledForm = form.bind(session());
			session().put("baseurl", "https://api.groupdocs.com/v2.0");
			status = ok(views.html.sample18.render(title, sample, result, filledForm));
		}
		//Process template
		return status;
	}
	
}