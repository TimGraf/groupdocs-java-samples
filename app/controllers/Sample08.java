//###<i>This sample will show how to use <b>GetDocumentPagesImageUrls</b> method from Doc Api to return a URL representing a single page of a Document</i>
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

public class Sample08 extends Controller {
	//###Set variables
	static String title = "GroupDocs Java SDK Samples";
	static Form<Credentials> form = form(Credentials.class);
	
	public static Result index() {
		List<String> thumbnailUrls = null;
		Form<Credentials> filledForm;
		String sample = "Sample8";
		Status status;
		//Check POST parameters
		if(request().method().equalsIgnoreCase("POST")){
			filledForm = form.bindFromRequest();
			if(filledForm.hasErrors()){
				status = badRequest(views.html.sample08.render(title, sample, thumbnailUrls, filledForm));
			} else {
				//Get POST data
				Credentials credentials = filledForm.get();
				session().put("client_id", credentials.client_id);
				session().put("private_key", credentials.private_key);
				session().put("server_type", credentials.server_type);
                String fileGuid = null;
                String pageNumber = null;

                try {
                    /////////////////////////////////////// -- //////////////////////////////////////
                    Http.MultipartFormData formData = request().body().asMultipartFormData();
                    Map<String, String[]> fieldsData = formData.asFormUrlEncoded();

                    String sourse = Utils.getFormValue(fieldsData, "sourse");
                    if ("guid".equals(sourse)) { // File GUID
                        fileGuid = Utils.getFormValue(fieldsData, "fileId");
                    }
                    else if ("url".equals(sourse)) { // Upload file fron URL
                        String url = Utils.getFormValue(fieldsData, "url");
                        ApiInvoker.getInstance().setRequestSigner(
                                new GroupDocsRequestSigner(credentials.private_key));
                        StorageApi storageApi = new StorageApi();
                        storageApi.setBasePath(credentials.server_type);
                        UploadResponse response = storageApi.UploadWeb(credentials.client_id, url);
                        if(response != null && response.getStatus().trim().equalsIgnoreCase("Ok")){
                            fileGuid = response.getResult().getGuid();
                        }
                    }
                    else if ("local".equals(sourse)) { // Upload local file
                        Http.MultipartFormData.FilePart file = formData.getFile("file");
                        ApiInvoker.getInstance().setRequestSigner(
                                new GroupDocsRequestSigner(credentials.private_key));
                        StorageApi storageApi = new StorageApi();
                        storageApi.setBasePath(credentials.server_type);
                        FileInputStream is = new FileInputStream(file.getFile());
                        UploadResponse response = storageApi.Upload(credentials.client_id, file.getFilename(), "uploaded", "", new FileStream(is));
                        if(response != null && response.getStatus().trim().equalsIgnoreCase("Ok")){
                            fileGuid = response.getResult().getGuid();
                        }
                    }
                    /////////////////////////////////////// -- //////////////////////////////////////
                    // Sample:

                    pageNumber = Utils.getFormValue(fieldsData, "pageNumber");
					//Check fileGuid
					if(fileGuid == null){
						throw new Exception();
					}
					 //###Create ApiInvoker, DocApi objects

		            //Create ApiInvoker object
					ApiInvoker.getInstance().setRequestSigner(
							new GroupDocsRequestSigner(credentials.private_key));
					//Create DocApi object
					DocApi api = new DocApi();
					api.setBasePath(credentials.server_type);
					//Get document metadata
					GetDocumentInfoResponse response = api.GetDocumentMetadata(credentials.client_id, fileGuid);
					//Convert pageNumber from String to Integer
					int page = Integer.parseInt( pageNumber );
					//###Make request to DocApi using client_id
		            
		            //Obtaining URl of entered page 
					thumbnailUrls = api.GetDocumentPagesImageUrls(credentials.client_id, fileGuid, page, 1, "150x150", null, null, null).getResult().getUrl();
					//If request was successfull - set  thumbnailUrls variable for template
					status = ok(views.html.sample08.render(title, sample, thumbnailUrls, filledForm));
				//###Definition of Api errors and conclusion of the corresponding message
				} catch (ApiException e) {
					if(e.getCode() == 401){
						List<Object> args = Arrays.asList(new Object[]{"https://apps.groupdocs.com/My/Manage", "Production Server"});
						filledForm.reject("Wrong Credentials. Please make sure to use credentials from", args);
					} else {
						filledForm.reject("Failed to access API: " + e.getMessage());
					}
					status = badRequest(views.html.sample08.render(title, sample, thumbnailUrls, filledForm));
				//###Definition of filledForm errors and conclusion of the corresponding message
				} catch (Exception e) {
					e.printStackTrace();
					if(fileGuid == null){
						filledForm.reject("fileGuid", "This field is required");
					} else if(pageNumber == null){
						filledForm.reject("pageNumber", "This field is required");
					} else {
						filledForm.reject("fileGuid", "Something wrong with your file: " + e.getMessage());
					}
					status = badRequest(views.html.sample08.render(title, sample, thumbnailUrls, filledForm));
				}
			}
		} else {
			filledForm = form.bind(session());
			session().put("server_type", "https://api.groupdocs.com/v2.0");
			status = ok(views.html.sample08.render(title, sample, thumbnailUrls, filledForm));
		}
		//Process template
		return status;
	}
	
}