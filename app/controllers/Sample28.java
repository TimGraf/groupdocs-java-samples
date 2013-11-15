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
    protected static Form<Credentials> form = Form.form(Credentials.class);

    public static Result index() {

        if (Utils.isPOST(request())) {
            form = Form.form(Credentials.class).bindFromRequest();
            // Check errors
            if (form.hasErrors()) {
                return badRequest(views.html.sample28.render(false, null, form));
            }
            // Save credentials to session
            Credentials credentials = form.get();
            session().put("clientId", credentials.getClientId());
            session().put("privateKey", credentials.getPrivateKey());
            session().put("basePath", credentials.getBasePath());
            credentials.normalizeBasePath("https://api.groupdocs.com/v2.0");
            // Get request parameters
            Http.MultipartFormData body = request().body().asMultipartFormData();
            String guid = Utils.getFormValue(body, "fileId");
            // Initialize SDK with private key
            ApiInvoker.getInstance().setRequestSigner(
                    new GroupDocsRequestSigner(credentials.getPrivateKey()));

            try {
                //
                guid = Utils.assertNotNull(guid);
                // Create Annotation object
                AntApi antApi = new AntApi();
                // Initialize API with base path
                antApi.setBasePath(credentials.getBasePath());
                // Make a request to Annotation API using clientId and fileId
                ListAnnotationsResponse listAnnotationsResponse = antApi.ListAnnotations(credentials.getClientId(), guid);
                listAnnotationsResponse = Utils.assertResponse(listAnnotationsResponse);
                for (AnnotationInfo annotationInfo : listAnnotationsResponse.getResult().getAnnotations()){
                    antApi.DeleteAnnotation(credentials.getClientId(), annotationInfo.getGuid());
                }
                String server = credentials.getBasePath().substring(0, credentials.getBasePath().indexOf(".com") + 4).replace("api", "apps");
                String frameUrl = server + "/document-viewer/embed/" + guid;
                // Render view
                return ok(views.html.sample28.render(true, frameUrl, form));
            } catch (Exception e) {
                return badRequest(views.html.sample28.render(false, null, form));
            }
        } else if (Utils.isGET(request())) {
            form = form.bind(session());
        }
        return ok(views.html.sample28.render(false, null, form));
    }
}
