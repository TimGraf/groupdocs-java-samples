//###<i>This sample will show how to use Signature Api to Create and Send Envelope for signing using Java SDK</i>
package controllers;
//Import of necessary libraries
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.IOUtils;

import models.Credentials;
import play.data.Form;
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

public class Sample21 extends Controller {
	//###Set variables
	static String title = "GroupDocs Java SDK Samples";
	static Form<Credentials> form = form(Credentials.class);
	
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
				
				MultipartFormData body = request().body().asMultipartFormData();
				Map<String, String[]> formData = body.asFormUrlEncoded();
				String email = formData.get("email") != null ? formData.get("email")[0] : null;
				String firstName = formData.get("firstName") != null ? formData.get("firstName")[0] : null;
				String lastName = formData.get("lastName") != null ? formData.get("lastName")[0] : null;
		        FilePart fi_document = body.getFile("fi_document");
		        
				try {
					//Check if all form fields are filled in
					if(fi_document == null || email == null || firstName == null || lastName == null){
						throw new Exception();
					}
					//Create ApiInvoker using given private_key
					ApiInvoker.getInstance().setRequestSigner(
							new GroupDocsRequestSigner(credentials.private_key));
					
					//###Make request to Storage Api to upload document
					StorageApi storageApi = new StorageApi();
					FileStream fs = new FileStream(new FileInputStream(fi_document.getFile()));
					String documentId = storageApi.Upload(credentials.client_id, "samples/signature/" + fi_document.getFilename(), null, fs).getResult().getGuid();
					IOUtils.closeQuietly(fs.getInputStream());

					//###Make a requests to Signature Api to create an envelope
					final SignatureApi api = new SignatureApi();
					SignatureEnvelopeSettings env = new SignatureEnvelopeSettings();
					env.setEmailSubject("Sign this!");
					SignatureEnvelopeResponse envelopeResponse = api.CreateSignatureEnvelope(credentials.client_id, "SampleEnvelope_" + UUID.randomUUID(), env, null, null);
					//Get an ID of created envelope
					final String envelopeId = envelopeResponse.getResult().getEnvelope().getId();
					
					//###Make a request to Signature Api to add document to envelope
					SignatureEnvelopeDocumentResponse envelopeDocument = api.AddSignatureEnvelopeDocument(credentials.client_id, envelopeId, documentId, null);
					//Update document ID after it's added to envelope
					documentId = envelopeDocument.getResult().getDocument().getDocumentId();

					//###Make a request to Signature Api to get all available roles
					String roleId = null;
					List<SignatureRoleInfo> roles = api.GetRolesList(credentials.client_id, null).getResult().getRoles();
					for(SignatureRoleInfo role : roles){
						//Get an ID of Signer role
						if(role.getName().equalsIgnoreCase("Signer")){
							roleId = role.getId();
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
					String callbackHost = "groupdocs-java-samples.herokuapp.com";
					FileStream stream = new FileStream(IOUtils.toInputStream("http://" + callbackHost + "/dummyCallbackHandler"));
					api.SignatureEnvelopeSend(credentials.client_id, envelopeId, stream);
					
					//Construct embedded signature url
					embedUrl = "https://apps.groupdocs.com/signature/signembed/" + envelopeId + "/" + recipientId;
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
			filledForm = form.bind(session());
			status = ok(views.html.sample21.render(title, sample, embedUrl, filledForm));
		}
		//Process template
		return status;
	}
	
}