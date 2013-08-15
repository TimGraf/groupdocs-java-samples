//###<i>This sample will show how to use <b>MoveFile</b> method from Storage Api to copy/move a file in GroupDocs Storage </i>
package controllers;
//Import of necessary libraries
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.FileStream;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.*;
import common.Utils;
import models.Credentials;

import org.apache.commons.lang3.StringUtils;

import play.data.Form;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import scala.actors.threadpool.Arrays;

import com.groupdocs.sdk.api.StorageApi;
import com.groupdocs.sdk.common.ApiException;

public class Sample05 extends Controller {
	//###Set variables
	static String title = "GroupDocs Java SDK Samples";
	static Form<Credentials> form = form(Credentials.class);
	
	public static Result index() {
		FileMoveResult moveResult = null;
		Form<Credentials> filledForm;
		String sample = "Sample5";
        String uploadDir = "";
        String action = "";
		Status status;
		//Check POST parameters
		if(request().method().equalsIgnoreCase("POST")){
			filledForm = form.bindFromRequest();
			if(filledForm.hasErrors()){
				status = badRequest(views.html.sample05.render(title, sample, moveResult, filledForm, action));
			} else {
				//Get POST data
				Credentials credentials = filledForm.get();
				session().put("client_id", credentials.getClient_id());
				session().put("private_key", credentials.getPrivate_key());
				session().put("server_type", credentials.getServer_type());
                try {
                    String guid = null;
                    String fileName = null;
                    Double fileId = 0d;
                    ApiInvoker.getInstance().setRequestSigner(
                            new GroupDocsRequestSigner(credentials.getPrivate_key()));
                    StorageApi api = new StorageApi();
                    api.setBasePath(credentials.getServer_type());
                    Http.MultipartFormData multipartFormData = request().body().asMultipartFormData();
                    Map<String, String[]> formData = multipartFormData.asFormUrlEncoded();
                    String sourse = Utils.getFormValue(formData, "sourse");
                    guid = Utils.getFormValue(formData, "srcPath");
                    if ("local".equals(sourse)){
                        Http.MultipartFormData.FilePart filePart = multipartFormData.getFile("file");
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
                            fileName = url.split("/")[url.split("/").length - 1];
                            fileId = response.getResult().getId();
                        }
                        else {
                            throw new Exception(response.getError_message());
                        }
                    }
                    String destPath = Utils.getFormValue(formData, "destPath");
                    if (StringUtils.isEmpty(guid) || destPath == null){
                        throw new Exception();
                    }

                    ListEntitiesResponse response =  api.ListEntities(credentials.getClient_id(), uploadDir, 0, null, null, null, null, null, null);
                    for (FileSystemDocument document : response.getResult().getFiles()){
                        if (guid.equals(document.getGuid())){
                            fileName = document.getName();
                            fileId = document.getId();
                            break;
                        }
                    }
                    if (fileName == null || fileId == null){
                        throw new Exception();
                    }
                    String copyToPath = (destPath + "/" + fileName).replaceAll("//", "/");
                    FileMoveResponse moveResponse = null;
                    if (Utils.getFormValue(formData, "copy") != null){
                        moveResponse = api.MoveFile(credentials.getClient_id(), copyToPath, null, Double.toString(fileId), null);
                        action = "copy";
                    }
                    else if (Utils.getFormValue(formData, "move") != null){
                        moveResponse = api.MoveFile(credentials.getClient_id(), copyToPath, null, null, Double.toString(fileId));
                        action = "move";
                    }
                    status = ok(views.html.sample05.render(title, sample, moveResponse.getResult(), filledForm, action));
				//###Definition of Api errors and conclusion of the corresponding message
				} catch (ApiException e) {
					if(e.getCode() == 401){
						List<Object> args = Arrays.asList(new Object[]{"https://apps.groupdocs.com/My/Manage", "Production Server"});
						filledForm.reject("Wrong Credentials. Please make sure to use credentials from", args);
					} else {
						filledForm.reject("Failed to access API: " + e.getMessage());
					}
					status = badRequest(views.html.sample05.render(title, sample, moveResult, filledForm, action));
			    //###Definition of filledForm errors and conclusion of the corresponding message	
				} catch (Exception e) {
                    filledForm.reject("srcPath", "Something wrong: " + e.getMessage());
					status = badRequest(views.html.sample05.render(title, sample, moveResult, filledForm, action));
				}
			}
		} else {
			filledForm = form.bind(session());
			session().put("server_type", "https://api.groupdocs.com/v2.0");
			status = ok(views.html.sample05.render(title, sample, moveResult, filledForm, action));
		}
		//Process template
		return status;
	}
	
}