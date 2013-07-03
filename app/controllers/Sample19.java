 //###<i>This sample will show how to use <b>GetFile</b> method from Storage Api to download a file from GroupDocs Storage</i>
package controllers;
//Import of necessary libraries
import java.util.List;
import java.util.Map;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.File;
import java.io.InputStream;


import models.Credentials;

import org.apache.commons.lang3.StringUtils;

import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import scala.actors.threadpool.Arrays;

import com.groupdocs.sdk.common.ApiException;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.model.CompareResponse;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.api.ComparisonApi;
import com.groupdocs.sdk.api.AsyncApi;
import com.groupdocs.sdk.model.GetJobDocumentsResponse;

public class Sample19 extends Controller {
	//###Set variables
	static String title = "GroupDocs Java SDK Samples";
	static Form<Credentials> form = form(Credentials.class);
	
	public static Result index() {
		String result = null;
		Form<Credentials> filledForm;
		String sample = "Sample19";
		Status status;
		//Check POST parameters
		if(request().method().equalsIgnoreCase("POST")){
			filledForm = form.bindFromRequest();
			if(filledForm.hasErrors()){
				status = badRequest(views.html.sample19.render(title, sample, result, filledForm));
			} else {
				//Get POST data
				Credentials credentials = filledForm.get();
				session().put("client_id", credentials.client_id);
				session().put("private_key", credentials.private_key);
				session().put("server_type", credentials.server_type);
				
				Map<String, String[]> formData = request().body().asFormUrlEncoded();
				String source_file_id = formData.get("sourceFileId") != null ? formData.get("sourceFileId")[0] : null;
				source_file_id = StringUtils.isBlank(source_file_id) ? null : source_file_id.trim();
				String target_file_id = formData.get("targetFileId") != null ? formData.get("targetFileId")[0] : null;
				target_file_id = StringUtils.isBlank(target_file_id) ? null : target_file_id.trim();
				String callback = formData.get("callbackUrl") != null ? formData.get("callbackUrl")[0] : null;
				callback = StringUtils.isBlank(callback) ? null : callback.trim();
				String basePath = credentials.server_type;
				basePath = StringUtils.isBlank(basePath) ? null : basePath.trim();
				
				try {
					//Check entered files id
					if(source_file_id == null || target_file_id == null){
						throw new Exception();
					}
					//###Create ApiInvoker, Storage Api objects
					
					//Create ApiInvoker object
					ApiInvoker.getInstance().setRequestSigner(
							new GroupDocsRequestSigner(credentials.private_key));
					//Create StorageApi object
					ComparisonApi api = new ComparisonApi();
					//Choose which server to use
					api.setBasePath(basePath);
					//Check is callback entered
					if(callback == null){
						callback = "";
					}
					//Make request to Comparison Api to compare documents
					CompareResponse compare = api.Compare(credentials.client_id, source_file_id, target_file_id, callback);
					//###Check request status
					
					if (compare.getStatus().trim().equalsIgnoreCase("Ok")) {
						Thread.sleep(5000);
						//If status Ok create AsyncApi 
						AsyncApi async = new AsyncApi();
						//Choose server to use
						async.setBasePath(basePath);
						//Make request to Async Api to get file GuId by Job Id
						GetJobDocumentsResponse job_info = async.GetJobDocuments(credentials.client_id, compare.getResult().getJob_id().toString(), "");
						//Get file GuId from request result
						String guid = job_info.getResult().getOutputs().get(0).getGuid();
						//Generation of iframe URL using $pageImage->result->guid
		                //iframe to prodaction server
						if (basePath.equals("https://api.groupdocs.com/v2.0")) {
		                    result = "https://apps.groupdocs.com/document-viewer/embed/" + guid + "?frameborder=0 width=500 height=650";
		                //iframe to dev server
		                } else if(basePath.equals("https://dev-api.groupdocs.com/v2.0")) {
		                    result = "https://dev-apps.groupdocs.com/document-viewer/embed/" + guid + "?frameborder=0 width=500 height=650";
		                //iframe to test server
		                } else if(basePath.equals("https://stage-api.groupdocs.com/v2.0")) {
		                    result = "https://stage-apps.groupdocs.com/document-viewer/embed/" + guid + "?frameborder=0 width=500 height=650";
		                }
						
					}
										
	                //If request was successfull - set file variable for template
	                status = ok(views.html.sample19.render(title, sample, result, filledForm));
	            //###Definition of Api errors and conclusion of the corresponding message
				} catch (ApiException e) {
					if(e.getCode() == 401){
						List<Object> args = Arrays.asList(new Object[]{"https://apps.groupdocs.com/My/Manage", "Production Server"});
						filledForm.reject("Wrong Credentials. Please make sure to use credentials from", args);
					} else {
						filledForm.reject("Failed to access API: " + e.getMessage());
					}
					status = badRequest(views.html.sample19.render(title, sample, result, filledForm));
				//###Definition of filledForm errors and conclusion of the corresponding message
				} catch (Exception e) {
					e.printStackTrace();
					if(source_file_id == null){
						filledForm.reject("source_file_id", "This field is required");
					} else {
						filledForm.reject("source_file_id", "Something wrong with your file: " + e.getMessage());
					}
					if(target_file_id == null){
						filledForm.reject("target_file_id", "This field is required");
					} else {
						filledForm.reject("target_file_id", "Something wrong with your file: " + e.getMessage());
					}
					status = badRequest(views.html.sample19.render(title, sample, result, filledForm));
				} 

			}
		} else {
			filledForm = form.bind(session());
			session().put("server_type", "https://api.groupdocs.com/v2.0");
			status = ok(views.html.sample19.render(title, sample, result, filledForm));
		}
		//Process template
		return status;
	}
	
}