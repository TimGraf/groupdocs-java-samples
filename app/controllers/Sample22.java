//###<i>This sample will show how to use <b>SetAnnotationCollaborators</b> method from Annotation Api to set collaborator for document</i>
package controllers;
//Import of necessary libraries

import com.groupdocs.sdk.api.AntApi;
import com.groupdocs.sdk.api.MgmtApi;
import com.groupdocs.sdk.api.StorageApi;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.FileStream;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.*;
import common.Utils;
import models.Credentials;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class Sample22 extends Controller {
    //
    protected static Form<Credentials> form = Form.form(Credentials.class);

    public static Result index() {

        if (Utils.isPOST(request())) {
            form = Form.form(Credentials.class).bindFromRequest();
            // Check errors
            if (form.hasErrors()) {
                return badRequest(views.html.sample22.render(false, null, form));
            }
            // Save credentials to session
            Credentials credentials = form.get();
            session().put("clientId", credentials.getClientId());
            session().put("privateKey", credentials.getPrivateKey());
            session().put("basePath", credentials.getBasePath());
            credentials.normalizeBasePath("https://api.groupdocs.com/v2.0");
            // Get request parameters
            Http.MultipartFormData body = request().body().asMultipartFormData();
            String sourse = Utils.getFormValue(body, "sourse");
            String email = Utils.getFormValue(body, "email");
            String firstName = Utils.getFormValue(body, "firstName");
            String lastName = Utils.getFormValue(body, "lastName");
            String callback = Utils.getFormValue(body, "callbackUrl");
            String basePath = credentials.getBasePath();
            // Initialize SDK with private key
            ApiInvoker.getInstance().setRequestSigner(
                    new GroupDocsRequestSigner(credentials.getPrivateKey()));

            try {
                //
                email = Utils.assertNotNull(email);
                firstName = Utils.assertNotNull(firstName);
                lastName = Utils.assertNotNull(lastName);
                basePath = Utils.assertNotNull(basePath);
                //
                String guid = null;
                //
                if ("guid".equals(sourse)) { // File GUID
                    guid = Utils.getFormValue(body, "fileId");
                } else if ("url".equals(sourse)) { // Upload file fron URL
                    String url = Utils.getFormValue(body, "url");
                    StorageApi storageApi = new StorageApi();
                    // Initialize API with base path
                    storageApi.setBasePath(credentials.getBasePath());
                    UploadResponse uploadResponse = storageApi.UploadWeb(credentials.getClientId(), url);
                    // Check response status
                    uploadResponse = Utils.assertResponse(uploadResponse);
                    guid = uploadResponse.getResult().getGuid();
                } else if ("local".equals(sourse)) { // Upload local file
                    Http.MultipartFormData.FilePart file = body.getFile("file");
                    StorageApi storageApi = new StorageApi();
                    // Initialize API with base path
                    storageApi.setBasePath(credentials.getBasePath());
                    FileInputStream is = new FileInputStream(file.getFile());
                    UploadResponse uploadResponse = storageApi.Upload(credentials.getClientId(), file.getFilename(), "uploaded", "", 1, new FileStream(is));
                    // Check response status
                    uploadResponse = Utils.assertResponse(uploadResponse);
                    guid = uploadResponse.getResult().getGuid();
                }
                guid = Utils.assertNotNull(guid);
                // Create AntApi object
                MgmtApi mgmtApi = new MgmtApi();
                // Initialize API with base path
                mgmtApi.setBasePath(basePath);
                // Create User info object
                UserInfo user = new UserInfo();
                // Create Role info object
                RoleInfo role = new RoleInfo();
                // Create array of roles.
                List<RoleInfo> roleList = new ArrayList<RoleInfo>();
                // Set user role Id. Can be: 1 -  SysAdmin, 2 - Admin, 3 - User, 4 - Guest
                role.setId(3.0);
                // Set user role name. Can be: SysAdmin, Admin, User, Guest
                role.setName("User");
                // Add RoleInfo object to roles array
                roleList.add(role);
                user.setNickname(firstName);
                // Set first name as entered first name
                user.setFirstname(firstName);
                // Set last name as entered last name
                user.setLastname(lastName);
                user.setRoles(roleList);
                // Set email as entered email
                user.setPrimary_email(email);
                // Creating of new user. $clientId - user id, $firstName - entered first name, $user - object with new user info
                UpdateAccountUserResponse updateAccountUserResponse = mgmtApi.UpdateAccountUser(credentials.getClientId(), email, user);
                // Check response status
                updateAccountUserResponse = Utils.assertResponse(updateAccountUserResponse);
                // Create AntApi object
                AntApi ant = new AntApi();
                // Initialize API with base path
                ant.setBasePath(basePath);
                // Create List object
                List<String> emailList = new ArrayList<String>();
                // Add email to the list
                emailList.add(email);
                // Make request to Annotation api for setting collaborator for document
                SetCollaboratorsResponse response = ant.SetAnnotationCollaborators(credentials.getClientId(), guid, "v2.0", emailList);
                Utils.assertResponse(response);
                // Make request to Annotation api to receive all collaborators for entered file id
                GetCollaboratorsResponse getCollaborators = ant.GetAnnotationCollaborators(credentials.getClientId(), guid);
                // Set reviewers rights for new user.
                SetReviewerRightsResponse reviewerRightsResponse = ant.SetReviewerRights(updateAccountUserResponse.getResult().getGuid(), guid, getCollaborators.getResult().getCollaborators());
                reviewerRightsResponse = Utils.assertResponse(reviewerRightsResponse);
                // Check is callback entered
                callback = (callback == null) ? "" : callback;
                // Set callback url. CallBack work results you can see here: http://groupdocs-php-samples.herokuapp.com/callbacks/annotation_check_file
                SetSessionCallbackUrlResponse setCallBack = ant.SetSessionCallbackUrl(updateAccountUserResponse.getResult().getGuid(), guid, callback);
                // Generation of iframe URL using $pageImage->result->guid
                String server = credentials.getBasePath().substring(0, credentials.getBasePath().indexOf(".com") + 4).replace("api", "apps");
                String iframeUrl = server + "/document-annotation2/embed/" + guid + "?&uid=" + updateAccountUserResponse.getResult().getGuid() + "&download=true";
                // Render view
                return ok(views.html.sample22.render(true, iframeUrl, form));
            } catch (Exception e) {
                return badRequest(views.html.sample22.render(false, null, form));
            }
        } else if (Utils.isGET(request())) {
            form = form.bind(session());
        }
        return ok(views.html.sample22.render(false, null, form));
    }
}