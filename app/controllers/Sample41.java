//###<i>This sample will show how to use <b>MoveFile</b> method from Storage Api to copy/move a file in GroupDocs Storage </i>
package controllers;
//Import of necessary libraries

import com.groupdocs.sdk.api.AntApi;
import com.groupdocs.sdk.api.MergeApi;
import com.groupdocs.sdk.api.MgmtApi;
import com.groupdocs.sdk.api.StorageApi;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.FileStream;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.*;
import common.Utils;
import models.Credentials;
import org.apache.commons.lang3.StringUtils;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;



public class Sample41 extends BaseController {
    public static String USER_INFO_FILE = "UserInfo_sample41.tmp";
    //
    protected static Form<Credentials> form = Form.form(Credentials.class);

    public static Result index() {

        if (Utils.isPOST(request())) {
            form = Form.form(Credentials.class).bindFromRequest();
            // Check errors
            if (form.hasErrors()) {
                return badRequest(views.html.sample41.render(false, null, form));
            }
            // Save credentials to session
            Credentials credentials = form.get();
            session().put("clientId", credentials.getClientId());
            session().put("privateKey", credentials.getPrivateKey());
            session().put("basePath", credentials.getBasePath());
            credentials.normalizeBasePath("https://api.groupdocs.com/v2.0");
            // Get request parameters
            Http.MultipartFormData body = request().body().asMultipartFormData();
            String sourse = Utils.getFormValue(body.asFormUrlEncoded(), "sourse");
            String firstEmail = Utils.getFormValue(body.asFormUrlEncoded(), "email");
            String secondEmail = Utils.getFormValue(body.asFormUrlEncoded(), "secondEmail");
            String callback = Utils.getFormValue(body, "callbackUrl");
            String basePath = credentials.getBasePath();
            // Initialize SDK with private key
            ApiInvoker.getInstance().setRequestSigner(
                    new GroupDocsRequestSigner(credentials.getPrivateKey()));

            try {
                //
                String guid = null;
                //
                if ("guid".equals(sourse)) { // File GUID
                    guid = Utils.getFormValue(body, "guid");
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
                    UploadResponse uploadResponse = storageApi.Upload(credentials.getClientId(), file.getFilename(), "uploaded", "", false, new FileStream(is));
                    // Check response status
                    uploadResponse = Utils.assertResponse(uploadResponse);
                    guid = uploadResponse.getResult().getGuid();
                }
                guid = Utils.assertNotNull(guid);
                // Create Annotation api object
                AntApi annotationApi = new AntApi();
                MergeApi mergeApi = new MergeApi();
                // Initialize API with base path
                annotationApi.setBasePath(basePath);
                if (callback != null){
                    //Set file sesion callback - will be trigered when user add, remove or edit commit for annotation
                    SetSessionCallbackUrlResponse setSessionCallbackUrlResponse = annotationApi.SetSessionCallbackUrl(credentials.getClientId(), guid, callback);
                    //Check the result of the request
                    setSessionCallbackUrlResponse = Utils.assertResponse(setSessionCallbackUrlResponse);
                }

                String annotationUrl = apiPath2framePath(mergeApi.getBasePath(), "/document-annotation2/embed/" + guid);

                // Create Menegment api object
                MgmtApi mgmtApi = new MgmtApi();
                // Initialize API with base path
                mgmtApi.setBasePath(basePath);

                ArrayList<String> collaborators = new ArrayList<String>();
                ArrayList<String> emails = new ArrayList<String>();
                emails.add(firstEmail);
                emails.add(secondEmail);
                String userGuid = null;
                // Get all users from accaunt
                GetAccountUsersResponse getAccountUsersResponse = mgmtApi.GetAccountUsers(credentials.getClientId());
                getAccountUsersResponse = Utils.assertResponse(getAccountUsersResponse);

                if (getAccountUsersResponse.getResult().getUsers() != null) {
                    //Loop for all users
                    for (String email : emails) {
                        int i = 0;
                       //Loop to get user GUID if user with same email already exist
                        while (i < getAccountUsersResponse.getResult().getUsers().size() ) {
                            List<UserInfo> users = getAccountUsersResponse.getResult().getUsers();
                            String primaryEmail = users.get(i).getPrimary_email();
                            //Check whether there is a user with entered email
                            if (primaryEmail.equals(email)) {
                                //Get user GUID
                                userGuid = users.get(i).getGuid();
                                break;
                            }
                            i++;
                        }
                        //Check is user with entered email was founded in GroupDocs account, if not user will be created
                        if (userGuid == null) {
                            //Create User info object
                            UserInfo userInfo = new UserInfo();
                            //Create Role info object
                            RoleInfo roleInfo = new RoleInfo();
                            //Set user role Id. Can be: 1 - SysAdmin, 2 - Admin, 3 - User, 4 - Guest
                            roleInfo.setId(3.0);
                            //Set user role name. Can be: SysAdmin, Admin, User, Guest
                            roleInfo.setName("User");
                            //Create array of roles.
                            List<RoleInfo> roles = new ArrayList<RoleInfo>();
                            roles.add(roleInfo);
                            //Set first name as entered first name
                            userInfo.setFirstname(email);
                            //Set last name as entered last name
                            userInfo.setLastname(email);
                            //Set email as entered email
                            userInfo.setPrimary_email(email);
                            userInfo.setRoles(roles);
                            //Creating of new user. $clientId - user id, $firstName - entered first name, $user - object with new user info
                            UpdateAccountUserResponse updateAccountUserResponse = mgmtApi.UpdateAccountUser(credentials.getClientId(),email, userInfo);
                            //Check the result of the request
                            updateAccountUserResponse = Utils.assertResponse(updateAccountUserResponse);
                            //Get user GUID
                            userGuid = updateAccountUserResponse.getResult().getGuid();
                        }
                        //Get all collaborators for current document
                        GetCollaboratorsResponse getCollaboratorsResponse = annotationApi.GetAnnotationCollaborators(credentials.getClientId(), guid);
                        //Check the result of the request
                        getCollaboratorsResponse = Utils.assertResponse(getCollaboratorsResponse);
                        int n = 0;
                        for (ReviewerInfo collaborator : getCollaboratorsResponse.getResult().getCollaborators()) {
                            if (collaborator.getPrimary_email().equals(email)){
                                collaborators.set(n, collaborator.getGuid());
                                n++;
                            }
                        }

                    }
                    if (collaborators.size() < 2){
                        SetCollaboratorsResponse setCollaboratorsResponse = annotationApi.SetAnnotationCollaborators(credentials.getClientId(), guid, "v2.0", emails);
                        //Check the result of the request
                        setCollaboratorsResponse = Utils.assertResponse(setCollaboratorsResponse);
                    }
                }
                annotationUrl = annotationUrl + "?uid=" + userGuid;
                String signedUrl = signUrl(credentials.getPrivateKey(), annotationUrl);
                if (!StringUtils.isEmpty(callback)) {
                    FileOutputStream fileOutputStream = new FileOutputStream(USER_INFO_FILE);
                    DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream);

                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(credentials.getClientId());
                    stringBuilder.append("|");
                    stringBuilder.append(credentials.getPrivateKey());
                    stringBuilder.append("|");
                    stringBuilder.append(credentials.getBasePath());

                    dataOutputStream.writeUTF(stringBuilder.toString());

                    dataOutputStream.flush();
                    fileOutputStream.close();
                }

                // Render view
                return ok(views.html.sample41.render(true, signedUrl, form));
            } catch (Exception e) {
                return badRequest(views.html.sample41.render(false, null, form));
            }
        } else if (Utils.isGET(request())) {
            form = form.bind(session());
        }
        return ok(views.html.sample41.render(false, null, form));
    }

}