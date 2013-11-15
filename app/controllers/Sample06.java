//###<i>This sample will show how to use <b>SignDocument</b> method from Signature Api to Sign Document and upload it to user storage</i>
package controllers;
//Import of necessary libraries

import com.groupdocs.sdk.api.SignatureApi;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.common.MimeUtils;
import com.groupdocs.sdk.model.*;
import common.Utils;
import models.Credentials;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;

import java.util.ArrayList;
import java.util.List;

public class Sample06 extends Controller {
    //
    protected static Form<Credentials> form = Form.form(Credentials.class);

    public static Result index() {

        if (Utils.isPOST(request())) {
            form = Form.form(Credentials.class).bindFromRequest();
            // Check errors
            if (form.hasErrors()) {
                return badRequest(views.html.sample06.render(false, null, form));
            }
            // Save credentials to session
            Credentials credentials = form.get();
            session().put("clientId", credentials.getClientId());
            session().put("privateKey", credentials.getPrivateKey());
            session().put("basePath", credentials.getBasePath());
            credentials.normalizeBasePath("https://api.groupdocs.com/v2.0");
            // Get request parameters
            MultipartFormData body = request().body().asMultipartFormData();
            FilePart fiDocument = body.getFile("fiDocument");
            FilePart fiSignature = body.getFile("fiSignature");
            String signerName = "GroupDocs Signer";
            // Initialize SDK with private key
            ApiInvoker.getInstance().setRequestSigner(
                    new GroupDocsRequestSigner(credentials.getPrivateKey()));

            try {
                fiDocument = Utils.assertNotNull(fiDocument);
                fiSignature = Utils.assertNotNull(fiSignature);

                // Read document to sign from URL
                String base64file = MimeUtils.readAsDataURL(fiDocument.getFile(), fiDocument.getContentType());
                //Read signature file from URL
                String base64signature = MimeUtils.readAsDataURL(fiSignature.getFile(), fiSignature.getContentType());
                // Set sign settings
                SignatureSignDocumentDocumentSettingsInfo document = new SignatureSignDocumentDocumentSettingsInfo();
                document.setName(fiDocument.getFilename());
                document.setData(base64file);
                // Create SignatureSignDocumentSignerSettings object
                SignatureSignDocumentSignerSettingsInfo signer = new SignatureSignDocumentSignerSettingsInfo();
                signer.setPlaceSignatureOn("");
                signer.setName(signerName);
                signer.setData(base64signature);
                signer.setHeight(40d);
                signer.setWidth(100d);
                signer.setTop(0.83319);
                signer.setLeft(0.72171);
                // Make request to sign settings
                SignatureSignDocumentSettingsInfo requestBody = new SignatureSignDocumentSettingsInfo();
                // Add signer to sign settings List
                List<SignatureSignDocumentSignerSettingsInfo> signers = new ArrayList<SignatureSignDocumentSignerSettingsInfo>();
                signers.add(signer);
                // Set signer
                requestBody.setSigners(signers);
                // Add document for sign to sign settings List
                List<SignatureSignDocumentDocumentSettingsInfo> documents = new ArrayList<SignatureSignDocumentDocumentSettingsInfo>();
                documents.add(document);
                // Set document
                requestBody.setDocuments(documents);
                // Make a request to Signature Api for sign document

                // Sign document using current user id and sign settings
                SignatureApi signatureApi = new SignatureApi();
                // Initialize API with base path
                signatureApi.setBasePath(credentials.getBasePath());
                // Request sample method
                SignatureSignDocumentResponse response = signatureApi.SignDocument(credentials.getClientId(), requestBody);
                // Check request status
                response = Utils.assertResponse(response);
                String jobId = response.getResult().getJobId();
                // Not good idea but need pause before get result of job
                Thread.sleep(5000);

                SignatureSignDocumentStatusResponse signResponse = signatureApi.GetSignDocumentStatus(credentials.getClientId(), jobId);
                signResponse = Utils.assertResponse(signResponse);
                String guid = signResponse.getResult().getDocuments().get(0).getDocumentId();
                guid = Utils.assertNotNull(guid);
                // Render view
                return ok(views.html.sample06.render(true, guid, form));
            } catch (Exception e) {
                return badRequest(views.html.sample06.render(false, null, form));
            }
        } else if (Utils.isGET(request())) {
            form = form.bind(session());
        }
        return ok(views.html.sample06.render(false, null, form));
    }
}