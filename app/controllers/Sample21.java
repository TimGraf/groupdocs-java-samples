//###<i>This sample will show how to use Signature Api to Create and Send Envelope for signing using Java SDK</i>
package controllers;
//Import of necessary libraries

import com.groupdocs.sdk.api.SignatureApi;
import com.groupdocs.sdk.api.StorageApi;
import com.groupdocs.sdk.common.ApiException;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.FileStream;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.*;
import com.sun.jersey.core.header.ContentDisposition;
import common.Utils;
import models.Credentials;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.node.ObjectNode;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.UUID;

public class Sample21 extends Controller {
    public static String USER_INFO_FILE = "UserInfo_sample21.tmp";
    //
    protected static Form<Credentials> form = form(Credentials.class);

    public static Result index() {

        if (Utils.isPOST(request())) {
            form = form(Credentials.class).bindFromRequest();
            // Check errors
            if (form.hasErrors()) {
                return badRequest(views.html.sample21.render(false, null, form));
            }
            // Save credentials to session
            Credentials credentials = form.get();
            session().put("client_id", credentials.getClient_id());
            session().put("private_key", credentials.getPrivate_key());
            session().put("server_type", credentials.getServer_type());
            // Get request parameters
            Http.MultipartFormData body = request().body().asMultipartFormData();
            String sourse = Utils.getFormValue(body, "sourse");
            String email = Utils.getFormValue(body, "email");
            String firstName = Utils.getFormValue(body, "name");
            String lastName = Utils.getFormValue(body, "lastName");
            String callback = Utils.getFormValue(body, "callbackUrl");
            String basePath = credentials.getServer_type();
            FilePart filePart = body.getFile("file");
            // Initialize SDK with private key
            ApiInvoker.getInstance().setRequestSigner(
                    new GroupDocsRequestSigner(credentials.getPrivate_key()));

            try {
                //
                String guid = null;
                //
                if ("guid".equals(sourse)) { // File GUID
                    guid = Utils.getFormValue(body, "fileId");
                }
                else if ("url".equals(sourse)) { // Upload file fron URL
                    String url = Utils.getFormValue(body, "url");
                    StorageApi storageApi = new StorageApi();
                    // Initialize API with base path
                    storageApi.setBasePath(credentials.getServer_type());
                    UploadResponse uploadResponse = storageApi.UploadWeb(credentials.getClient_id(), url);
                    // Check response status
                    uploadResponse = Utils.assertResponse(uploadResponse);
                    guid = uploadResponse.getResult().getGuid();
                }
                else if ("local".equals(sourse)) { // Upload local file
                    Http.MultipartFormData.FilePart file = body.getFile("file");
                    StorageApi storageApi = new StorageApi();
                    // Initialize API with base path
                    storageApi.setBasePath(credentials.getServer_type());
                    FileInputStream is = new FileInputStream(file.getFile());
                    UploadResponse uploadResponse = storageApi.Upload(credentials.getClient_id(), file.getFilename(), "uploaded", "", new FileStream(is));
                    // Check response status
                    uploadResponse = Utils.assertResponse(uploadResponse);
                    guid = uploadResponse.getResult().getGuid();
                }
                guid = Utils.assertNotNull(guid);
                // Create Signature api object
                SignatureApi signatureApi = new SignatureApi();
                // Initialize API with base path
                signatureApi.setBasePath(basePath);
                // Make a requests to Signature Api to create an envelope
                SignatureEnvelopeSettings env = new SignatureEnvelopeSettings();
                env.setEmailSubject("Sign this!");
                SignatureEnvelopeResponse envelopeResponse = signatureApi.CreateSignatureEnvelope(credentials.getClient_id(), "SampleEnvelope_" + UUID.randomUUID(), null, null, null, true, env);
                envelopeResponse = Utils.assertResponse(envelopeResponse);
                // Get an ID of created envelope
                final String envelopeId = envelopeResponse.getResult().getEnvelope().getId();

                // Make a request to Signature Api to add document to envelope
                SignatureEnvelopeDocumentResponse envelopeDocument = signatureApi.AddSignatureEnvelopeDocument(credentials.getClient_id(), envelopeId, guid, null, true);
                // Check response status
                envelopeResponse = Utils.assertResponse(envelopeResponse);
                // Update document ID after it's added to envelope
                String documentId = envelopeDocument.getResult().getDocument().getDocumentId();

                // Make a request to Signature Api to get all available roles
                SignatureRolesResponse signatureRolesResponse = signatureApi.GetRolesList(credentials.getClient_id(), null);
                // Check response status
                signatureRolesResponse = Utils.assertResponse(signatureRolesResponse);
                List<SignatureRoleInfo> roles = signatureRolesResponse.getResult().getRoles();
                String roleGuid = null;
                for(SignatureRoleInfo role : roles){
                    // Get an ID of Signer role
                    if("Signer".equalsIgnoreCase(role.getName())){
                        roleGuid = role.getId();
                        break;
                    }
                }
                // Make a request to Signature Api to add new recipient to envelope
                SignatureEnvelopeRecipientResponse signatureEnvelopeRecipientResponse = signatureApi.AddSignatureEnvelopeRecipient(credentials.getClient_id(), envelopeId, email, firstName, lastName, roleGuid, null);
                // Check response status
                signatureEnvelopeRecipientResponse = Utils.assertResponse(signatureEnvelopeRecipientResponse);
                String recipientId = signatureEnvelopeRecipientResponse.getResult().getRecipient().getId();

                // Make a request to Signature Api to get all available fields
                SignatureFieldsResponse signatureFieldsResponse = signatureApi.GetFieldsList(credentials.getClient_id(), null);
                // Check response status
                signatureFieldsResponse = Utils.assertResponse(signatureFieldsResponse);
                List<SignatureFieldInfo> fields = signatureFieldsResponse.getResult().getFields();
                String fieldId = null;
                for(SignatureFieldInfo field : fields){
                    // Get an ID of single line field
                    if(field.getFieldType() == 2){ // single line, see http://scotland.groupdocs.com/wiki/display/SDS/field.type
                        fieldId = field.getId();
                        break;
                    }
                }
                // Create new field called City
                SignatureEnvelopeFieldSettings envField = new SignatureEnvelopeFieldSettings();
                envField.setName("City");
                envField.setLocationX(0.3);
                envField.setLocationY(0.2);
                envField.setPage(1);
                // Make a request to Signature Api to add city field to envelope
                SignatureEnvelopeFieldsResponse signatureEnvelopeFieldsResponse = signatureApi.AddSignatureEnvelopeField(credentials.getClient_id(), envelopeId, documentId, recipientId, fieldId, envField);
                // Check response status
                Utils.assertNotNull(signatureEnvelopeFieldsResponse);

                fieldId = null;
                envField = new SignatureEnvelopeFieldSettings();
                for(SignatureFieldInfo field : fields){
                    // Get an ID of signature field
                    if(field.getFieldType() == 1){ // signature, see http://scotland.groupdocs.com/wiki/display/SDS/field.type
                        fieldId = field.getId();
                        break;
                    }
                }
                envField.setLocationX(0.3);
                envField.setLocationY(0.3);
                envField.setPage(1);
                // Make a request to Signature Api to add signature field to envelope
                signatureEnvelopeFieldsResponse = signatureApi.AddSignatureEnvelopeField(credentials.getClient_id(), envelopeId, documentId, recipientId, fieldId, envField);
                Utils.assertResponse(signatureEnvelopeFieldsResponse);
                // Check is callback entered
                callback = (callback == null) ? "" : callback;

                FileStream stream = new FileStream(IOUtils.toInputStream(callback));
                SignatureEnvelopeSendResponse signatureEnvelopeSendResponse = signatureApi.SignatureEnvelopeSend(credentials.getClient_id(), envelopeId, stream);
                Utils.assertResponse(signatureEnvelopeSendResponse);

                // Store envelopeId in session for later ues in checkCallbackStatus action
                session().put("envelopeId", envelopeId);

                if (!StringUtils.isEmpty(callback)) {
                    FileOutputStream fileOutputStream = new FileOutputStream(USER_INFO_FILE);
                    DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream);

                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(credentials.getClient_id());
                    stringBuilder.append("|");
                    stringBuilder.append(credentials.getPrivate_key());
                    stringBuilder.append("|");
                    stringBuilder.append(credentials.getServer_type());

                    dataOutputStream.writeUTF(stringBuilder.toString());

                    dataOutputStream.flush();
                    fileOutputStream.close();
                }
                //
                String server = credentials.getServer_type().substring(0, credentials.getServer_type().indexOf(".com") + 4).replace("api", "apps");
                String embedUrl = server + "/signature/signembed/" + envelopeId + "/" + recipientId;
                // Render view
                return ok(views.html.sample21.render(true, embedUrl, form));
            } catch (Exception e) {
                return badRequest(views.html.sample21.render(false, null, form));
            }
        } else if (Utils.isGET(request())) {
            form = form.bind(session());
            session().put("server_type", "https://api.groupdocs.com/v2.0");
        }
        return ok(views.html.sample21.render(false, null, form));
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
                // Create Signature api object
                SignatureApi signatureApi = new SignatureApi();

                FileStream zip = signatureApi.GetSignedEnvelopeDocuments(clientId, envelopeId);
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