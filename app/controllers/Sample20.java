 //###<i>This sample will show how to Get Compare Change list for document</i>
package controllers;
//Import of necessary libraries

 import com.groupdocs.sdk.api.ComparisonApi;
 import com.groupdocs.sdk.api.StorageApi;
 import com.groupdocs.sdk.common.ApiException;
 import com.groupdocs.sdk.common.ApiInvoker;
 import com.groupdocs.sdk.common.GroupDocsRequestSigner;
 import com.groupdocs.sdk.model.ChangesResponse;
 import com.groupdocs.sdk.model.UploadResponse;
 import common.Utils;
 import models.Credentials;
 import org.apache.commons.lang3.StringUtils;
 import play.data.Form;
 import play.mvc.Controller;
 import play.mvc.Http;
 import play.mvc.Result;
 import scala.actors.threadpool.Arrays;

 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;

public class Sample20 extends Controller {
    //
    protected static Form<Credentials> form = form(Credentials.class);

    public static Result index() {

        if (Utils.isPOST(request())) {
            form = form.bindFromRequest();
            // Check errors
            if (form.hasErrors()) {
                return badRequest(views.html.sample20.render(false, null, form));
            }
            // Save credentials to session
            Credentials credentials = form.get();
            session().put("client_id", credentials.getClient_id());
            session().put("private_key", credentials.getPrivate_key());
            session().put("server_type", credentials.getServer_type());
            // Get request parameters
            Http.MultipartFormData body = request().body().asMultipartFormData();
            String file_id = Utils.getFormValue(body, "resultFileId");
            // Initialize SDK with private key
            ApiInvoker.getInstance().setRequestSigner(
                    new GroupDocsRequestSigner(credentials.getPrivate_key()));

            try {
                //
                file_id = Utils.assertNotNull(file_id);
                // Create StorageApi object
                ComparisonApi api = new ComparisonApi();
                // Initialize API with base path
                api.setBasePath(credentials.getServer_type());
                ChangesResponse changesResponse = api.GetChanges(credentials.getClient_id(), file_id);
                // Check response status
                changesResponse = Utils.assertResponse(changesResponse);
                // Render view
                return ok(views.html.sample20.render(true, changesResponse.getResult(), form));
            } catch (Exception e) {
                return badRequest(views.html.sample20.render(false, null, form));
            }
        } else if (Utils.isGET(request())) {
            form = form.bind(session());
            session().put("server_type", "https://api.groupdocs.com/v2.0");
        }
        return ok(views.html.sample20.render(false, null, form));
    }
}