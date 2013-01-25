//###<i>This sample will show how to use <b>GuId</b> of file to insert Assembly questionary into webpage</i>
package controllers;
//Import of necessary libraries
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

public class Sample16 extends Controller {
	//###Set variables
	static String title = "GroupDocs Java SDK Samples";
	static Form<Credentials> form = form(Credentials.class);
	
	public static Result index() {
		HashMap<String, String> data = null;
		Form<Credentials> filledForm;
		String sample = "Sample16";
		Status status;
		//Check POST parameters
		if(request().method().equalsIgnoreCase("POST")){
			filledForm = form.bindFromRequest();
				//Get POST data
				Map<String, String[]> formData = request().body().asFormUrlEncoded();
				String fileId = formData.get("fileId") != null ? formData.get("fileId")[0] : null;
				fileId = StringUtils.isBlank(fileId) ? null : fileId.trim();
								
				try {
					//CHeck fileId, width and height
					if(fileId != null){
						//Put entered parameters to data HashMap
						data = new HashMap<String, String>();
						data.put("guid", fileId);
															
					} else {
						throw new Exception("Not Found");
						}
					//If request was successful - set  data variable for template
					status = ok(views.html.sample16.render(title, sample, data, filledForm));
				//###Definition of filledForm errors and conclusion of the corresponding message
				} catch (Exception e) {
					e.printStackTrace();
					if(fileId == null){
						filledForm.reject("fileId", "This field is required");
					} else {
						filledForm.reject("fileId", "Something wrong with your file: " + e.getMessage());
					}
					status = badRequest(views.html.sample16.render(title, sample, data, filledForm));
				}

		} else {
			filledForm = form.bind(session());
			status = ok(views.html.sample16.render(title, sample, data, filledForm));
		}
		//Process template
		return status;
	}
	
}