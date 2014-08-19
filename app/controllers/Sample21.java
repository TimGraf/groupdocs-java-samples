//###<i>This sample will show how to use Signature Api to Create and Send Envelope for signing using Java SDK</i>
package controllers;
//Import of necessary libraries

import com.fasterxml.jackson.databind.node.ObjectNode;
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
import org.apache.commons.lang3.StringUtils;
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
    protected static Form<Credentials> form = Form.form(Credentials.class);

    public static Result index() {

        if (Utils.isPOST(request())) {
            form = Form.form(Credentials.class).bindFromRequest();
            // Check errors
            if (form.hasErrors()) {
                return badRequest(views.html.sample21.render(false, null, form));
            }
            // Save credentials to session
            Credentials credentials = form.get();
            session().put("clientId", credentials.getClientId());
            session().put("privateKey", credentials.getPrivateKey());
            session().put("basePath", credentials.getBasePath());
            credentials.normalizeBasePath("https://api.groupdocs.com/v2.0");
            // Get request parameters
            Http.MultipartFormData body = request().body().asMultipartFormData();
            String sourse = Utils.getFormValue(body, "sourse");
            String email = Utils.getFormValue(body, "email");
            String firstName = Utils.getFormValue(body, "name");
            String lastName = Utils.getFormValue(body, "lastName");
            String callback = Utils.getFormValue(body, "callbackUrl");
            String basePath = credentials.getBasePath();
            FilePart filePart = body.getFile("file");
            // Initialize SDK with private key
            ApiInvoker.getInstance().setRequestSigner(
                    new GroupDocsRequestSigner(credentials.getPrivateKey()));

            try {
                //
                String guid = null;
                //
                if ("guid".equals(sourse)) { // File GUID
                    guid = Utils.getFormValue(body, "fileId");
                } else if ("url".equals(sourse)) { // Upload file fron URL
                    String url = Utils.getFormValue(body, "url");
                    StorageApi storageApi = new StorageApi();
                    // Initialize API with base path
                    storageApi.setBasePath(credentials.getBasePath());
                    UploadResponse uploadResponse = storageApi.UploadWeb(credentials.getClientId(), url);
                    // Check response status
                    uploadResponse = Utils.assertResponse(uploadResponse);
                    guid = uploadResponse.getResult().getGuid();
                } else if ("local".equals(sourse)) { // Upload local file
                    Http.MultipartFormData.FilePart file = body.getFile("file");
                    StorageApi storageApi = new StorageApi();
                    // Initialize API with base path
                    storageApi.setBasePath(credentials.getBasePath());
                    FileInputStream is = new FileInputStream(file.getFile());
                    UploadResponse uploadResponse = storageApi.Upload(credentials.getClientId(), file.getFilename(), "uploaded", "", false, new FileStream(is));
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
                SignatureEnvelopeSettingsInfo env = new SignatureEnvelopeSettingsInfo();
                env.setEmailSubject("Sign this!");
                SignatureEnvelopeResponse envelopeResponse = signatureApi.CreateSignatureEnvelope(credentials.getClientId(), "SampleEnvelope_" + UUID.randomUUID(), null, null, null, true, env);
                envelopeResponse = Utils.assertResponse(envelopeResponse);
                // Get an ID of created envelope
                final String envelopeGuid = envelopeResponse.getResult().getEnvelope().getId();

                // Make a request to Signature Api to add document to envelope
                SignatureEnvelopeDocumentResponse envelopeDocument = signatureApi.AddSignatureEnvelopeDocument(credentials.getClientId(), envelopeGuid, guid, null, true);
                // Check response status
                envelopeResponse = Utils.assertResponse(envelopeResponse);
                // Update document ID after it's added to envelope
                String documentGuid = envelopeDocument.getResult().getDocument().getDocumentId();

                // Make a request to Signature Api to get all available roles
                SignatureRolesResponse signatureRolesResponse = signatureApi.GetRolesList(credentials.getClientId(), null);
                // Check response status
                signatureRolesResponse = Utils.assertResponse(signatureRolesResponse);
                List<SignatureRoleInfo> roles = signatureRolesResponse.getResult().getRoles();
                String roleGuid = null;
                for (SignatureRoleInfo role : roles) {
                    // Get an ID of Signer role
                    if ("Signer".equalsIgnoreCase(role.getName())) {
                        roleGuid = role.getId();
                        break;
                    }
                }
                // Make a request to Signature Api to add new recipient to envelope
                SignatureEnvelopeRecipientResponse signatureEnvelopeRecipientResponse = signatureApi.AddSignatureEnvelopeRecipient(credentials.getClientId(), envelopeGuid, email, firstName, lastName, roleGuid, null);
                // Check response status
                signatureEnvelopeRecipientResponse = Utils.assertResponse(signatureEnvelopeRecipientResponse);
                String recipientGuid = signatureEnvelopeRecipientResponse.getResult().getRecipient().getId();

                // Make a request to Signature Api to get all available fields
                SignatureEnvelopeDocumentsResponse getEnvelopDocument = signatureApi.GetSignatureEnvelopeDocuments(credentials.getClientId(), envelopeGuid);
                // Check response status
                getEnvelopDocument = Utils.assertResponse(getEnvelopDocument);
                // Create new field called City
                SignatureEnvelopeFieldSettingsInfo envField = new SignatureEnvelopeFieldSettingsInfo();
                envField.setName("City");
                envField.setLocationX(0.3);
                envField.setLocationY(0.2);
                envField.setLocationWidth(150.0);
                envField.setLocationHeight(50.0);
                envField.setForceNewField(true);
                envField.setPage(1);
                // Make a request to Signature Api to add city field to envelope
                SignatureEnvelopeFieldsResponse signatureEnvelopeFieldsResponse = signatureApi.AddSignatureEnvelopeField(credentials.getClientId(), envelopeGuid, getEnvelopDocument.getResult().getDocuments().get(0).getDocumentId(), recipientGuid, "0545e589fb3e27c9bb7a1f59d0e3fcb9", envField);
                // Check response status
                Utils.assertNotNull(signatureEnvelopeFieldsResponse);
                callback = (callback == null) ? "" : callback;
                WebhookInfo webhookInfo = new WebhookInfo();
                webhookInfo.setCallbackUrl(callback);
                SignatureEnvelopeSendResponse signatureEnvelopeSendResponse = signatureApi.SignatureEnvelopeSend(credentials.getClientId(), envelopeGuid, webhookInfo);
                Utils.assertResponse(signatureEnvelopeSendResponse);

                // Store envelopeId in session for later ues in checkCallbackStatus action
                session().put("envelopeId", envelopeGuid);

                if (!StringUtils.isEmpty(callback)) {
                    FileOutputStream fileOutputStream = new FileOutputStream(USER_INFO_FILE);
                    DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream);

                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(credentials.getClientId());
                    stringBuilder.append("|");
                    stringBuilder.append(credentials.getPrivateKey());
                    stringBuilder.append("|");
                    stringBuilder.append(credentials.getBasePath());

                    dataOutputStream.writeUTF(stringBuilder.toString());

                    dataOutputStream.flush();
                    fileOutputStream.close();
                }
                //
                String server = credentials.getBasePath().substring(0, credentials.getBasePath().indexOf(".com") + 4).replace("api", "apps");
                String embedUrl = server + "/signature2/signembed/" + envelopeGuid + "/" + recipientGuid;
                // Render view
                return ok(views.html.sample21.render(true, embedUrl, form));
            } catch (Exception e) {
                return badRequest(views.html.sample21.render(false, null, form));
            }
        } else if (Utils.isGET(request())) {
            form = form.bind(session());
        }
        return ok(views.html.sample21.render(false, null, form));
    }

    public static Result checkCallbackStatus() {
        String envelopeId = session().get("envelopeId");
        ObjectNode result = Json.newObject();
        boolean status = false;
        String message = "Not everybody signed the envelope";

        if (envelopeId != null && new File(".", envelopeId).exists()) {
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

        if (envelopeId != null && new File(".", envelopeId).exists()) {
            String clientId = session().get("clientId");
            String privateKey = session().get("privateKey");
            System.out.println(clientId + " " + privateKey);

            //Create ApiInvoker using given privateKey
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