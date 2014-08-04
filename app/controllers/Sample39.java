//###<i>This sample will show how to use <b>Upload</b> method from Storage Api to upload file to GroupDocs Storage </i>
package controllers;
//Import of necessary libraries

import com.fasterxml.jackson.databind.JsonNode;
import com.groupdocs.sdk.api.AntApi;
import com.groupdocs.sdk.api.MgmtApi;
import com.groupdocs.sdk.api.SignatureApi;
import com.groupdocs.sdk.api.StorageApi;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.FileStream;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.*;
import common.Utils;
import models.Credentials;
import org.apache.commons.lang3.StringUtils;
import play.data.Form;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Http;
import play.mvc.Result;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;

public class Sample39 extends BaseController {
    public static String USER_INFO_FILE = "UserInfo_sample39.tmp";
    //
//    protected static Form<Credentials> form = Form.form(Credentials.class);


    public static Result index() {

        if (Utils.isPOST(request())) {

            String[] names = request().queryString().get("name");

//            Http.RawBuffer rawBuffer = request().body().asRaw();
//            String jsonStr = new String(rawBuffer.asBytes());
//            JsonNode json = Json.parse(jsonStr);

            Http.MultipartFormData body = request().body().asMultipartFormData();
//            form = Form.form(Credentials.class).bindFromRequest();
            // Check errors
//            if (form.hasErrors()) {
//                return badRequest(views.html.sample39.render(false, null, form));
//            }
            // Save credentials to session
//            Credentials credentials = form.get();
//            session().put("clientId", credentials.getClientId());
//            session().put("privateKey", credentials.getPrivateKey());
//            session().put("basePath", credentials.getBasePath());
//            credentials.normalizeBasePath("https://api.groupdocs.com/v2.0");


            try {
                //request().body().asJson()
                // request().body().asText() // request().body().asRaw() // request().body().asMultipartFormData() // request().body().asFormUrlEncoded()
//            $postdata = file_get_contents("php://input");
                // TODO: Parse JSON request!!!
                //### Check if user use Widget for signing
                if (false/* !empty($postdata) */) {
                    // TODO: IF IT IS AJAX JSON REQUEST
                    String error = null;
                    //Decode ajax data
//                $json_post_data = json_decode($postdata, true);
                    //Get Client ID
                    String clientId = null; //$json_post_data['userId'];
                    //
                    String basePath = null;
                    //Get Private Key
                    String privateKey = null; //$json_post_data['privateKey'];
                    //Get document for sign
                    SignatureSignDocumentDocumentSettingsInfo documents = new SignatureSignDocumentDocumentSettingsInfo(); //$json_post_data['documents'];
                    //Get signature file
                    SignatureSignDocumentSignerSettingsInfo signers = new SignatureSignDocumentSignerSettingsInfo(); //$json_post_data['signers'];
                    //Inable signature parameter for the signature object
//                for (int i = 0; i < signers; $i++) {
//                    $signers[$i]['placeSignatureOn'] = '';
//                }
                    ApiInvoker.getInstance().setRequestSigner(new GroupDocsRequestSigner(privateKey));
                    //Create Signature Api object
                    SignatureApi signatureApi = new SignatureApi();
                    signatureApi.setBasePath(basePath);
                    //Create object of sign ssettings
                    SignatureSignDocumentSettingsInfo settings = new SignatureSignDocumentSettingsInfo();
                    //Set document for signing
                    settings.setDocuments(Arrays.asList(documents));
                    //Set signature
                    settings.setSigners(Arrays.asList(signers));
                    //Make request to sign documnet
                    SignatureSignDocumentResponse signatureSignDocumentResponse = signatureApi.SignDocument(clientId, settings);
                    Utils.assertResponse(signatureSignDocumentResponse);
                    String guid = null;
                    //Get signed document GUID
                    for (int i = 0; i < 5; i++) {
                        //Check status of documnet is it signed
                        SignatureSignDocumentStatusResponse signatureSignDocumentStatusResponse = signatureApi.GetSignDocumentStatus(clientId, signatureSignDocumentResponse.getResult().getJobId());
                        Utils.assertResponse(signatureSignDocumentStatusResponse);
                        if ("Completed".equals(signatureSignDocumentStatusResponse.getResult().getDocuments().get(0).getStatus())) {
                            //Get file GUID
                            guid = signatureSignDocumentStatusResponse.getResult().getDocuments().get(0).getDocumentId();
                            break;
                        } else {
                            //Wait while server processed data
                            Thread.sleep(3);
                        }
                    }
                    //Create array with result data
                    String result = "'guid': '" + guid + "'," +
                            "'clientId': '" + clientId + "'" +
                            "'privateKey': '" + privateKey + "'";
                } else /* if (!empty($_POST["clientId"]))*/ {

                    // Get request parameters
                    String clientId = Utils.getFormValue(body, "clientId");
                    String privateKey = Utils.getFormValue(body, "privateKey");
                    String basePath = Utils.getFormValue(body, "basePath");
                    String email = Utils.getFormValue(body, "email");
                    String signName = Utils.getFormValue(body, "name");
                    String lastName = Utils.getFormValue(body, "lastName");
                    String callbackUrl = Utils.getFormValue(body, "callbackUrl");
                    // Initialize SDK with private key
//            ApiInvoker.getInstance().setRequestSigner(
//                    new GroupDocsRequestSigner(credentials.getPrivateKey()));
                    //path to settings file - temporary save userId and apiKey like to property file
                    if (!StringUtils.isEmpty(callbackUrl)) {
                        FileOutputStream fileOutputStream = new FileOutputStream(USER_INFO_FILE);
                        DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream);

                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(clientId);
                        stringBuilder.append("|");
                        stringBuilder.append(privateKey);
                        stringBuilder.append("|");
                        stringBuilder.append(basePath);

                        dataOutputStream.writeUTF(stringBuilder.toString());

                        dataOutputStream.flush();
                        fileOutputStream.close();
                    }
                    ApiInvoker.getInstance().setRequestSigner(new GroupDocsRequestSigner(privateKey));
                    //Create Storage Api object
                    StorageApi storageApi = new StorageApi();
                    //Get uploaded file
                    File file = body.getFile("file").getFile();
                    if (file != null && !file.getName().isEmpty()) {
                        //###Make a request to Storage API using clientId
                        //Upload file to current user storage
                        UploadResponse uploadResponse = storageApi.Upload(clientId, file.getName(), "uploaded", "", new FileStream(new FileInputStream(file)));
                        //###Check if file uploaded successfully
                        Utils.assertResponse(uploadResponse);
                        //Get file GUID
                        String fileGuId = uploadResponse.getResult().getGuid();
                        //Get file name
                        String adjName = uploadResponse.getResult().getAdj_name();
                        //Create SignatureApi object
                        SignatureApi signature = new SignatureApi();
                        //Create envilope using user id and entered by user name
                        SignatureEnvelopeResponse signatureEnvelopeResponse = signature.CreateSignatureEnvelope(clientId, adjName, null, null, null, false, null);
                        Utils.assertResponse(signatureEnvelopeResponse);
                        Thread.sleep(5);
                        //Add uploaded document to envelope
                        SignatureEnvelopeDocumentResponse signatureEnvelopeDocumentResponse = signature.AddSignatureEnvelopeDocument(clientId, signatureEnvelopeResponse.getResult().getEnvelope().getId(), fileGuId, null, true);
                        Utils.assertResponse(signatureEnvelopeDocumentResponse);
                        //Get role list for current user
                        SignatureRolesResponse signatureRolesResponse = signature.GetRolesList(clientId, null);
                        Utils.assertResponse(signatureRolesResponse);
                        //Get id of role which can sign\
                        String roleId = null;
                        for (int i = 0; i < signatureRolesResponse.getResult().getRoles().size(); i++) {
                            if ("Signer".equals(signatureRolesResponse.getResult().getRoles().get(i).getName())) {
                                roleId = signatureRolesResponse.getResult().getRoles().get(i).getId();
                            }
                        }
                        String envelopeId = signatureEnvelopeDocumentResponse.getResult().getEnvelopeId();
                        //Add recipient to envelope
                        SignatureEnvelopeRecipientResponse signatureEnvelopeRecipientResponse = signature.AddSignatureEnvelopeRecipient(clientId, envelopeId, email, signName, lastName, roleId, null);
                        Utils.assertResponse(signatureEnvelopeRecipientResponse);
                        //Ger recipient ID
                        SignatureEnvelopeRecipientsResponse signatureEnvelopeRecipientsResponse = signature.GetSignatureEnvelopeRecipients(clientId, envelopeId);
                        Utils.assertResponse(signatureEnvelopeRecipientsResponse);
                        String recipientId = signatureEnvelopeRecipientsResponse.getResult().getRecipients().get(0).getId();
                        //Get Url for callbackUrl
                        //Get document from envelop
                        SignatureEnvelopeDocumentsResponse signatureEnvelopeDocumentsResponse = signature.GetSignatureEnvelopeDocuments(clientId, envelopeId);
                        Utils.assertResponse(signatureEnvelopeDocumentsResponse);
                        //Create sognature field
                        SignatureEnvelopeFieldSettingsInfo signFieldEnvelopSettings = new SignatureEnvelopeFieldSettingsInfo();
                        signFieldEnvelopSettings.setLocationX(0.15);
                        signFieldEnvelopSettings.setLocationY(0.73);
                        signFieldEnvelopSettings.setLocationWidth(150d);
                        signFieldEnvelopSettings.setLocationHeight(50d);
                        signFieldEnvelopSettings.setName("test_" + signFieldEnvelopSettings.toString());
                        signFieldEnvelopSettings.setForceNewField(true);
                        signFieldEnvelopSettings.setPage(1);
                        //Add signature field to document
                        signature.AddSignatureEnvelopeField(clientId, envelopeId, signatureEnvelopeDocumentsResponse.getResult().getDocuments().get(0).getDocumentId(), recipientId, "0545e589fb3e27c9bb7a1f59d0e3fcb9", signFieldEnvelopSettings);
                        //Create WebHook object (URL which will be trigered by callback)
                        WebhookInfo webHook = new WebhookInfo();
                        webHook.setCallbackUrl(callbackUrl != null && !callbackUrl.isEmpty() ? callbackUrl : "");
                        //Send envelop for signing
                        SignatureEnvelopeSendResponse signatureEnvelopeSendResponse = signature.SignatureEnvelopeSend(clientId, envelopeId, webHook);
                        Utils.assertResponse(signatureEnvelopeSendResponse);
                        //Create URL for iframe
                        String server = basePath.substring(0, basePath.indexOf(".com") + 4).replace("api", "apps");
                        String url = server + "/signature2/signembed/" + envelopeId + "/" + recipientId;
                        //Sign URL
                        url = new GroupDocsRequestSigner(privateKey).signUrl(url);
                        return ok(views.html.sample39.render(true, url));
                    }
                }
            } catch (Exception e) {
                return badRequest(views.html.sample39.render(false, null));
            }

        } else if (Utils.isGET(request())) {
//            form = form.bind(session());
        }
        return ok(views.html.sample39.render(false, null));
    }
}