//###<i>This sample will show how to use <b>SetAnnotationCollaborators</b> method from Annotation Api to set collaborator for document</i>
package controllers;
//Import of necessary libraries
import java.io.FileInputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import com.groupdocs.sdk.api.StorageApi;
import com.groupdocs.sdk.common.FileStream;
import com.groupdocs.sdk.model.*;
import common.Utils;
import models.Credentials;

import org.apache.commons.lang3.StringUtils;

import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import scala.actors.threadpool.Arrays;

import com.groupdocs.sdk.common.ApiException;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.api.AntApi;
import com.groupdocs.sdk.api.MgmtApi;

public class Sample22 extends Controller {
	//###Set variables
	static String title = "GroupDocs Java SDK Samples";
	static Form<Credentials> form = form(Credentials.class);
	
	public static Result index() {
		
		String result = null;
		Form<Credentials> filledForm;
		String sample = "Sample22";
		Status status;
		//Check POST parameters
		if(request().method().equalsIgnoreCase("POST")){
			filledForm = form.bindFromRequest();
			if(filledForm.hasErrors()){
				//If filledForm have errors return to template
				status = badRequest(views.html.sample22.render(title, sample, result, filledForm));
			} else {
				//If filledForm have no errors get all parameters
				Credentials credentials = filledForm.get();
				session().put("client_id", credentials.client_id);
				session().put("private_key", credentials.private_key);
				session().put("server_type", credentials.server_type);
                String fileId = null;
                String email = null;

                try {
                    /////////////////////////////////////// -- //////////////////////////////////////
                    Http.MultipartFormData formData = request().body().asMultipartFormData();
                    Map<String, String[]> fieldsData = formData.asFormUrlEncoded();

                    String fileData = Utils.getFormValue(fieldsData, "fileData");
                    if ("IDfileId".equals(fileData)) { // File GUID
                        fileId = Utils.getFormValue(fieldsData, "fileId");
                    }
                    else if ("IDfileUrl".equals(fileData)) { // Upload file fron URL
                        String fileUrl = Utils.getFormValue(fieldsData, "fileUrl");
                        ApiInvoker.getInstance().setRequestSigner(
                                new GroupDocsRequestSigner(credentials.private_key));
                        StorageApi storageApi = new StorageApi();
                        storageApi.setBasePath(credentials.server_type);
                        UploadResponse response = storageApi.UploadWeb(credentials.client_id, fileUrl);
                        if(response != null && response.getStatus().trim().equalsIgnoreCase("Ok")){
                            fileId = response.getResult().getGuid();
                        }
                    }
                    else if ("IDfilePart".equals(fileData)) { // Upload local file
                        Http.MultipartFormData.FilePart filePart = formData.getFile("filePart");
                        ApiInvoker.getInstance().setRequestSigner(
                                new GroupDocsRequestSigner(credentials.private_key));
                        StorageApi storageApi = new StorageApi();
                        storageApi.setBasePath(credentials.server_type);
                        FileInputStream is = new FileInputStream(filePart.getFile());
                        UploadResponse response = storageApi.Upload(credentials.client_id, filePart.getFilename(), "uploaded", null, new FileStream(is));
                        if(response != null && response.getStatus().trim().equalsIgnoreCase("Ok")){
                            fileId = response.getResult().getGuid();
                        }
                    }
                    /////////////////////////////////////// -- //////////////////////////////////////
                    // Sample:

                    email = Utils.getFormValue(fieldsData, "email");
                    String first_name = Utils.getFormValue(fieldsData, "first_name");
                    String last_name = Utils.getFormValue(fieldsData, "last_name");
                    String callback = Utils.getFormValue(fieldsData, "callbackUrl");
                    String basePath = credentials.server_type;

					//### Check is all parameters entered
					if(credentials.client_id == null || credentials.private_key == null || fileId == null || email == null || first_name == null || last_name == null){
						throw new Exception();
					}
					//###Create ApiInvoker, AntApi objects

		            //Create ApiInvoker object
					ApiInvoker.getInstance().setRequestSigner(
								new GroupDocsRequestSigner(credentials.private_key));
					//Create AntApi object
					MgmtApi api = new MgmtApi();
					//Set selected server
					api.setBasePath(basePath);
					//Create User info object
					UserInfo user = new UserInfo();
					 //Create Role info object
					RoleInfo role = new RoleInfo();
					//Create array of roles.
					List<RoleInfo> roleList = new ArrayList<RoleInfo>();
					//Set user role Id. Can be: 1 -  SysAdmin, 2 - Admin, 3 - User, 4 - Guest
					role.setId(3.0);
					//Set user role name. Can be: SysAdmin, Admin, User, Guest
					role.setName("User");
					//Add RoleInfo object to roles array
					roleList.add(role);
					user.setNickname(first_name);
		            //Set first name as entered first name
		            user.setFirstname(first_name);
		            //Set last name as entered last name
		            user.setLastname(last_name);
		            user.setRoles(roleList);
		            //Set email as entered email
		            user.setPrimary_email(email);
		            //Creating of new user. $clientId - user id, $firstName - entered first name, $user - object with new user info
		            UpdateAccountUserResponse newUser = api.UpdateAccountUser(credentials.client_id, email, user);
		            
		            if (newUser.getStatus().equals("Ok")) {
		            	//Create AntApi object
						AntApi ant = new AntApi();
						//Set selected server
						ant.setBasePath(basePath);
		            	//Create List object
						List<String> emailList = new ArrayList<String>();
						//Add email to the list
						emailList.add(email);
						//###Make request to Annotation api for setting collaborator for document  
						SetCollaboratorsResponse response = ant.SetAnnotationCollaborators(credentials.client_id, fileId, "v2.0", emailList);
						//Make request to Annotation api to receive all collaborators for entered file id
						GetCollaboratorsResponse getCollaborators = ant.GetAnnotationCollaborators(credentials.client_id, fileId);
						//Set reviewers rights for new user. $newUser->result->guid - GuId of created user, $fileId - entered file id, 
		                //$getCollaborators->result->collaborators - array of collabotors in which new user will be added
						SetReviewerRightsResponse setReviewer = ant.SetReviewerRights(newUser.getResult().getGuid(), fileId, getCollaborators.getResult().getCollaborators());
						//Check is callback entered
						if (callback == null) {
							callback = "";
						}
						//Set callback url. CallBack work results you can see here: http://groupdocs-php-samples.herokuapp.com/callbacks/annotation_check_file
		                SetSessionCallbackUrlResponse setCallBack = ant.SetSessionCallbackUrl(newUser.getResult().getGuid(), fileId, callback);
		                //###Generation of iframe URL using $pageImage->result->guid
		                
		                //iframe to prodaction server
		                if (basePath.equals("https://api.groupdocs.com/v2.0")) {
		                    result = "https://apps.groupdocs.com//document-annotation2/embed/" + fileId + "?&uid=" + newUser.getResult().getGuid() + "&download=true frameborder=0 width=720 height=600";
		                //iframe to dev server
		                } else if(basePath.equals("https://dev-api.groupdocs.com/v2.0")) {
		                    result = "https://dev-apps.groupdocs.com//document-annotation2/embed/" + fileId + "?&uid=" + newUser.getResult().getGuid() + "&download=true frameborder=0 width=720 height=600";
		                //iframe to test server
		                } else if(basePath.equals("https://stage-api.groupdocs.com/v2.0")) {
		                    result = "https://stage-apps.groupdocs.com//document-annotation2/embed/" + fileId + "?&uid=" + newUser.getResult().getGuid() + "&download=true frameborder=0 width=720 height=600";;
		                }
		               
		            }
					//If request was successful - set result variable for template
					status = ok(views.html.sample22.render(title, sample, result, filledForm));
				//###Definition of Api errors and conclusion of the corresponding message
				} catch (ApiException e) {
					if(e.getCode() == 401){
						List<Object> args = Arrays.asList(new Object[]{"https://apps.groupdocs.com/My/Manage", "Production Server"});
						filledForm.reject("Wrong Credentials. Please make sure to use credentials from", args);
					} else {
						filledForm.reject("Failed to access API: " + e.getMessage());
					}
					status = badRequest(views.html.sample22.render(title, sample, result, filledForm));
				//###Definition of filledForm errors and conclusion of the corresponding message
				} catch (Exception e) {
					e.printStackTrace();
					if(fileId == null){
						filledForm.reject("fileId", "This field is required");
					} else if(email == null){
						filledForm.reject("email", "This field is required");
					} else {
						filledForm.reject("fileId", "Something wrong with your file: " + e.getMessage());
					}
					status = badRequest(views.html.sample22.render(title, sample, result, filledForm));
				} 
			}
		} else {
			filledForm = form.bind(session());
			session().put("server_type", "https://api.groupdocs.com/v2.0");
			status = ok(views.html.sample22.render(title, sample, result, filledForm));
		}
		//Process template
		return status;
	}
	
}