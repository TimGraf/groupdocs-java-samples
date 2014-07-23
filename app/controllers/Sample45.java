package controllers;


import com.groupdocs.sdk.api.*;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.FileStream;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.*;
import common.Utils;
import models.Credentials;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import com.google.common.base.Strings;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Sample45 extends Controller {
    //
    protected static Form<Credentials> form = Form.form(Credentials.class);

    public static Result index() {

        if (Utils.isPOST(request())) {
            form = Form.form(Credentials.class).bindFromRequest();
            // Check errors
            if (form.hasErrors()) {
                return badRequest(views.html.sample45.render(false, null, form));
            }
            // Save credentials to session
            Credentials credentials = form.get();
            session().put("clientId", credentials.getClientId());
            session().put("privateKey", credentials.getPrivateKey());
            session().put("basePath", credentials.getBasePath());
            credentials.normalizeBasePath("https://api.groupdocs.com/v2.0");
            // Get request parameters
            Http.MultipartFormData body = request().body().asMultipartFormData();
            String folderName = Utils.getFormValue(body, "folderName");
            String fileName = Utils.getFormValue(body, "fileName");

            String fileId = null;
            // Initialize SDK with private key
            ApiInvoker.getInstance().setRequestSigner(
                    new GroupDocsRequestSigner(credentials.getPrivateKey()));
            try {
                //
                StorageApi storageApi = new StorageApi();
                // Initialize API with base path
                storageApi.setBasePath(credentials.getBasePath());
                String path = null;
                // Check emptiness path string
                path = (Strings.isNullOrEmpty(folderName))?"":folderName + '/';
                // Call sample method
                ListEntitiesResponse listEntitiesResponse = storageApi.ListEntities(credentials.getClientId(), path, null, null, null, null, null, null, null);
                // Check response status
                listEntitiesResponse = Utils.assertResponse(listEntitiesResponse);
                // Get document Guid
                for (FileSystemDocument document : listEntitiesResponse.getResult().getFiles()) {
                    if (document.getName().equals(fileName)) {
                        fileId = document.getGuid();
                        break;
                    }
                }
                //
                DocApi documentApi = new DocApi();
                // Initialize API with base path
                documentApi.setBasePath(credentials.getBasePath());
                // Call sample method
                GetDocumentInfoResponse documentMetadata = documentApi.GetDocumentMetadata(credentials.getClientId(), fileId);
                // Check response status
                documentMetadata = Utils.assertResponse(documentMetadata);

                return ok(views.html.sample45.render(true, documentMetadata.getResult(), form));
            } catch (Exception e) {
                return badRequest(views.html.sample45.render(false, null, form));
            }
        } else if (Utils.isGET(request())) {
            form = form.bind(session());
        }
        return ok(views.html.sample45.render(false, null, form));
    }
}