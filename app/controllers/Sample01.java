//###<i>This sample will show how to use <b>Signer object</b> to be authorized at GroupDocs and how to get GroupDocs user infromation using Java SDK</i>

package controllers;
//Import of necessary libraries

import com.groupdocs.sdk.api.MgmtApi;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.UserInfo;
import com.groupdocs.sdk.model.UserInfoResponse;
import common.Utils;
import models.Credentials;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

public class Sample01 extends Controller {
    //
    protected static Form<Credentials> form = Form.form(Credentials.class);

    public static Result index() {

        if (Utils.isPOST(request())) {
            form = Form.form(Credentials.class).bindFromRequest();
            // Check errors
            if (form.hasErrors()) {
                return badRequest(views.html.sample01.render(false, null, form));
            }
            // Save credentials to session
            Credentials credentials = form.get();
            session().put("clientId", credentials.getClientId());
            session().put("privateKey", credentials.getPrivateKey());
            session().put("basePath", credentials.getBasePath());
            credentials.normalizeBasePath("https://api.groupdocs.com/v2.0");

            // Initialize SDK with private key
            ApiInvoker.getInstance().setRequestSigner(
                    new GroupDocsRequestSigner(credentials.getPrivateKey()));

            try {
                //
                MgmtApi mgmtApi = new MgmtApi();
                // Initialize API with base path
                mgmtApi.setBasePath(credentials.getBasePath());
                // Call sample method
                UserInfoResponse userInfoResponse = mgmtApi.GetUserProfile(credentials.getClientId());
                // Check response status
                userInfoResponse = Utils.assertResponse(userInfoResponse);
                //
                UserInfo userInfo = userInfoResponse.getResult().getUser();
                // Render view
                return ok(views.html.sample01.render(true, userInfo, form));
            } catch (Exception e) {
                return badRequest(views.html.sample01.render(false, null, form));
            }
        } else if (Utils.isGET(request())) {
            form = form.bind(session());
        }
        return ok(views.html.sample01.render(false, null, form));
    }
}