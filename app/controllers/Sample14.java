//###<i>This sample will show how to use <b>GetFolderSharers</b> method from Storage Api to get folders sharers in GroupDocs Storage </i>
package controllers;
//Import of necessary libraries
import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.StringTokenizer;

import common.Utils;
import models.Credentials;

import org.apache.commons.lang3.StringUtils;

import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import scala.actors.threadpool.Arrays;

import com.groupdocs.sdk.api.DocApi;
import com.groupdocs.sdk.api.StorageApi;
import com.groupdocs.sdk.common.ApiException;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.FileSystemFolder;
import com.groupdocs.sdk.model.ListEntitiesResponse;
import com.groupdocs.sdk.model.SharedUsersResponse;
import com.groupdocs.sdk.model.UserInfo;

public class Sample14 extends Controller {
	//###Set variables
	static String title = "GroupDocs Java SDK Samples";
	static Form<Credentials> form = form(Credentials.class);
	
	public static Result index() {
		List<FileSystemFolder> folders = null;
		List<UserInfo> sharer = null;
		Form<Credentials> filledForm;
		String sample = "Sample14";
		Status status;
		//Check POST parameters
		if(request().method().equalsIgnoreCase("POST")){
			filledForm = form.bindFromRequest();
			if(filledForm.hasErrors()){
				status = badRequest(views.html.sample14.render(title, sample, sharer, filledForm));
			} else {
				//Get POST data
				Credentials credentials = filledForm.get();
				session().put("client_id", credentials.getClient_id());
				session().put("private_key", credentials.getPrivate_key());
				session().put("server_type", credentials.getServer_type());
				
				Map<String, String[]> formData = request().body().asFormUrlEncoded();
                Http.MultipartFormData multipartFormData = request().body().asMultipartFormData();
                Map<String, String[]> formUrlEncodedData = multipartFormData.asFormUrlEncoded();
                String path = Utils.getFormValue(formUrlEncodedData, "path");
						        
				try {
					//Check source and destination path 
					if(path == null || credentials.getClient_id() == null || credentials.getPrivate_key() == null){
						throw new Exception();
					}
					//###Create ApiInvoker, Doc and Storage Api objects
					
					//Create ApiInvoker object
					ApiInvoker.getInstance().setRequestSigner(
							new GroupDocsRequestSigner(credentials.getPrivate_key()));
					//Create Doc Api object and get document metadata
					DocApi metadata = new DocApi();
					metadata.setBasePath(credentials.getServer_type());
					//Create Storage Api object
					StorageApi storage = new StorageApi();
					storage.setBasePath(credentials.getServer_type());
					


                    String folderId  ="";
					//###Make request to Storage Api to get list of elements in the storage
					ListEntitiesResponse listResponse = storage.ListEntities(credentials.getClient_id(), "", null, null, null, null, null, null, null);
					//Check request result
					if(listResponse != null && listResponse.getStatus().trim().equalsIgnoreCase("Ok")){
						//If request status is Ok get folders info from storage 
						folders = listResponse.getResult().getFolders();
						//Get folder id by folder name
						for(int col = 0; col < folders.size(); col++){
							//Check if folder name equal to entered folder name
							if(path.equals(folders.get(col).getName())){
								//Get folder id
								folderId = folders.get(col).getId().toString();
							}
						}
					}
					//###Make request to Doc Api to get folder sharers
					SharedUsersResponse shares = metadata.GetFolderSharers(credentials.getClient_id(), folderId);
					//Check request status
					if(shares != null && shares.getStatus().trim().equalsIgnoreCase("Ok")){
						//If status Ok get shared users
						sharer = shares.getResult().getShared_users();
					}
					//If request was successful - set moveResult variable for template
					status = ok(views.html.sample14.render(title, sample, sharer, filledForm));
				//###Definition of Api errors and conclusion of the corresponding message
				} catch (ApiException e) {
					if(e.getCode() == 401){
						List<Object> args = Arrays.asList(new Object[]{"https://apps.groupdocs.com/My/Manage", "Production Server"});
						filledForm.reject("Wrong Credentials. Please make sure to use credentials from", args);
					} else {
						filledForm.reject("Failed to access API: " + e.getMessage());
					}
					status = badRequest(views.html.sample14.render(title, sample, sharer, filledForm));
			    //###Definition of filledForm errors and conclusion of the corresponding message	
				} catch (Exception e) {
					if(path == null){
						filledForm.reject("srcPath", "This field is required");
					} else {
						filledForm.reject("srcPath", "Something wrong with your folder: " + e.getMessage());
					}
					status = badRequest(views.html.sample14.render(title, sample, sharer, filledForm));
				}
			}
		} else {
			filledForm = form.bind(session());
			session().put("server_type", "https://api.groupdocs.com/v2.0");
			status = ok(views.html.sample14.render(title, sample, sharer, filledForm));
		}
		//Process template
		return status;
	}
	
}