package controllers;

import common.Utils;
import models.Credentials;
import org.apache.commons.lang3.StringUtils;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: work
 * Date: 05.09.13
 * Time: 9:51
 * This sample will show how to use Filepicker.io to upload document and get it's URL
 */
public class Sample29 extends Controller {
    //
    protected static Form<Credentials> form = Form.form(Credentials.class);

    public static Result index() {

        if (Utils.isPOST(request())) {
            form = Form.form(Credentials.class).bindFromRequest();

            Map<String, String[]> body = request().body().asFormUrlEncoded();
            String url = body.get("url")[0];
            String clientId = body.get("clientId")[0];
            String basePath = body.get("basePath")[0];
            basePath = StringUtils.isEmpty(basePath) ? "https://api.groupdocs.com/v2.0" : basePath;

            try {
                //
                String server = basePath.substring(0, basePath.indexOf(".com") + 4).replace("api", "apps");
                String frameUrl = server + "/document-viewer/embed?url=" + url + "&user_id=" + clientId;
                // Render view
                return ok("{ \"iframe\": \"" + frameUrl + "\" }");

            } catch (Exception e) {
                return badRequest(views.html.sample29.render(false, null, form));
            }
        } else if (Utils.isGET(request())) {
            form = form.bind(session());
        }
        return ok(views.html.sample29.render(false, null, form));
    }
}
