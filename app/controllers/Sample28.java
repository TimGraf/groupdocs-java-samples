package controllers;

import com.groupdocs.sdk.api.AntApi;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.AnnotationInfo;
import com.groupdocs.sdk.model.ListAnnotationsResponse;
import common.Utils;
import models.Credentials;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

/**
 * Created with IntelliJ IDEA.
 * User: liosha
 * Date: 05.09.13
 * Time: 9:05
 * This sample will show how to delete all annotations from document using Java SDK
 */
public class Sample28 extends Controller {
    //
    protected static Form<Credentials> form = form(Credentials.class);

    public static Result index() {

        if (Utils.isPOST(request())) {
            form = form(Credentials.class).bindFromRequest();
            // Check errors
            if (form.hasErrors()) {
                return badRequest(views.html.sample28.render(false, null, form));
            }
            // Save credentials to session
            Credentials credentials = form.get();
            session().put("client_id", credentials.getClient_id());
            session().put("private_key", credentials.getPrivate_key());
            session().put("server_type", credentials.getServer_type());
            // Get request parameters
            Http.MultipartFormData body = request().body().asMultipartFormData();
            String guid = Utils.getFormValue(body, "fileId");
            // Initialize SDK with private key
            ApiInvoker.getInstance().setRequestSigner(
                    new GroupDocsRequestSigner(credentials.getPrivate_key()));

            try {
                //
                guid = Utils.assertNotNull(guid);
                // Create Annotation object
                AntApi antApi = new AntApi();
                // Initialize API with base path
                antApi.setBasePath(credentials.getServer_type());
                // Make a request to Annotation API using clientId and fileId
                ListAnnotationsResponse listAnnotationsResponse = antApi.ListAnnotations(credentials.getClient_id(), guid);
                listAnnotationsResponse = Utils.assertResponse(listAnnotationsResponse);
                for (AnnotationInfo annotationInfo : listAnnotationsResponse.getResult().getAnnotations()){
                    antApi.DeleteAnnotation(credentials.getClient_id(), annotationInfo.getGuid());
                }
                String server = credentials.getServer_type().substring(0, credentials.getServer_type().indexOf(".com") + 4).replace("api", "apps");
                String frameUrl = server + "/document-viewer/embed/" + guid;
                // Render view
                return ok(views.html.sample28.render(true, frameUrl, form));
            } catch (Exception e) {
                return badRequest(views.html.sample28.render(false, null, form));
            }
        } else if (Utils.isGET(request())) {
            form = form.bind(session());
            session().put("server_type", "https://api.groupdocs.com/v2.0");
        }
        return ok(views.html.sample28.render(false, null, form));
    }
}
