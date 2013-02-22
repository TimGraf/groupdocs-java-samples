 //###<i>This sample will show how to use <b>GetFile</b> method from Storage Api to download a file from GroupDocs Storage</i>
package controllers;
//Import of necessary libraries
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.io.InputStream;
import java.lang.reflect.Array;

import javax.swing.text.html.HTML;

import models.Credentials;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import scala.actors.threadpool.Arrays;

import com.groupdocs.sdk.common.ApiException;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.api.ComparisonApi;
import com.groupdocs.sdk.model.ChangeInfo;
import com.groupdocs.sdk.model.ChangesResponse;

public class Sample20 extends Controller {
	//###Set variables
	static String title = "GroupDocs Java SDK Samples";
	static Form<Credentials> form = form(Credentials.class);
	
	public static Result index() {
		String result = null;
		String action = null;
		String page = null;
		String text = null;
		String type = null;
		HashMap<String, String> changes = new HashMap();
		ArrayList<HashMap<String, String>> content = new ArrayList<HashMap<String, String>>();
		String table = null;
		Form<Credentials> filledForm;
		String sample = "Sample20";
		Status status;
		//Check POST parameters
		if(request().method().equalsIgnoreCase("POST")){
			filledForm = form.bindFromRequest();
			if(filledForm.hasErrors()){
				status = badRequest(views.html.sample20.render(title, sample, result, filledForm));
			} else {
				//Get POST data
				Credentials credentials = filledForm.get();
				session().put("client_id", credentials.client_id);
				session().put("private_key", credentials.private_key);
				
				Map<String, String[]> formData = request().body().asFormUrlEncoded();
				String file_id = formData.get("fileId") != null ? formData.get("fileId")[0] : null;
				file_id = StringUtils.isBlank(file_id) ? null : file_id.trim();
				
				
				try {
					//Check entered files id
					if(file_id == null){
						throw new Exception();
					}
					//###Create ApiInvoker, Storage Api objects
					
					//Create ApiInvoker object
					ApiInvoker.getInstance().setRequestSigner(
							new GroupDocsRequestSigner(credentials.private_key));
					//Create StorageApi object
					ComparisonApi api = new ComparisonApi();
					
					ChangesResponse getChanges = api.GetChanges(credentials.client_id, file_id);
					
					if (getChanges.getStatus().trim().equalsIgnoreCase("Ok")) {

						table = "<table border=1>";
						table += "<tr><td><font color='green'>Change Name</font></td><td><font color='green'>Change</font></td></tr>";
						
						for (int i = 0; i < getChanges.getResult().getChanges().size(); i++) {
						
							if (getChanges.getResult().getChanges().get(i).getAction() == null) {
								action = "";
							} else {
								action = getChanges.getResult().getChanges().get(i).getAction().toString();
							}
							
							if (getChanges.getResult().getChanges().get(i).getPage() == null) {
								page = "";
							} else {
								page = getChanges.getResult().getChanges().get(i).getPage().toString();
							}
							
							if ( getChanges.getResult().getChanges().get(i).getText() == null) {
								text = "";
							} else {
								text = getChanges.getResult().getChanges().get(i).getText().toString();
							}
							
							if ( getChanges.getResult().getChanges().get(i).getType() == null) {
								type = "";
							} else {
								type = getChanges.getResult().getChanges().get(i).getType().toString();
							}
							
							table += "<tr><td>Id</td><td>" + getChanges.getResult().getChanges().get(i).getId().toString() + "</td></tr>";
							table += "<tr><td>Action</td><td>" + action + "</td></tr>";
							table += "<tr><td>Text</td><td>" + text + "</td></tr>";
							table += "<tr><td>Type</td><td>" + type + "</td></tr>";
							table += "<tr><td>Page</td><td>" + page + "</td></tr>";
							table += "<tr bgcolor='#808080'><td></td><td></td></tr>";
						}
						
						table += "</table>";
			     		result = table;
					       
					}
										
	                //If request was successfull - set file variable for template
	                status = ok(views.html.sample20.render(title, sample, result, filledForm));
	            //###Definition of Api errors and conclusion of the corresponding message
				} catch (ApiException e) {
					if(e.getCode() == 401){
						List<Object> args = Arrays.asList(new Object[]{"https://apps.groupdocs.com/My/Manage", "Production Server"});
						filledForm.reject("Wrong Credentials. Please make sure to use credentials from", args);
					} else {
						filledForm.reject("Failed to access API: " + e.getMessage());
					}
					status = badRequest(views.html.sample20.render(title, sample, result, filledForm));
				//###Definition of filledForm errors and conclusion of the corresponding message
				} catch (Exception e) {
					e.printStackTrace();
					if(file_id == null){
						filledForm.reject("file_id", "This field is required");
					} else {
						filledForm.reject("file_id", "Something wrong with your file: " + e.getMessage());
					}
					status = badRequest(views.html.sample20.render(title, sample, result, filledForm));
				} 

			}
		} else {
			filledForm = form.bind(session());
			status = ok(views.html.sample20.render(title, sample, result, filledForm));
		}
		//Process template
		return status;
	}
	
}