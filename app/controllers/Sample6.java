package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import models.Credentials;

import org.apache.commons.lang3.StringUtils;

import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import scala.actors.threadpool.Arrays;

import com.groupdocs.sdk.api.SignatureApi;
import com.groupdocs.sdk.common.ApiException;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.SignatureSignDocumentDocumentSettings;
import com.groupdocs.sdk.model.SignatureSignDocumentResponse;
import com.groupdocs.sdk.model.SignatureSignDocumentSettings;
import com.groupdocs.sdk.model.SignatureSignDocumentSignerSettings;

public class Sample6 extends Controller {

	static String title = "GroupDocs Java SDK Samples";
	static Form<Credentials> form = form(Credentials.class);
	
	public static Result index() {
		String fileGuid = null;
		Form<Credentials> filledForm;
		String sample = "Sample6";
		Status status;
		if(request().method().equalsIgnoreCase("POST")){
			filledForm = form.bindFromRequest();
			if(filledForm.hasErrors()){
				status = badRequest(views.html.sample6.render(title, sample, fileGuid, filledForm));
			} else {
				Credentials credentials = filledForm.get();
				session().put("clientId", credentials.clientId);
				session().put("privateKey", credentials.privateKey);
				
				MultipartFormData body = request().body().asMultipartFormData();
				Map<String, String[]> formData = body.asFormUrlEncoded();
				String signerName = formData.get("signerName") != null ? formData.get("signerName")[0] : null;
				signerName = StringUtils.isBlank(signerName) ? null : signerName.trim();
		        FilePart resourceFile = body.getFile("resourceFile");
		        FilePart resourceSignature = body.getFile("resourceSignature");
		        
				try {
					if(signerName == null || resourceFile == null || resourceSignature == null){
						throw new Exception();
					}
					
					ApiInvoker.getInstance().setRequestSigner(
							new GroupDocsRequestSigner(credentials.privateKey));
					
					String base64file = ApiInvoker.readAsDataURL(resourceFile.getFile(), resourceFile.getContentType());
					String base64signature = ApiInvoker.readAsDataURL(resourceSignature.getFile(), resourceSignature.getContentType());
			  		
			  		SignatureSignDocumentDocumentSettings document = new SignatureSignDocumentDocumentSettings();
			  		document.setName(resourceFile.getFilename());
			  		document.setData(base64file);
					
					SignatureSignDocumentSignerSettings signer = new SignatureSignDocumentSignerSettings();
					signer.setPlaceSingatureOn("");
					signer.setName(signerName);
					signer.setData(base64signature);
					signer.setHeight(40d);
					signer.setWidth(100d);
					signer.setTop(0.83319);
					signer.setLeft(0.72171);
					
					SignatureSignDocumentSettings requestBody = new SignatureSignDocumentSettings();
					List<SignatureSignDocumentSignerSettings> signers = new ArrayList<SignatureSignDocumentSignerSettings>();
					signers.add(signer);
					requestBody.setSigners(signers);
			  		List<SignatureSignDocumentDocumentSettings> documents = new ArrayList<SignatureSignDocumentDocumentSettings>();
					documents.add(document);
					requestBody.setDocuments(documents);
					
					SignatureSignDocumentResponse response = new SignatureApi().SignDocument(credentials.clientId, requestBody);
					if(response != null && response.getStatus().trim().equalsIgnoreCase("Ok")){
						fileGuid = response.getResult().getDocumentId();
					} else {
						throw new ApiException(400, response.getError_message());
					}
					status = ok(views.html.sample6.render(title, sample, fileGuid, filledForm));
				} catch (ApiException e) {
					if(e.getCode() == 401){
						List<Object> args = Arrays.asList(new Object[]{"https://apps.groupdocs.com/My/Manage", "Production Server"});
						filledForm.reject("Wrong Credentials. Please make sure to use credentials from", args);
					} else {
						filledForm.reject("Failed to access API: " + e.getMessage());
					}
					status = badRequest(views.html.sample6.render(title, sample, fileGuid, filledForm));
				} catch (Exception e) {
					if(signerName == null || resourceFile == null || resourceSignature == null){
						if(signerName == null){
							filledForm.reject("signerName", "This field is required");
						}
						if(resourceFile == null){
							filledForm.reject("resourceFile", "This field is required");
						}
						if(resourceSignature == null){
							filledForm.reject("resourceSignature", "This field is required");
						}
					} else {
						filledForm.reject("Unknown Error: " + e.getMessage());
					}
					status = badRequest(views.html.sample6.render(title, sample, fileGuid, filledForm));
				}
			}
		} else {
			filledForm = form.bind(session());
			status = ok(views.html.sample6.render(title, sample, fileGuid, filledForm));
		}
		return status;
	}
	
}