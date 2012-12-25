//###<i>This sample will show how to use <b>ListEntities</b> method from Storage Api to create a list of thumbnails for a document</i>
package controllers;
//Import of necessary libraries
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.reflect.Array;
import java.io.File;
import java.io.FileWriter;
import java.io.FileOutputStream;


import models.Credentials;

import org.apache.commons.lang3.StringUtils;



import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import scala.actors.threadpool.Arrays;
import scala.actors.remote.*;

import com.groupdocs.sdk.api.DocApi;
import com.groupdocs.sdk.api.StorageApi;
import com.groupdocs.sdk.common.ApiException;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.GetDocumentInfoResponse;
import com.groupdocs.sdk.model.GetImageUrlsResponse;
import com.groupdocs.sdk.model.ListEntitiesResponse;
import com.groupdocs.sdk.model.FileSystemDocument;
import com.groupdocs.sdk.model.UserInfo;
import com.groupdocs.sdk.model.UserInfoResponse;
import com.groupdocs.sdk.model.GetDocumentInfoResponse;
import com.groupdocs.sdk.model.GetDocumentInfoResult;

public class Sample7 extends Controller {
	//###Set variables
	static String title = "GroupDocs Java SDK Samples";
	static Form<Credentials> form = form(Credentials.class);
	
	public static Result index() {
		List<String> thumbnailUrls = new ArrayList<String>();
		List<FileSystemDocument> documents = null;
		Form<Credentials> filledForm;
		String sample = "Sample7";
		Status status;
		//Check POST parameters
		if(request().method().equalsIgnoreCase("POST")){
			filledForm = form.bindFromRequest();
			if(filledForm.hasErrors()){
				status = badRequest(views.html.sample7.render(title, sample, thumbnailUrls, filledForm));
			} else {
				//Get POST data
				Credentials credentials = filledForm.get();
				session().put("client_id", credentials.client_id);
				session().put("private_key", credentials.private_key);
				
				Map<String, String[]> formData = request().body().asFormUrlEncoded();

				try {
					//Check clien_if and private_key
					if (credentials.private_key == null
							|| credentials.client_id == null) {
						throw new Exception();
					}
					//###Create ApiInvoker, Storage Api objects

		            //Create ApiInvoker object
					ApiInvoker.getInstance()
							.setRequestSigner(
									new GroupDocsRequestSigner(
											credentials.private_key));
					//Create Storage Api object
					StorageApi api = new StorageApi();
					//###Make request to Storage

		            //Geting all Entities with thumbnails from current user
					ListEntitiesResponse response = api.ListEntities(
							credentials.client_id, "", 0, null, null, null,
							null, null, true);
					//Check request result
					if (response != null
							&& response.getStatus().trim()
									.equalsIgnoreCase("Ok")) {
						//Get all files
						documents = response.getResult().getFiles();
						//Get thumbnails for all files
						for (int i = 0; i <= documents.size(); i++) {

							FileSystemDocument document = documents.get(i);
							//Check if file have thumbnail 
							if (document.getThumbnail() != null) {
								//Create Doc api object
								DocApi Docapi = new DocApi();
								//Get document metadata
								GetDocumentInfoResponse info = Docapi
										.GetDocumentMetadata(
												credentials.client_id,
												document.getGuid());
								Integer pageCount = null;
								//Check file info
								if (info != null
										&& info.getStatus().trim()
												.equalsIgnoreCase("Ok")) {
									//Get number of pages
									pageCount = info.getResult()
											.getPage_count();
								} else {
									throw new Exception("Not Found");
								}
								String dimention = "65x65";
								//###Make request to DocApi
								
								//Get list of URLs for document pages
								List<String> temp = Docapi
										.GetDocumentPagesImageUrls(
												credentials.client_id,
												document.getGuid(), 0,
												pageCount, dimention, null,
												null, null).getResult()
										.getUrl();
								//Add all thumbnails to URLs list
								thumbnailUrls.addAll(temp);

							}

						}
						//If request was successfull - set  thumbnailUrls variable for template
			                status = ok(views.html.sample7.render(title, sample, thumbnailUrls, filledForm));
			            } else {
			                throw new Exception("Result error!");
			            }
				//###Definition of Api errors and conclusion of the corresponding message
				} catch (ApiException e) {
					if(e.getCode() == 401){
						List<Object> args = Arrays.asList(new Object[]{"https://apps.groupdocs.com/My/Manage", "Production Server"});
						filledForm.reject("Wrong Credentials. Please make sure to use credentials from", args);
					} else {
						filledForm.reject("Failed to access API: " + e.getMessage());
					}
					status = badRequest(views.html.sample7.render(title, sample, thumbnailUrls, filledForm));
				//###Definition of filledForm errors and conclusion of the corresponding message	
				} catch (Exception e) {
					e.printStackTrace();
					status = badRequest(views.html.sample7.render(title, sample, thumbnailUrls, filledForm));
				}
			}
		} else {
			filledForm = form.bind(session());
			status = ok(views.html.sample7.render(title, sample, thumbnailUrls, filledForm));
		}
		//Process template
		return status;
	}

}