//###<i>This sample will show how to use <b>Upload</b> method from Storage Api to upload file to GroupDocs Storage </i>
package controllers;
//Import of necessary libraries

import com.groupdocs.sdk.api.SignatureApi;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.FileStream;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.SignatureEnvelopeDocumentsResponse;
import common.Utils;
import models.Credentials;
import org.apache.commons.io.IOUtils;
import play.Play;
import play.data.Form;
import play.mvc.Http;
import play.mvc.Result;

import java.io.File;
import java.io.FileOutputStream;

public class Sample36 extends BaseController {
    //
    protected static Form<Credentials> form = Form.form(Credentials.class);

    public static Result index() {

        if (Utils.isPOST(request())) {
            Http.MultipartFormData body = request().body().asMultipartFormData();
                form = Form.form(Credentials.class).bindFromRequest();
                // Check errors
                if (form.hasErrors()) {
                    return badRequest(views.html.sample36.render(false, null, form));
                }
                // Save credentials to session
                Credentials credentials = form.get();
                session().put("clientId", credentials.getClientId());
                session().put("privateKey", credentials.getPrivateKey());
                session().put("basePath", credentials.getBasePath());
                credentials.normalizeBasePath("https://api.groupdocs.com/v2.0");
                // Get request parameters
                String envelopeGuid = Utils.getFormValue(body, "envelopeGuid");
                // Initialize SDK with private key
                ApiInvoker.getInstance().setRequestSigner(
                        new GroupDocsRequestSigner(credentials.getPrivateKey()));

                try {
                    // Make a request to Storage Api for downloading file
                    // Obtaining file stream of downloading file and definition of folder where to download file
                    // Set path for local storage were to download file
                    String currentDirectory = Play.application().path().getAbsolutePath();
                    File downloadPath = new File(currentDirectory + File.separator + "assets");
                    downloadPath.mkdir();
                    SignatureApi signatureApi = new SignatureApi();
                    signatureApi.setBasePath(credentials.getBasePath());
                    // Get envelop info
                    SignatureEnvelopeDocumentsResponse signatureEnvelopeDocumentsResponse = signatureApi.GetSignatureEnvelopeDocuments(credentials.getClientId(), envelopeGuid);
                    Utils.assertResponse(signatureEnvelopeDocumentsResponse);
                    // Get document name from envelop info
                    String documentName = signatureEnvelopeDocumentsResponse.getResult().getDocuments().get(0).getName();
                    // Get file stream for signed document from envelop
                    FileStream fileStream = signatureApi.GetSignedEnvelopeDocuments(credentials.getClientId(), envelopeGuid);
                    if (fileStream == null) {
                        return badRequest(views.html.sample36.render(false, null, form));
                    }
                    String filePath = downloadPath + File.separator + documentName;
                    // Write file stream to file
                    FileOutputStream fileOutputStream = new FileOutputStream(filePath);
                    IOUtils.copy(fileStream.getInputStream(), fileOutputStream);
                    fileOutputStream.close();
                    // Result message with link to downloaded file for view in browser
                    return ok(views.html.sample36.render(true, documentName, form));
                } catch (Exception e) {
                    return badRequest(views.html.sample36.render(false, null, form));
                }

        } else if (Utils.isGET(request())) {
            form = form.bind(session());
        }
        return ok(views.html.sample36.render(false, null, form));
    }
}