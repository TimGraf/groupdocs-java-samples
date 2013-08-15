//###<i>This sample will show how to use <b>SetAnnotationCollaborators</b> method from Annotation Api to set collaborator for document</i>
package controllers;
//Import of necessary libraries
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import com.groupdocs.sdk.api.StorageApi;
import com.groupdocs.sdk.common.FileStream;
import com.groupdocs.sdk.model.UploadResponse;
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
import com.groupdocs.sdk.model.SetCollaboratorsResponse;
import com.groupdocs.sdk.model.SetCollaboratorsResult;
import com.groupdocs.sdk.api.AntApi;

public class Sample13 extends Controller {
	//###Set variables
	static String title = "GroupDocs Java SDK Samples";
	static Form<Credentials> form = form(Credentials.class);
	
	public static Result index() {
		
		SetCollaboratorsResult result = null;
		Form<Credentials> filledForm;
		String sample = "Sample13";
        String guid = null;
        String email = null;
        String uploadDir = "";
		Status status;
		//Check POST parameters
		if(request().method().equalsIgnoreCase("POST")){
			filledForm = form.bindFromRequest();
			if(filledForm.hasErrors()){
				//If filledForm have errors return to template
				status = badRequest(views.html.sample13.render(title, sample, result, filledForm));
			} else {
				//If filledForm have no errors get all parameters
				Credentials credentials = filledForm.get();
				session().put("client_id", credentials.getClient_id());
				session().put("private_key", credentials.getPrivate_key());
				session().put("server_type", credentials.getServer_type());

//				Map<String, String[]> formData = request().body().asFormUrlEncoded();
//				String fileId = Utils.getFormValue(formData, "fileId");
//				String email = formData.get("email") != null ? formData.get("email")[0] : null;
//				email = StringUtils.isBlank(email) ? null : email.trim();

				try {
					//### Check client_id, private_key, fileId and email
					if(credentials.getClient_id() == null || credentials.getPrivate_key() == null){
						throw new Exception();
					}
					//###Create ApiInvoker, AntApi objects

		            //Create ApiInvoker object
					ApiInvoker.getInstance().setRequestSigner(
							new GroupDocsRequestSigner(credentials.getPrivate_key()));
                    StorageApi api = new StorageApi();
                    api.setBasePath(credentials.getServer_type());
                    Http.MultipartFormData multipartFormData = request().body().asMultipartFormData();
                    Map<String, String[]> formData = multipartFormData.asFormUrlEncoded();
                    String sourse = Utils.getFormValue(formData, "sourse");
                    email = Utils.getFormValue(formData, "email");
                    guid = Utils.getFormValue(formData, "fileId");
                    if ("local".equals(sourse)){
                        Http.MultipartFormData.FilePart filePart = multipartFormData.getFile("local");
                        FileInputStream is = new FileInputStream(filePart.getFile());
                        UploadResponse response = api.Upload(credentials.getClient_id(), uploadDir + filePart.getFilename(), "comment", "", new FileStream(is));

                        if (response != null && "Ok".equalsIgnoreCase(response.getStatus())) {
                            guid = response.getResult().getGuid();
                        }
                        else {
                            throw new Exception(response.getError_message());
                        }
                    }
                    else if ("url".equals(sourse)){
                        String url = Utils.getFormValue(formData, "url");
                        UploadResponse response = api.UploadWeb(credentials.getClient_id(), url);

                        if (response != null && "Ok".equalsIgnoreCase(response.getStatus())) {
                            guid = response.getResult().getGuid();
                        }
                        else {
                            throw new Exception(response.getError_message());
                        }
                    }
                    if (StringUtils.isEmpty(guid) || StringUtils.isEmpty(email)){
                        throw new Exception();
                    }





					//Create AntApi object
					AntApi ant = new AntApi();
					ant.setBasePath(credentials.getServer_type());
					//Create List object
					List<String> emailList = new ArrayList<String>();
					//Add email to the list
					emailList.add(email);
					//###Make request to Annotation api for setting collaborator for document  
					SetCollaboratorsResponse response = ant.SetAnnotationCollaborators(credentials.getClient_id(), guid, "v2.0", emailList);
					//Check request result
					if(response != null && response.getStatus().trim().equalsIgnoreCase("Ok")){
						//If request status Ok get results
						result = response.getResult();
						
					} else {
						//If status error throw exception massage
						throw new Exception("User identified by " + " not Found");
					}
					//If request was successful - set result variable for template
					status = ok(views.html.sample13.render(title, sample, result, filledForm));
				//###Definition of Api errors and conclusion of the corresponding message
				} catch (ApiException e) {
					if(e.getCode() == 401){
						List<Object> args = Arrays.asList(new Object[]{"https://apps.groupdocs.com/My/Manage", "Production Server"});
						filledForm.reject("Wrong Credentials. Please make sure to use credentials from", args);
					} else {
						filledForm.reject("Failed to access API: " + e.getMessage());
					}
					status = badRequest(views.html.sample13.render(title, sample, result, filledForm));
				//###Definition of filledForm errors and conclusion of the corresponding message
				} catch (Exception e) {
					e.printStackTrace();
					if(guid == null){
						filledForm.reject("fileId", "This field is required");
					} else if(email == null){
						filledForm.reject("email", "This field is required");
					} else {
						filledForm.reject("fileId", "Something wrong with your file: " + e.getMessage());
					}
					status = badRequest(views.html.sample13.render(title, sample, result, filledForm));
				} 
			}
		} else {
			filledForm = form.bind(session());
			session().put("server_type", "https://api.groupdocs.com/v2.0");
			status = ok(views.html.sample13.render(title, sample, result, filledForm));
		}
		//Process template
		return status;
	}
	
}