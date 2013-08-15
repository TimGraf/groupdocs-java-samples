//###<i>This sample will show how to use <b>ShareDocument</b> tmethod from Doc Api to share a document to other users</i>
package controllers;
//Import of necessary libraries
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

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

import com.groupdocs.sdk.api.DocApi;
import com.groupdocs.sdk.common.ApiException;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.GetDocumentInfoResponse;
import com.groupdocs.sdk.model.SharedUsersResponse;
import com.groupdocs.sdk.model.SharedUsersResult;

public class Sample10 extends Controller {
	//###Set variables
	static String title = "GroupDocs Java SDK Samples";
	static Form<Credentials> form = form(Credentials.class);
	
	public static Result index() {
		
		SharedUsersResult result = null;
		Form<Credentials> filledForm;
		String sample = "Sample10";
		Status status;
		//Check POST parameters
		if(request().method().equalsIgnoreCase("POST")){
			filledForm = form.bindFromRequest();
			if(filledForm.hasErrors()){
				//If filledForm have errors return to template
				status = badRequest(views.html.sample10.render(title, sample, result, filledForm));
			} else {
				//If filledForm have no errors get all parameters
				Credentials credentials = filledForm.get();
				session().put("client_id", credentials.getClient_id());
				session().put("private_key", credentials.getPrivate_key());
				session().put("server_type", credentials.getServer_type());
                String fileId = null;
                String email = null;

                try {
                    /////////////////////////////////////// -- //////////////////////////////////////
                    Http.MultipartFormData formData = request().body().asMultipartFormData();
                    Map<String, String[]> fieldsData = formData.asFormUrlEncoded();

                    String sourse = Utils.getFormValue(fieldsData, "sourse");
                    if ("guid".equals(sourse)) { // File GUID
                        fileId = Utils.getFormValue(fieldsData, "fileId");
                    }
                    else if ("url".equals(sourse)) { // Upload file fron URL
                        String fileUrl = Utils.getFormValue(fieldsData, "fileUrl");
                        ApiInvoker.getInstance().setRequestSigner(
                                new GroupDocsRequestSigner(credentials.getPrivate_key()));
                        StorageApi storageApi = new StorageApi();
                        storageApi.setBasePath(credentials.getServer_type());
                        UploadResponse response = storageApi.UploadWeb(credentials.getClient_id(), fileUrl);
                        if(response != null && response.getStatus().trim().equalsIgnoreCase("Ok")){
                            fileId = response.getResult().getGuid();
                        }
                    }
                    else if ("local".equals(sourse)) { // Upload local file
                        Http.MultipartFormData.FilePart filePart = formData.getFile("filePart");
                        ApiInvoker.getInstance().setRequestSigner(
                                new GroupDocsRequestSigner(credentials.getPrivate_key()));
                        StorageApi storageApi = new StorageApi();
                        storageApi.setBasePath(credentials.getServer_type());
                        FileInputStream is = new FileInputStream(filePart.getFile());
                        UploadResponse response = storageApi.Upload(credentials.getClient_id(), filePart.getFilename(), "uploaded", "", new FileStream(is));
                        if(response != null && response.getStatus().trim().equalsIgnoreCase("Ok")){
                            fileId = response.getResult().getGuid();
                        }
                    }
                    /////////////////////////////////////// -- //////////////////////////////////////
                    // Sample:

                    email = Utils.getFormValue(fieldsData, "email");
				
					//### Check fileId amd email
					if(fileId == null || email == null){
						throw new Exception();
					}
					//###Create ApiInvoker, DocApi objects

		            //Create ApiInvoker object
					ApiInvoker.getInstance().setRequestSigner(
							new GroupDocsRequestSigner(credentials.getPrivate_key()));
					//Create DocApi obect
					DocApi api = new DocApi();
					api.setBasePath(credentials.getServer_type());
					//###Make a request to DocApi to get document metadeta for entered fileId
					GetDocumentInfoResponse metadata = new DocApi().GetDocumentMetadata(credentials.getClient_id(), fileId);
					String file_Id = null;
					//Check request result
					if(metadata != null && metadata.getStatus().trim().equalsIgnoreCase("Ok")){
						file_Id = metadata.getResult().getId().toString();
					} else {
						throw new Exception("Not Found");
					}
					//###Make a request to DocApi to share document
					SharedUsersResponse response = api.ShareDocument(credentials.getClient_id(), file_Id, Arrays.asList(new String[]{email}));
					//Check request result
					if(response != null && metadata.getStatus().trim().equalsIgnoreCase("Ok")){
						result = response.getResult();
					} else {
						throw new Exception("User identified by " + " not Found");
					}
					//If request was successfull - set result variable for template
					status = ok(views.html.sample10.render(title, sample, result, filledForm));
				//###Definition of Api errors and conclusion of the corresponding message
				} catch (ApiException e) {
					if(e.getCode() == 401){
						List<Object> args = Arrays.asList(new Object[]{"https://apps.groupdocs.com/My/Manage", "Production Server"});
						filledForm.reject("Wrong Credentials. Please make sure to use credentials from", args);
					} else {
						filledForm.reject("Failed to access API: " + e.getMessage());
					}
					status = badRequest(views.html.sample10.render(title, sample, result, filledForm));
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
					status = badRequest(views.html.sample10.render(title, sample, result, filledForm));
				} 
			}
		} else {
			filledForm = form.bind(session());
			session().put("server_type", "https://api.groupdocs.com/v2.0");
			status = ok(views.html.sample10.render(title, sample, result, filledForm));
		}
		//Process template
		return status;
	}
	
}