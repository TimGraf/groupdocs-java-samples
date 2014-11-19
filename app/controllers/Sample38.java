//###<i>This sample will show how to use <b>Upload</b> method from Storage Api to upload file to GroupDocs Storage </i>
package controllers;
//Import of necessary libraries

import com.groupdocs.sdk.api.AntApi;
import com.groupdocs.sdk.api.MgmtApi;
import com.groupdocs.sdk.api.SignatureApi;
import com.groupdocs.sdk.api.StorageApi;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.FileStream;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.*;
import common.Utils;
import models.Credentials;
import org.apache.commons.io.IOUtils;
import play.Play;
import play.data.Form;
import play.mvc.Http;
import play.mvc.Result;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;

public class Sample38 extends BaseController {
    //
    protected static Form<Credentials> form = Form.form(Credentials.class);

    public static Result index() {

        if (Utils.isPOST(request())) {
            Http.MultipartFormData body = request().body().asMultipartFormData();
            form = Form.form(Credentials.class).bindFromRequest();
            // Check errors
            if (form.hasErrors()) {
                return badRequest(views.html.sample38.render(false, null, null, form));
            }
            // Save credentials to session
            Credentials credentials = form.get();
            session().put("clientId", credentials.getClientId());
            session().put("privateKey", credentials.getPrivateKey());
            session().put("basePath", credentials.getBasePath());
            credentials.normalizeBasePath("https://api.groupdocs.com/v2.0");
            // Get request parameters
            String sourse = Utils.getFormValue(body, "sourse");
            String email = Utils.getFormValue(body, "email");
            String firstName = Utils.getFormValue(body, "firstName");
            String lastName = Utils.getFormValue(body, "lastName");
            // Initialize SDK with private key
            ApiInvoker.getInstance().setRequestSigner(
                    new GroupDocsRequestSigner(credentials.getPrivateKey()));

            try {

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
                    Http.MultipartFormData.FilePart file = body.getFile("local");
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
                // Create Annotation object
                AntApi antApi = new AntApi();
                antApi.setBasePath(credentials.getBasePath());
                //Create Storage Api object
                StorageApi storageApi = new StorageApi();
                storageApi.setBasePath(credentials.getBasePath());
                //Create MgmtApi object (this class allow manipulations with User account)
                MgmtApi mgmtApi = new MgmtApi();
                mgmtApi.setBasePath(credentials.getBasePath());

                //Get all users from accaunt
                GetAccountUsersResponse getAccountUsersResponse = mgmtApi.GetAccountUsers(credentials.getClientId());
                Utils.assertResponse(getAccountUsersResponse);
                //Loop for all users
                String userGuid = null;
                for (int i = 0; i < getAccountUsersResponse.getResult().getUsers().size(); i++) {
                    //Check whether there is a user with entered email
                    if (email.equals(getAccountUsersResponse.getResult().getUsers().get(i).getPrimary_email())) {
                        //Get user GUID
                        userGuid = getAccountUsersResponse.getResult().getUsers().get(i).getGuid();
                        break;
                    }
                }
                //Check is user with entered email was founded in GroupDocs account, if not user will be created
                if (userGuid == null) {
                    //###Create User info object
                    //Create User info object
                    UserInfo user = new UserInfo();
                    //Create Role info object
                    RoleInfo role = new RoleInfo();
                    //Set user role Id. Can be: 1 -  SysAdmin, 2 - Admin, 3 - User, 4 - Guest
                    role.setId(3d);
                    //Set user role name. Can be: SysAdmin, Admin, User, Guest
                    role.setName("User");
                    //Set nick name as entered first name
                    user.setNickname(firstName);
                    //Set first name as entered first name
                    user.setFirstname(firstName);
                    //Set last name as entered last name
                    user.setLastname(lastName);
                    user.setRoles(Arrays.asList(role));
                    //Set email as entered email
                    user.setPrimary_email(email);
                    //Creating of new user. $clientId - user id, $firstName - entered first name, $user - object with new user info
                    UpdateAccountUserResponse updateAccountUserResponse = mgmtApi.UpdateAccountUser(credentials.getClientId(), email, user);
                    //Check the result of the request
                    Utils.assertResponse(updateAccountUserResponse);
                    //Get user GUID
                    userGuid = updateAccountUserResponse.getResult().getGuid();
                }
                //Get all collaborators for current document
                GetCollaboratorsResponse getCollaboratorsResponse = antApi.GetAnnotationCollaborators(credentials.getClientId(), guid);
                Utils.assertResponse(getCollaboratorsResponse);
//                    //Loop for checking all collaborators
//                    for (int n = 0; n < getCollaboratorsResponse.getResult().getCollaborators().size(); n++) {
//                        //Check is user with entered email already in collaborators
//                        if (userGuid.equals(getCollaboratorsResponse.getResult().getCollaborators().get(n).getGuid())) {
//                            //Add user GUID as "uid" parameter to the iframe URL
//                            url = url + "?uid=" + userGuid;
//                            //Sign iframe URL
//                            url = $signer->signUrl($url);
//                            break;
//                        }
//                    }
                //Add user as collaborators for the document
                SetCollaboratorsResponse setCollaboratorsResponse = antApi.SetAnnotationCollaborators(credentials.getClientId(), guid, "v2.0", Arrays.asList(email));
                Utils.assertResponse(setCollaboratorsResponse);

                String server = credentials.getBasePath().substring(0, credentials.getBasePath().indexOf(".com") + 4).replace("api", "apps");
                // https://apps.groupdocs.com/document-annotation2/embed/453d79b8ab6f5b7b9c8bcd63c5675961430707da41296b72f0fb42b3102053fa?uid=88932effe9df5881&signature=h4kkqUuFfim0UCnJEque7CDx1n8
                String url = server + "/document-annotation2/embed/" + guid + "?uid=" + userGuid;
                url = new GroupDocsRequestSigner(credentials.getPrivateKey()).signUrl(url);

                // Result message with link to downloaded file for view in browser
                return ok(views.html.sample38.render(true, setCollaboratorsResponse.getResult().getCollaborators().get(0).getPrimary_email(), url, form));
            } catch (Exception e) {
                return badRequest(views.html.sample38.render(false, null, null, form));
            }

        } else if (Utils.isGET(request())) {
            form = form.bind(session());
        }
        return ok(views.html.sample38.render(false, null, null, form));
    }
}