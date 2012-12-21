package controllers;

import java.util.List;
import java.util.Map;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.File;
import java.io.InputStream;


import models.Credentials;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import scala.actors.threadpool.Arrays;

import com.groupdocs.sdk.api.StorageApi;
import com.groupdocs.sdk.common.ApiException;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.FileStream;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.sun.jersey.core.header.ContentDisposition;

public class Sample4 extends Controller {

	static String title = "GroupDocs Java SDK Samples";
	static Form<Credentials> form = form(Credentials.class);
	
	public static Result index() {
		FileStream file = null;
		Form<Credentials> filledForm;
		String sample = "Sample4";
		Status status;
		if(request().method().equalsIgnoreCase("POST")){
			filledForm = form.bindFromRequest();
			if(filledForm.hasErrors()){
				status = badRequest(views.html.sample4.render(title, sample, file, filledForm));
			} else {
				Credentials credentials = filledForm.get();
				session().put("client_id", credentials.client_id);
				session().put("private_key", credentials.private_key);
				
				Map<String, String[]> formData = request().body().asFormUrlEncoded();
				String file_id = formData.get("file_id") != null ? formData.get("file_id")[0] : null;
				file_id = StringUtils.isBlank(file_id) ? null : file_id.trim();
				
				try {
					if(file_id == null){
						throw new Exception();
					}
					ApiInvoker.getInstance().setRequestSigner(
							new GroupDocsRequestSigner(credentials.private_key));
					StorageApi api = new StorageApi();
					FileStream resp = api.GetFile(credentials.client_id, file_id);
					if(resp != null && resp.getInputStream() != null){
						file = resp;
					} else {
						throw new Exception("Not Found");
					}
					if(file.getFileName() == null){
						file.setFileName(file_id);
					}
			
					String separator = System.getProperty("file.separator");
	                String path = System.getProperty("user.dir");
	                String downloadPath = path + separator + "public" + separator + "images" + separator;
	                FileOutputStream newFile = new FileOutputStream(downloadPath + file.getFileName());
	                IOUtils.copy(file.getInputStream(), newFile);
	                IOUtils.closeQuietly(file.getInputStream());
	                status = ok(views.html.sample4.render(title, sample, file, filledForm));
				} catch (ApiException e) {
					if(e.getCode() == 401){
						List<Object> args = Arrays.asList(new Object[]{"https://apps.groupdocs.com/My/Manage", "Production Server"});
						filledForm.reject("Wrong Credentials. Please make sure to use credentials from", args);
					} else {
						filledForm.reject("Failed to access API: " + e.getMessage());
					}
					status = badRequest(views.html.sample4.render(title, sample, file, filledForm));
				} catch (Exception e) {
					e.printStackTrace();
					if(file_id == null){
						filledForm.reject("file_id", "This field is required");
					} else {
						filledForm.reject("file_id", "Something wrong with your file: " + e.getMessage());
					}
					status = badRequest(views.html.sample4.render(title, sample, file, filledForm));
				} 

			}
		} else {
			filledForm = form.bind(session());
			status = ok(views.html.sample4.render(title, sample, file, filledForm));
		}
		return status;
	}
	
}