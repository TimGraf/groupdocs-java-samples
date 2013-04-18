//###This sample will show how to use <b>ListAnnotations</b> method from Storage  API  to list annotations for document
package controllers;
//Import of necessary libraries
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import models.Credentials;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import scala.actors.threadpool.Arrays;

import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.api.AntApi;
import com.groupdocs.sdk.model.AnnotationInfo;
import com.groupdocs.sdk.model.ListAnnotationsResponse;

public class Sample12 extends Controller {
	//###Set variables
	static String title = "GroupDocs Java SDK Samples";
	static Form<Credentials> form = form(Credentials.class);
	
	public static Result index() {
		
		List<AnnotationInfo> annotations = null;
		Form<Credentials> filledForm;
		String sample = "Sample12";
		Status status;
		//Check POST parameters
		if(request().method().equalsIgnoreCase("POST")){
			filledForm = form.bindFromRequest();
			if(filledForm.hasErrors()){
				status = badRequest(views.html.sample12.render(title, sample, annotations, filledForm));
			} else {
				//Get POST data
				Credentials credentials = filledForm.get();
				session().put("client_id", credentials.client_id);
				session().put("private_key", credentials.private_key);
				session().put("baseurl", credentials.baseurl);
				
				Map<String, String[]> formData = request().body().asFormUrlEncoded();
				String fileId = formData.get("fileId") != null ? formData.get("fileId")[0] : null;
				fileId = StringUtils.isBlank(fileId) ? null : fileId.trim();
				
				//###Create ApiInvoker, Annotation API objects
				try {
					
					if(credentials.client_id == null || credentials.private_key == null || fileId == null){
						throw new Exception();
					}
					 //Create ApiInvoker object
					ApiInvoker.getInstance().setRequestSigner(
							new GroupDocsRequestSigner(credentials.private_key));
					//Create Annotation api object
					AntApi ant = new AntApi(); 
					ant.setBasePath(credentials.baseurl);
					//Make request to Annotation api to receive list of annotations
					ListAnnotationsResponse response = ant.ListAnnotations(credentials.client_id, fileId);
					//Check request status
					if(response != null && response.getStatus().trim().equalsIgnoreCase("Ok")){
						//If status Ok get annotations
						annotations = response.getResult().getAnnotations();
						
					} else {
						//If status error throw exception massage
						throw new Exception("It wasn't succeeded to get the annotations list");
						
					}
					
					//If request was successful - set files variable for template
					status = ok(views.html.sample12.render(title, sample, annotations, filledForm));
				//###Definition of Api errors and conclusion of the corresponding message
				} catch (Exception e) {
					if(e.equals(401)){
						List<Object> args = Arrays.asList(new Object[]{"https://apps.groupdocs.com/My/Manage", "Production Server"});
						filledForm.reject("Wrong Credentials. Please make sure to use credentials from", args);
				} else {
						filledForm.reject("Failed to access API: " + e.getMessage());
					}
					status = badRequest(views.html.sample12.render(title, sample, annotations, filledForm));
				}
			}
		} else {
			filledForm = form.bind(session());
			session().put("baseurl", "https://api.groupdocs.com/v2.0");
			status = ok(views.html.sample12.render(title, sample, annotations, filledForm));
		}
		//Process template
		return status;
	}

}