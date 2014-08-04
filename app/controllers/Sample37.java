//###<i>This sample will show how to use <b>Upload</b> method from Storage Api to upload file to GroupDocs Storage </i>
package controllers;
//Import of necessary libraries

import com.groupdocs.sdk.api.SignatureApi;
import com.groupdocs.sdk.api.StorageApi;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.FileStream;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.*;
import common.Utils;
import models.Credentials;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import play.Play;
import play.data.Form;
import play.mvc.Http;
import play.mvc.Result;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class Sample37 extends BaseController {
    public static String USER_INFO_FILE = "UserInfo_sample37.tmp";
    //
    protected static Form<Credentials> form = Form.form(Credentials.class);

    public static Result index() {

        if (Utils.isPOST(request())) {
            Http.MultipartFormData body = request().body().asMultipartFormData();
                form = Form.form(Credentials.class).bindFromRequest();
                // Check errors
                if (form.hasErrors()) {
                    return badRequest(views.html.sample37.render(false, null, null, null, null, form));
                }
                // Save credentials to session
                Credentials credentials = form.get();
                session().put("clientId", credentials.getClientId());
                session().put("privateKey", credentials.getPrivateKey());
                session().put("basePath", credentials.getBasePath());
                credentials.normalizeBasePath("https://api.groupdocs.com/v2.0");
                // Initialize SDK with private key
                ApiInvoker.getInstance().setRequestSigner(
                        new GroupDocsRequestSigner(credentials.getPrivateKey()));

                try {
                    // Get request parameters
                    String email = Utils.getFormValue(body, "email");
                    // Get request parameters
                    String signName = Utils.getFormValue(body, "name");
                    // Get request parameters
                    String lastName = Utils.getFormValue(body, "lastName");
                    // Get request parameters
                    String callbackUrl = Utils.getFormValue(body, "callbackUrl");
                    //
                    String sourse = Utils.getFormValue(body, "sourse");


                    //path to settings file - temporary save userId and apiKey like to property file
                    if (!StringUtils.isEmpty(callbackUrl)) {
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
                        Http.MultipartFormData.FilePart file = body.getFile("local");
                        StorageApi storageApi = new StorageApi();
                        // Initialize API with base path
                        storageApi.setBasePath(credentials.getBasePath());
                        FileInputStream is = new FileInputStream(file.getFile());
                        UploadResponse uploadResponse = storageApi.Upload(credentials.getClientId(), file.getFilename(), "uploaded", "", new FileStream(is));
                        // Check response status
                        uploadResponse = Utils.assertResponse(uploadResponse);
                        guid = uploadResponse.getResult().getGuid();
                    }
                    guid = Utils.assertNotNull(guid);
                        //###Make a request to Storage API using clientId
                        //Obtaining all Entities from current user
                    StorageApi storageApi = new StorageApi();
                    String fileName = Utils.getFileNameByGuid(credentials.getClientId(), credentials.getPrivateKey(), credentials.getBasePath(), guid);

                    //Create SignatureApi object
                    SignatureApi signature = new SignatureApi();
                    signature.setBasePath(credentials.getBasePath());
                    //Create envilope using user id and entered by user name
                    SignatureEnvelopeResponse signatureEnvelopeResponse = signature.CreateSignatureEnvelope(credentials.getClientId(), fileName, null, null, null, false, null);
                    Utils.assertResponse(signatureEnvelopeResponse);
                        Thread.sleep(5);
                        //Add uploaded document to envelope
                    SignatureEnvelopeDocumentResponse signatureEnvelopeDocumentResponse = signature.AddSignatureEnvelopeDocument(credentials.getClientId(), signatureEnvelopeResponse.getResult().getEnvelope().getId(), guid, null, true);
                    Utils.assertResponse(signatureEnvelopeDocumentResponse);
                    String envelopeId = signatureEnvelopeDocumentResponse.getResult().getEnvelopeId();
                    SignatureRolesResponse signatureRolesResponse = signature.GetRolesList(credentials.getClientId(), null);
                    Utils.assertResponse(signatureRolesResponse);
                    //Get id of role which can sign
                    String roleId = null;
                    for (int i = 0; i < signatureRolesResponse.getResult().getRoles().size(); i++) {
                        if ("Signer".equals(signatureRolesResponse.getResult().getRoles().get(i).getName())) {
                            roleId = signatureRolesResponse.getResult().getRoles().get(i).getId();
                        }
                    }
                        //Add recipient to envelope
                    SignatureEnvelopeRecipientResponse signatureEnvelopeRecipientResponse = signature.AddSignatureEnvelopeRecipient(credentials.getClientId(), envelopeId, email, signName, lastName, roleId, null);
                    Utils.assertResponse(signatureEnvelopeRecipientResponse);
                    //Get recipient id
                    SignatureEnvelopeRecipientsResponse signatureEnvelopeRecipientsResponse = signature.GetSignatureEnvelopeRecipients(credentials.getClientId(), envelopeId);
                    Utils.assertResponse(signatureEnvelopeRecipientsResponse);
                    String recipientId = signatureEnvelopeRecipientsResponse.getResult().getRecipients().get(0).getId();
                    //Url for callbackUrl
                    SignatureEnvelopeDocumentsResponse signatureEnvelopeDocumentsResponse = signature.GetSignatureEnvelopeDocuments(credentials.getClientId(), envelopeId);
                    Utils.assertResponse(signatureEnvelopeDocumentsResponse);
                    SignatureEnvelopeFieldSettingsInfo signFieldEnvelopSettings = new SignatureEnvelopeFieldSettingsInfo();
                    signFieldEnvelopSettings.setLocationX(0.15);
                    signFieldEnvelopSettings.setLocationY(0.73);
                    signFieldEnvelopSettings.setLocationWidth(150d);
                    signFieldEnvelopSettings.setLocationHeight(50d);
                    signFieldEnvelopSettings.setName("test_" + signFieldEnvelopSettings.toString());
                    signFieldEnvelopSettings.setForceNewField(true);
                    signFieldEnvelopSettings.setPage(1);
                    SignatureEnvelopeFieldsResponse signatureEnvelopeFieldsResponse = signature.AddSignatureEnvelopeField(credentials.getClientId(), envelopeId, signatureEnvelopeDocumentsResponse.getResult().getDocuments().get(0).getDocumentId(), recipientId, "0545e589fb3e27c9bb7a1f59d0e3fcb9", signFieldEnvelopSettings);
                    Utils.assertResponse(signatureEnvelopeFieldsResponse);
                    WebhookInfo webHook = new WebhookInfo();
                    if (callbackUrl != null && !callbackUrl.isEmpty()) {
                        webHook.setCallbackUrl(callbackUrl);
                    } else {
                        webHook.setCallbackUrl("");
                    }
                    SignatureEnvelopeSendResponse signatureEnvelopeSendResponse = signature.SignatureEnvelopeSend(credentials.getClientId(), envelopeId, webHook);
                    Utils.assertResponse(signatureEnvelopeSendResponse);

                    String server = credentials.getBasePath().substring(0, credentials.getBasePath().indexOf(".com") + 4).replace("api", "apps");

                    return ok(views.html.sample37.render(true, fileName, server, envelopeId, recipientId, form));
                } catch (Exception e) {
                    return badRequest(views.html.sample37.render(false, null, null, null, null, form));
                }

        } else if (Utils.isGET(request())) {
            form = form.bind(session());
        }
        return ok(views.html.sample37.render(false, null, null, null, null, form));
    }
}