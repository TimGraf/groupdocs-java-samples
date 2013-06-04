//###This sample will show how to use <b>ListAnnotations</b> method from Storage  API  to list annotations for document
package controllers;
//Import of necessary libraries
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

import com.groupdocs.sdk.api.StorageApi;
import com.groupdocs.sdk.common.FileStream;
import com.groupdocs.sdk.model.UploadResponse;
import common.Utils;
import org.apache.commons.lang3.StringUtils;

import models.Credentials;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
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
                String fileId = null;

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
                        storageApi.setBasePath(credentials.baseurl);
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
                        storageApi.setBasePath(credentials.baseurl);
                        FileInputStream is = new FileInputStream(filePart.getFile());
                        UploadResponse response = storageApi.Upload(credentials.client_id, filePart.getFilename(), "uploaded", "", new FileStream(is));
                        if(response != null && response.getStatus().trim().equalsIgnoreCase("Ok")){
                            fileId = response.getResult().getGuid();
                        }
                    }
                    /////////////////////////////////////// -- //////////////////////////////////////
                    // Sample:

                    //###Create ApiInvoker, Annotation API objects
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