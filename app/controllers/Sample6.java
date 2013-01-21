//###<i>This sample will show how to use <b>SignDocument</b> method from Signature Api to Sign Document and upload it to user storage</i>
package controllers;
//Import of necessary libraries
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
import com.groupdocs.sdk.common.MimeUtils;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.SignatureSignDocumentDocumentSettings;
import com.groupdocs.sdk.model.SignatureSignDocumentsResponse;
import com.groupdocs.sdk.model.SignatureSignDocumentSettings;
import com.groupdocs.sdk.model.SignatureSignDocumentSignerSettings;

public class Sample6 extends Controller {
	//###Set variables
	static String title = "GroupDocs Java SDK Samples";
	static Form<Credentials> form = form(Credentials.class);
	
	public static Result index() {
		String fileGuid = null;
		Form<Credentials> filledForm;
		String sample = "Sample6";
		Status status;
		//Check POST parameters
		if(request().method().equalsIgnoreCase("POST")){
			filledForm = form.bindFromRequest();
			if(filledForm.hasErrors()){
				status = badRequest(views.html.sample6.render(title, sample, fileGuid, filledForm));
			} else {
				//Get POST data
				Credentials credentials = filledForm.get();
				session().put("client_id", credentials.client_id);
				session().put("private_key", credentials.private_key);
				
				MultipartFormData body = request().body().asMultipartFormData();
				Map<String, String[]> formData = body.asFormUrlEncoded();
		        FilePart fi_document = body.getFile("fi_document");
		        FilePart fi_signature = body.getFile("fi_signature");
		        String signerName = "GroupDocs";
		        
				try {
					//Check is NULL document to sign and signature file
					if(fi_document == null || fi_signature == null){
						throw new Exception();
					}
					//Create ApiInvoker,
					ApiInvoker.getInstance().setRequestSigner(
							new GroupDocsRequestSigner(credentials.private_key));
					//Read document to sign from URL
					String base64file = MimeUtils.readAsDataURL(fi_document.getFile(), fi_document.getContentType());
					//Read signature file from URL
					String base64signature = MimeUtils.readAsDataURL(fi_signature.getFile(), fi_signature.getContentType());
			  		//Set sign settings
			  		SignatureSignDocumentDocumentSettings document = new SignatureSignDocumentDocumentSettings();
			  		document.setName(fi_document.getFilename());
			  		document.setData(base64file);
					//Create SignatureSignDocumentSignerSettings object
					SignatureSignDocumentSignerSettings signer = new SignatureSignDocumentSignerSettings();
					signer.setPlaceSignatureOn("");
					signer.setName(signerName);
					signer.setData(base64signature);
					signer.setHeight(40d);
					signer.setWidth(100d);
					signer.setTop(0.83319);
					signer.setLeft(0.72171);
					//###Make request to sign settings
					SignatureSignDocumentSettings requestBody = new SignatureSignDocumentSettings();
					//Add signer to sign settings List
					List<SignatureSignDocumentSignerSettings> signers = new ArrayList<SignatureSignDocumentSignerSettings>();
					signers.add(signer);
					//Set signer
					requestBody.setSigners(signers);
					//Add document for sign to sign settings List
			  		List<SignatureSignDocumentDocumentSettings> documents = new ArrayList<SignatureSignDocumentDocumentSettings>();
					documents.add(document);
					//Set document
					requestBody.setDocuments(documents);
					//###Make a request to Signature Api for sign document
			        
			        //Sign document using current user id and sign settings
					SignatureSignDocumentsResponse response = new SignatureApi().SignDocument(credentials.client_id, requestBody);
					//Check request result
					if(response != null && response.getStatus().trim().equalsIgnoreCase("Ok")){
						fileGuid = response.getResult().getDocuments().getDocumentId();
					} else {
						throw new ApiException(400, response.getError_message());
					}
					//If request was successfull - set fileGuid variable for template
					status = ok(views.html.sample6.render(title, sample, fileGuid, filledForm));
			    //###Definition of Api errors and conclusion of the corresponding message
				} catch (ApiException e) {
					if(e.getCode() == 401){
						List<Object> args = Arrays.asList(new Object[]{"https://apps.groupdocs.com/My/Manage", "Production Server"});
						filledForm.reject("Wrong Credentials. Please make sure to use credentials from", args);
					} else {
						filledForm.reject("Failed to access API: " + e.getMessage());
					}
					status = badRequest(views.html.sample6.render(title, sample, fileGuid, filledForm));
				//###Definition of filledForm errors and conclusion of the corresponding message	
				} catch (Exception e) {
					if(fi_document == null || fi_signature == null){

						if(fi_document == null){
							filledForm.reject("fi_document", "This field is required");
						}
						if(fi_signature == null){
							filledForm.reject("fi_signature", "This field is required");
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
		//Process template
		return status;
	}
	
}