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
    protected static Form<Credentials> form = form(Credentials.class);

    public static Result index() {

        if (Utils.isPOST(request())) {
            form = form(Credentials.class).bindFromRequest();
            // Check errors
            if (form.hasErrors()) {
                return badRequest(views.html.sample06.render(false, null, form));
            }
            // Save credentials to session
            Credentials credentials = form.get();
            session().put("client_id", credentials.getClient_id());
            session().put("private_key", credentials.getPrivate_key());
            session().put("server_type", credentials.getServer_type());
            // Get request parameters
            MultipartFormData body = request().body().asMultipartFormData();
            FilePart fi_document = body.getFile("fi_document");
            FilePart fi_signature = body.getFile("fi_signature");
            String signerName = "GroupDocs Signer";
            // Initialize SDK with private key
            ApiInvoker.getInstance().setRequestSigner(
                    new GroupDocsRequestSigner(credentials.getPrivate_key()));

            try {
                fi_document = Utils.assertNotNull(fi_document);
                fi_signature = Utils.assertNotNull(fi_signature);

                // Read document to sign from URL
                String base64file = MimeUtils.readAsDataURL(fi_document.getFile(), fi_document.getContentType());
                //Read signature file from URL
                String base64signature = MimeUtils.readAsDataURL(fi_signature.getFile(), fi_signature.getContentType());
                // Set sign settings
                SignatureSignDocumentDocumentSettings document = new SignatureSignDocumentDocumentSettings();
                document.setName(fi_document.getFilename());
                document.setData(base64file);
                // Create SignatureSignDocumentSignerSettings object
                SignatureSignDocumentSignerSettings signer = new SignatureSignDocumentSignerSettings();
                signer.setPlaceSignatureOn("");
                signer.setName(signerName);
                signer.setData(base64signature);
                signer.setHeight(40d);
                signer.setWidth(100d);
                signer.setTop(0.83319);
                signer.setLeft(0.72171);
                // Make request to sign settings
                SignatureSignDocumentSettings requestBody = new SignatureSignDocumentSettings();
                // Add signer to sign settings List
                List<SignatureSignDocumentSignerSettings> signers = new ArrayList<SignatureSignDocumentSignerSettings>();
                signers.add(signer);
                // Set signer
                requestBody.setSigners(signers);
                // Add document for sign to sign settings List
                List<SignatureSignDocumentDocumentSettings> documents = new ArrayList<SignatureSignDocumentDocumentSettings>();
                documents.add(document);
                // Set document
                requestBody.setDocuments(documents);
                // Make a request to Signature Api for sign document

                // Sign document using current user id and sign settings
                SignatureApi signatureApi = new SignatureApi();
                // Initialize API with base path
                signatureApi.setBasePath(credentials.getServer_type());
                // Request sample method
                SignatureSignDocumentResponse response = signatureApi.SignDocument(credentials.getClient_id(), requestBody);
                // Check request status
                response = Utils.assertResponse(response);
                String jobId = response.getResult().getJobId();
                // Not good idea but need pause before get result of job
                Thread.sleep(5000);

                SignatureSignDocumentStatusResponse signResponse = signatureApi.GetSignDocumentStatus(credentials.getClient_id(), jobId);
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
            session().put("server_type", "https://api.groupdocs.com/v2.0");
        }
        return ok(views.html.sample06.render(false, null, form));
    }
}