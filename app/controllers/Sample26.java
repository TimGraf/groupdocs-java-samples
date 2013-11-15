//###<i>This sample will show how to use <b>Login</b> to be authorized at GroupDocs and how to get GroupDocs user infromation using PHP SDK</i>

package controllers;
//Import of necessary libraries

import com.groupdocs.sdk.api.SharedApi;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.UserInfo;
import com.groupdocs.sdk.model.UserInfoResponse;
import common.Utils;
import org.apache.commons.lang3.StringUtils;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

public class Sample26 extends Controller {
    //
    protected static Form form = Form.form();

    public static Result index() {

        if (Utils.isPOST(request())) {
            form = Form.form().bindFromRequest();
            // Check errors
            if (form.hasErrors()) {
                return badRequest(views.html.sample26.render(false, null, form));
            }

            // Get request parameters
            Http.MultipartFormData body = request().body().asMultipartFormData();
            String login = Utils.getFormValue(body, "login");
            String password = Utils.getFormValue(body, "password");
            String basePath = Utils.getFormValue(body, "basePath");
            basePath = StringUtils.isEmpty(basePath) ? "https://api.groupdocs.com/v2.0" : basePath;

            session().put("basePath", basePath);

            // Initialize SDK with private key
            ApiInvoker.getInstance().setRequestSigner(
                    new GroupDocsRequestSigner("any_value"));
            try {
                // Create SharedApi object
                SharedApi sharedApi = new SharedApi();
                // Initialize API with base path
                sharedApi.setBasePath(basePath);
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
        }
        return ok(views.html.sample26.render(false, null, form));
    }
}