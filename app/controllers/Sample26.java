//###<i>This sample will show how to use <b>Login</b> to be authorized at GroupDocs and how to get GroupDocs user infromation using PHP SDK</i>

package controllers;
//Import of necessary libraries

import com.groupdocs.sdk.api.SharedApi;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.UserInfo;
import com.groupdocs.sdk.model.UserInfoResponse;
import common.Utils;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

public class Sample26 extends Controller {
    //
    protected static Form form = form();

    public static Result index() {

        if (Utils.isPOST(request())) {
            form = form().bindFromRequest();
            // Check errors
            if (form.hasErrors()) {
                return badRequest(views.html.sample26.render(false, null, form));
            }

            // Get request parameters
            Http.MultipartFormData body = request().body().asMultipartFormData();
            String login = Utils.getFormValue(body, "login");
            String password = Utils.getFormValue(body, "password");
            String server_type = Utils.getFormValue(body, "server_type");

            session().put("server_type", server_type);

            // Initialize SDK with private key
            ApiInvoker.getInstance().setRequestSigner(
                    new GroupDocsRequestSigner("any_value"));
            try {
                // Create SharedApi object
                SharedApi sharedApi = new SharedApi();
                // Initialize API with base path
                sharedApi.setBasePath(server_type);
                // Login to the GroupDocs
                UserInfoResponse userInfoResponse = sharedApi.LoginUser(login, password);
                // Check result status
                userInfoResponse = Utils.assertResponse(userInfoResponse);
                //
                UserInfo userInfo = userInfoResponse.getResult().getUser();
                // Render view
                return ok(views.html.sample26.render(true, userInfo, form));
            } catch (Exception e) {
                return badRequest(views.html.sample26.render(false, null, form));
            }
        } else if (Utils.isGET(request())) {
            form = form.bind(session());
            session().put("server_type", "https://api.groupdocs.com/v2.0");
        }
        return ok(views.html.sample26.render(false, null, form));
    }
}