//###<i>This sample will show how to use <b>ListEntities</b> method from Storage Api to create a list of thumbnails for a document</i>
package controllers;
//Import of necessary libraries
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.reflect.Array;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;


import models.Credentials;

import org.apache.commons.io.IOUtils;
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
import com.ning.http.util.Base64;

public class Sample07 extends Controller {
	//###Set variables
	static String title = "GroupDocs Java SDK Samples";
	static Form<Credentials> form = form(Credentials.class);
	
	public static Result index() {
		List<String> thumbnailUrls = new ArrayList<String>();
		String thumbnail = "";
		List<FileSystemDocument> documents = null;
		Form<Credentials> filledForm;
		String sample = "Sample7";
		Status status;
		//Check POST parameters
		if(request().method().equalsIgnoreCase("POST")){
			filledForm = form.bindFromRequest();
			if(filledForm.hasErrors()){
				status = badRequest(views.html.sample07.render(title, sample, thumbnail, filledForm));
			} else {
				//Get POST data
				Credentials credentials = filledForm.get();
				session().put("client_id", credentials.client_id);
				session().put("private_key", credentials.private_key);
				session().put("baseurl", credentials.baseurl);
				
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
					api.setBasePath(credentials.baseurl);
					//###Make request to Storage

		            //Geting all Entities with thumbnails from current user
					ListEntitiesResponse response = api.ListEntities(
							credentials.client_id, "", null, null, null, null,
							null, null, true);
					//Check request result
					if (response != null
							&& response.getStatus().trim()
									.equalsIgnoreCase("Ok")) {
						//Get all files
						documents = response.getResult().getFiles();
						//Get thumbnails for all files
						//Get separator symbol from system
						String separator = System.getProperty("file.separator");
						//Get dir where samples placed
						String path = System.getProperty("user.dir");
						for (int i = 0; i <= documents.size(); i++) {

							FileSystemDocument document = documents.get(i);
							//Check if file have thumbnail 
							BufferedImage image = null;
							if (document.getThumbnail() != null) {
								
				                //Create path for downloaded thumbnails
//				                String downloadPath = path + separator + separator + "public" + separator + "images" + separator;
//				                //Create image file 
//				                File destinationFile = new File(downloadPath + "thumbnail" + i + ".jpg");
//				                //Generate Output stream from created image file
//				                OutputStream out = new FileOutputStream(destinationFile);
//				                //Decode thumbnail string data to Base64 data and write to the image file
//				                out.write(Base64.decode(document.getThumbnail()));
//				                out.close();
				                //Generate HTML code for template
								thumbnail += "<img src= 'data:image/jpg;base64,"+document.getThumbnail()+"', width=40px, height=40px>" + document.getName().toString() + "</img><br>";
							
							}

						}
						//If request was successfull - set  thumbnailUrls variable for template
			                status = ok(views.html.sample07.render(title, sample, thumbnail, filledForm));
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
					status = badRequest(views.html.sample07.render(title, sample, thumbnail, filledForm));
				//###Definition of filledForm errors and conclusion of the corresponding message	
				} catch (Exception e) {
					e.printStackTrace();
					status = badRequest(views.html.sample07.render(title, sample, thumbnail, filledForm));
				}
			}
		} else {
			filledForm = form.bind(session());
			session().put("baseurl", "https://api.groupdocs.com/v2.0");
			status = ok(views.html.sample07.render(title, sample, thumbnail, filledForm));
		}
		//Process template
		return status;
	}

}