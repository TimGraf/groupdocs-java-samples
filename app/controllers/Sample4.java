package controllers;

import java.util.List;
import java.util.Map;

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
				session().put("clientId", credentials.clientId);
				session().put("privateKey", credentials.privateKey);
				
				Map<String, String[]> formData = request().body().asFormUrlEncoded();
				String fileGuid = formData.get("fileGuid") != null ? formData.get("fileGuid")[0] : null;
				fileGuid = StringUtils.isBlank(fileGuid) ? null : fileGuid.trim();
				
				try {
					if(fileGuid == null){
						throw new Exception();
					}
					ApiInvoker.getInstance().setRequestSigner(
							new GroupDocsRequestSigner(credentials.privateKey));
					StorageApi api = new StorageApi();
					FileStream response = api.GetFile(credentials.clientId, fileGuid);
					if(response != null && response.getInputStream() != null){
						file = response;
					} else {
						throw new Exception("Not Found");
					}
					if(file.getFileName() == null){
						file.setFileName(fileGuid);
					}
					response().setHeader("Content-Disposition", ContentDisposition.type("attachment").fileName(file.getFileName()).build().toString());
//					IOUtils.copy(file.getInputStream(), new FileOutputStream(file.getFileName()));
					status = ok(file.getInputStream());
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
					if(fileGuid == null){
						filledForm.reject("fileGuid", "This field is required");
					} else {
						filledForm.reject("fileGuid", "Something wrong with your file: " + e.getMessage());
					}
					status = badRequest(views.html.sample4.render(title, sample, file, filledForm));
				} finally {
					if(file != null){
						IOUtils.closeQuietly(file.getInputStream());
					}
				}
			}
		} else {
			filledForm = form.bind(session());
			status = ok(views.html.sample4.render(title, sample, file, filledForm));
		}
		return status;
	}
	
}