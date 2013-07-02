 //###<i>This sample will show how to use <b>GetFile</b> method from Storage Api to download a file from GroupDocs Storage</i>
package controllers;
//Import of necessary libraries
import java.util.List;
import java.util.Map;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.File;
import java.io.InputStream;


import com.groupdocs.sdk.api.DocApi;
import com.groupdocs.sdk.api.SharedApi;
import com.groupdocs.sdk.model.GetDocumentInfoResponse;
import com.groupdocs.sdk.model.GetDocumentInfoResult;
import com.groupdocs.sdk.model.UploadRequestResult;
import com.groupdocs.sdk.model.UploadResponse;
import common.Utils;
import models.Credentials;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import scala.actors.threadpool.Arrays;

import com.groupdocs.sdk.api.StorageApi;
import com.groupdocs.sdk.common.ApiException;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.FileStream;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.sun.jersey.core.header.ContentDisposition;

public class Sample04 extends Controller {
	//###Set variables
	static String title = "GroupDocs Java SDK Samples";
	static Form<Credentials> form = form(Credentials.class);
	
	public static Result index() {
		FileStream file = null;
		Form<Credentials> filledForm;
		String sample = "Sample4";
		Status status;
		//Check POST parameters
		if(request().method().equalsIgnoreCase("POST")){
			filledForm = form.bindFromRequest();
			if(filledForm.hasErrors()){
				status = badRequest(views.html.sample04.render(title, sample, file, filledForm));
			} else {
				//Get POST data
				Credentials credentials = filledForm.get();
				session().put("client_id", credentials.client_id);
				session().put("private_key", credentials.private_key);
				session().put("baseurl", credentials.baseurl);
                String file_id = null;

                try {
                    /////////////////////////////////////// -- //////////////////////////////////////
                    Http.MultipartFormData formData = request().body().asMultipartFormData();
                    Map<String, String[]> fieldsData = formData.asFormUrlEncoded();

                    String fileData = Utils.getFormValue(fieldsData, "fileData");
                    if ("IDfileId".equals(fileData)) { // File GUID
                        file_id = Utils.getFormValue(fieldsData, "fileId");
                    }
                    else if ("IDfileUrl".equals(fileData)) { // Upload file fron URL
                        String fileUrl = Utils.getFormValue(fieldsData, "fileUrl");
                        ApiInvoker.getInstance().setRequestSigner(
                                new GroupDocsRequestSigner(credentials.private_key));
                        StorageApi storageApi = new StorageApi();
                        storageApi.setBasePath(credentials.baseurl);
                        UploadResponse response = storageApi.UploadWeb(credentials.client_id, fileUrl);
                        if(response != null && response.getStatus().trim().equalsIgnoreCase("Ok")){
                            file_id = response.getResult().getGuid();
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
                            file_id = response.getResult().getGuid();
                        }
                    }
                    /////////////////////////////////////// -- //////////////////////////////////////
                    // Sample:

					//Check entered file id
					if(file_id == null){
						throw new Exception();
					}

                    //Create ApiInvoker object
                    ApiInvoker.getInstance().setRequestSigner(
                            new GroupDocsRequestSigner(credentials.private_key));

                    DocApi docApi = new DocApi();
                    docApi.setBasePath(credentials.baseurl);
                    GetDocumentInfoResponse docInfoResponse = docApi.GetDocumentMetadata(credentials.client_id, file_id);
                    GetDocumentInfoResult docInfoResult = docInfoResponse.getResult();
                    String fileName = docInfoResult.getLast_view().getDocument().getName();
					//###Create ApiInvoker, Storage Api objects
					
					//Create StorageApi object
                    SharedApi api = new SharedApi();
					api.setBasePath(credentials.baseurl);
					//###Make a request to Storage API using clientId
					
					//Get file from storage
					FileStream resp = api.Download(file_id, fileName, false);
					//Check request result
					if(resp != null && resp.getInputStream() != null){
						file = resp;
					} else {
						throw new Exception("Not Found");
					}
					//Check file name
                    System.out.println("file.getFileName() = " + file.getFileName());
                    System.out.println("fileName = " + fileName);
					if(file.getFileName() == null){
						file.setFileName(fileName);
					}
					//###Obtaining file stream of downloading file and definition of folder where to download file
					String separator = System.getProperty("file.separator");
	                String path = new File(".").getAbsolutePath();
	                String downloadPath = path + separator + "public" + separator + "images" + separator;
	                FileOutputStream newFile = new FileOutputStream(downloadPath + file.getFileName());
	                //Write file to local folder
	                IOUtils.copy(file.getInputStream(), newFile);
	                IOUtils.closeQuietly(file.getInputStream());
	                //If request was successfull - set file variable for template
	                status = ok(views.html.sample04.render(title, sample, file, filledForm));
	            //###Definition of Api errors and conclusion of the corresponding message
				} catch (ApiException e) {
					if(e.getCode() == 401){
						List<Object> args = Arrays.asList(new Object[]{"https://apps.groupdocs.com/My/Manage", "Production Server"});
						filledForm.reject("Wrong Credentials. Please make sure to use credentials from", args);
					} else {
						filledForm.reject("Failed to access API: " + e.getMessage());
					}
					status = badRequest(views.html.sample04.render(title, sample, file, filledForm));
				//###Definition of filledForm errors and conclusion of the corresponding message
				} catch (Exception e) {
					e.printStackTrace();
					if(file_id == null){
						filledForm.reject("file_id", "This field is required");
					} else {
						filledForm.reject("file_id", "Something wrong with your file: " + e.getMessage());
					}
					status = badRequest(views.html.sample04.render(title, sample, file, filledForm));
				} 

			}
		} else {
			filledForm = form.bind(session());
			session().put("baseurl", "https://api.groupdocs.com/v2.0");
			status = ok(views.html.sample04.render(title, sample, file, filledForm));
		}
		//Process template
		return status;
	}
}