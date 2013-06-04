//###<i>This sample will show how to use Signature Api to Create and Send Envelope for signing using Java SDK</i>
package controllers;
//Import of necessary libraries
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import models.Credentials;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.node.ObjectNode;

import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import scala.actors.threadpool.Arrays;

import com.groupdocs.sdk.api.SignatureApi;
import com.groupdocs.sdk.api.StorageApi;
import com.groupdocs.sdk.common.ApiException;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.FileStream;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.SignatureEnvelopeDocumentResponse;
import com.groupdocs.sdk.model.SignatureEnvelopeFieldSettings;
import com.groupdocs.sdk.model.SignatureEnvelopeResponse;
import com.groupdocs.sdk.model.SignatureEnvelopeSettings;
import com.groupdocs.sdk.model.SignatureFieldInfo;
import com.groupdocs.sdk.model.SignatureRoleInfo;
import com.sun.jersey.core.header.ContentDisposition;

public class Sample21 extends Controller {
	//###Set variables
	static String title = "GroupDocs Java SDK Samples";
	static Form<Credentials> form = form(Credentials.class);
	static String server = ""; // use "" for production, "stage-" for staging server
	static StorageApi storageApi = new StorageApi();
	static SignatureApi api = new SignatureApi();
	
	static {
		storageApi.setBasePath("https://" + server + "api.groupdocs.com/v2.0");
		api.setBasePath("https://" + server + "api.groupdocs.com/v2.0");
	}
	
	public static Result index() {
		String embedUrl = null;
		Form<Credentials> filledForm;
		String sample = "Sample21";
		Status status;
		//Check POST parameters
		if(request().method().equalsIgnoreCase("POST")){
			filledForm = form.bindFromRequest();
			if(filledForm.hasErrors()){
				status = badRequest(views.html.sample21.render(title, sample, embedUrl, filledForm));
			} else {
				//Get POST data
				Credentials credentials = filledForm.get();
				session().put("client_id", credentials.client_id);
				session().put("private_key", credentials.private_key);
				session().put("baseurl", credentials.baseurl);
				
				MultipartFormData body = request().body().asMultipartFormData();
				Map<String, String[]> formData = body.asFormUrlEncoded();
				String email = formData.get("email") != null ? formData.get("email")[0] : null;
				String firstName = formData.get("name") != null ? formData.get("name")[0] : null;
				String lastName = formData.get("lastName") != null ? formData.get("lastName")[0] : null;
		        FilePart fi_document = body.getFile("file");
		        String callback = formData.get("callbackUrl") != null ? formData.get("callbackUrl")[0] : null;
				callback = StringUtils.isBlank(callback) ? null : callback.trim();
				String basePath = credentials.baseurl;
		        
				try {
					//Check if all form fields are filled in
					if(fi_document == null || email == null || firstName == null || lastName == null){
						throw new Exception();
					}
					//Create ApiInvoker using given private_key
					ApiInvoker.getInstance().setRequestSigner(
							new GroupDocsRequestSigner(credentials.private_key));
					//Create Storage Api object
					StorageApi storage = new StorageApi();
					//Choose server to use
					storage.setBasePath(basePath);
					//###Make request to Storage Api to upload document
					FileStream fs = new FileStream(new FileInputStream(fi_document.getFile()));
					String documentId = storage.Upload(credentials.client_id, "samples/signature/" + fi_document.getFilename(), "uploaded", "", fs).getResult().getGuid();
					IOUtils.closeQuietly(fs.getInputStream());
					//Create Signature api object
					SignatureApi api = new SignatureApi();
					//Choose Server to use
					api.setBasePath(basePath);
					//Make a requests to Signature Api to create an envelope
					SignatureEnvelopeSettings env = new SignatureEnvelopeSettings();
					env.setEmailSubject("Sign this!");
					SignatureEnvelopeResponse envelopeResponse = api.CreateSignatureEnvelope(credentials.client_id, "SampleEnvelope_" + UUID.randomUUID(), null, null, null, env);
					//Get an ID of created envelope
					final String envelopeId = envelopeResponse.getResult().getEnvelope().getId();
					
					//###Make a request to Signature Api to add document to envelope
					SignatureEnvelopeDocumentResponse envelopeDocument = api.AddSignatureEnvelopeDocument(credentials.client_id, envelopeId, documentId, null);
					//Update document ID after it's added to envelope
					documentId = envelopeDocument.getResult().getDocument().getDocumentId();

					//###Make a request to Signature Api to get all available roles
					Integer roleId = null;
					List<SignatureRoleInfo> roles = api.GetRolesList(credentials.client_id, null).getResult().getRoles();
					for(SignatureRoleInfo role : roles){
						//Get an ID of Signer role
						if(role.getName().equalsIgnoreCase("Signer")){
							roleId = Integer.parseInt(role.getId());
						}
					}
					//###Make a request to Signature Api to add new recipient to envelope
					String recipientId = api.AddSignatureEnvelopeRecipient(credentials.client_id, envelopeId, email, firstName, lastName, null, roleId).getResult().getRecipient().getId();
					
					//###Make a request to Signature Api to get all available fields
					String fieldId = null;
					List<SignatureFieldInfo> fields = api.GetFieldsList(credentials.client_id, null).getResult().getFields();
					for(SignatureFieldInfo field : fields){
						//Get an ID of single line field
						if(field.getFieldType() == 2){ // single line, see http://scotland.groupdocs.com/wiki/display/SDS/field.type
							fieldId = field.getId();
						}
					}
					//Create new field called City
					SignatureEnvelopeFieldSettings envField = new SignatureEnvelopeFieldSettings();
					envField.setName("City");
					envField.setLocationX(0.3);
					envField.setLocationY(0.2);
					envField.setPage(1);
					//###Make a request to Signature Api to add city field to envelope
					api.AddSignatureEnvelopeField(credentials.client_id, envelopeId, documentId, recipientId, fieldId, envField);
					
					fieldId = null;
					envField = new SignatureEnvelopeFieldSettings();
					for(SignatureFieldInfo field : fields){
						//Get an ID of signature field
						if(field.getFieldType() == 1){ // signature, see http://scotland.groupdocs.com/wiki/display/SDS/field.type
							fieldId = field.getId();
						}
					}
					envField.setLocationX(0.3);
					envField.setLocationY(0.3);
					envField.setPage(1);
					//###Make a request to Signature Api to add signature field to envelope
					api.AddSignatureEnvelopeField(credentials.client_id, envelopeId, documentId, recipientId, fieldId, envField);
					
					//###Make a request to Signature Api to send envelope for signing
					
					//Check is callback entered
					if (callback == null) {
						callback = "";
					}
					FileStream stream = new FileStream(IOUtils.toInputStream(callback));
					api.SignatureEnvelopeSend(credentials.client_id, envelopeId, stream);
					
					//Store envelopeId in session for later ues in checkCallbackStatus action
					session().put("envelopeId", envelopeId);
					
					//Construct embedded signature url
					if (basePath.equals("https://api.groupdocs.com/v2.0")) {
						embedUrl = "https://apps.groupdocs.com/signature/signembed/" + envelopeId + "/" + recipientId;
	                //iframe to dev server
	                } else if(basePath.equals("https://dev-api.groupdocs.com/v2.0")) {
	                	embedUrl = "https://dev-apps.groupdocs.com/signature/signembed/" + envelopeId + "/" + recipientId;
	                //iframe to test server
	                } else if(basePath.equals("https://stage-api.groupdocs.com/v2.0")) {
	                	embedUrl = "https://stage-apps.groupdocs.com/signature/signembed/" + envelopeId + "/" + recipientId;
	                }
					//Use embedded signature url in template
					status = ok(views.html.sample21.render(title, sample, embedUrl, filledForm));
			    //###Definition of Api errors and conclusion of the corresponding message
				} catch (ApiException e) {
					if(e.getCode() == 401){
						List<Object> args = Arrays.asList(new Object[]{"https://apps.groupdocs.com/My/Manage", "Production Server"});
						filledForm.reject("Wrong Credentials. Please make sure to use credentials from", args);
					} else {
						filledForm.reject("Failed to access API: " + e.getMessage());
					}
					status = badRequest(views.html.sample21.render(title, sample, embedUrl, filledForm));
				//###Definition of filledForm errors and conclusion of the corresponding message	
				} catch (Exception e) {
					if(fi_document == null){

						if(fi_document == null){
							filledForm.reject("fi_document", "This field is required");
						}
					} else {
						filledForm.reject("Unknown Error: " + e.getMessage());
					}
					status = badRequest(views.html.sample21.render(title, sample, embedUrl, filledForm));
				}
			}
		} else {
			Map<String, String> sampleValues = new HashMap<String, String>(session());
			sampleValues.put("email", "john@smith.com");
			sampleValues.put("firstName", "John");
			sampleValues.put("lastName", "Smith");
			
			filledForm = form.bind(sampleValues);
			session().put("baseurl", "https://api.groupdocs.com/v2.0");
			status = ok(views.html.sample21.render(title, sample, embedUrl, filledForm));
		}
		//Process template
		return status;
	}
	
	public static Result checkCallbackStatus() {
		String envelopeId = session().get("envelopeId");
		ObjectNode result = Json.newObject();
		boolean status = false;
		String message = "Not everybody signed the envelope";
		
		if(envelopeId != null && new File(".", envelopeId).exists()){
			status = true;
			message = "Everybody signed the envelope. Click <a href='/downloadEnvelope' target='_blank'>here</a> to download it";
		}
		
		result.put("status", status);
		result.put("message", message);
		return ok(result);
	}
	
	public static Result downloadEnvelope() {
		String envelopeId = session().get("envelopeId");
		Result status;
		
		if(envelopeId != null && new File(".", envelopeId).exists()){
			String clientId = session().get("client_id");
			String privateKey = session().get("private_key");
			System.out.println(clientId + " " + privateKey);
			
			//Create ApiInvoker using given private_key
//			ApiInvoker.getInstance().setDebug(true);
			ApiInvoker.getInstance().setRequestSigner(
					new GroupDocsRequestSigner(privateKey));
			try {
				FileStream zip = api.GetSignedEnvelopeDocuments(clientId, envelopeId);
				response().setHeader("Content-Disposition", ContentDisposition.type("attachment").fileName(zip.getFileName()).build().toString());
				status = ok(zip.getInputStream());
//				new File(".", envelopeId).delete();
				
			} catch (ApiException e) {
				e.printStackTrace();
				status = badRequest("Failed to access API: " + e.getMessage());
			} 
		} else {
			status = ok("Callback handler was not called yet.");
		}
		
		return status;
	}
	
}