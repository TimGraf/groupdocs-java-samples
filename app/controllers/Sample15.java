//###<i>This sample will show how to use <b>GetDocumentViews</b> method from Storage Api to get number of viewings for this user </i>
package controllers;
//Import of necessary libraries

import java.util.List;

import common.Utils;
import models.Credentials;

import org.apache.commons.lang3.StringUtils;

import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import scala.actors.threadpool.Arrays;

import com.groupdocs.sdk.api.DocApi;
import com.groupdocs.sdk.common.ApiException;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.DocumentViewsResponse;
import com.groupdocs.sdk.model.DocumentViewInfo;

public class Sample15 extends Controller {
    //
    protected static Form<Credentials> form = form(Credentials.class);

    public static Result index() {

        if (Utils.isPOST(request())) {
            form = form.bindFromRequest();
            // Check errors
            if (form.hasErrors()) {
                return badRequest(views.html.sample15.render(false, null, form));
            }
            // Save credentials to session
            Credentials credentials = form.get();
            session().put("client_id", credentials.getClient_id());
            session().put("private_key", credentials.getPrivate_key());
            session().put("server_type", credentials.getServer_type());
            // Get request parameters
            // Initialize SDK with private key
            ApiInvoker.getInstance().setRequestSigner(
                    new GroupDocsRequestSigner(credentials.getPrivate_key()));

            try {
                // Create Doc Api object and get document metadata
                DocApi docApi = new DocApi();
                // Initialize API with base path
                docApi.setBasePath(credentials.getServer_type());

                // Make request to get documents views
                DocumentViewsResponse documentViewsResponse = docApi.GetDocumentViews(credentials.getClient_id(), null, null);
                // Check request status
                documentViewsResponse = Utils.assertResponse(documentViewsResponse);

                // Render view
                return ok(views.html.sample15.render(true, documentViewsResponse.getResult().getViews(), form));
            } catch (Exception e) {
                return badRequest(views.html.sample15.render(false, null, form));
            }
        } else if (Utils.isGET(request())) {
            form = form.bind(session());
            session().put("server_type", "https://api.groupdocs.com/v2.0");
        }
        return ok(views.html.sample15.render(false, null, form));
    }
}