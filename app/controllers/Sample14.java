//###<i>This sample will show how to use <b>GetFolderSharers</b> method from Storage Api to get folders sharers in GroupDocs Storage </i>
package controllers;
//Import of necessary libraries

import com.groupdocs.sdk.api.DocApi;
import com.groupdocs.sdk.api.StorageApi;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.FileSystemFolder;
import com.groupdocs.sdk.model.ListEntitiesResponse;
import com.groupdocs.sdk.model.SharedUsersResponse;
import common.Utils;
import models.Credentials;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.util.List;

public class Sample14 extends Controller {
    //
    protected static Form<Credentials> form = Form.form(Credentials.class);

    public static Result index() {

        if (Utils.isPOST(request())) {
            form = Form.form(Credentials.class).bindFromRequest();
            // Check errors
            if (form.hasErrors()) {
                return badRequest(views.html.sample14.render(false, null, form));
            }
            // Save credentials to session
            Credentials credentials = form.get();
            session().put("client_id", credentials.getClient_id());
            session().put("private_key", credentials.getPrivate_key());
            session().put("server_type", credentials.getServer_type());
            // Get request parameters
            Http.MultipartFormData body = request().body().asMultipartFormData();
            String path = Utils.getFormValue(body, "path");
            // Initialize SDK with private key
            ApiInvoker.getInstance().setRequestSigner(
                    new GroupDocsRequestSigner(credentials.getPrivate_key()));

            try {
                // Create Storage Api object
                StorageApi storageApi = new StorageApi();
                // Initialize API with base path
                storageApi.setBasePath(credentials.getServer_type());
                String folderId = "";
                // Make request to Storage Api to get list of elements in the storage
                ListEntitiesResponse listResponse = storageApi.ListEntities(credentials.getClient_id(), "", null, null, null, null, null, null, null);
                // Check request status
                listResponse = Utils.assertResponse(listResponse);
                // Get folders info from storage
                List<FileSystemFolder> folders = listResponse.getResult().getFolders();
                // Get folder ID by folder name
                for (FileSystemFolder folder : folders) {
                    // Check if folder name equal to entered folder name
                    if (path.equals(folder.getName())) {
                        // Get folder id
                        folderId = folder.getId().toString();
                        break;
                    }
                }
                // Create Doc Api object and get document metadata
                DocApi metadata = new DocApi();
                // Initialize API with base path
                metadata.setBasePath(credentials.getServer_type());
                // Make request to Doc Api to get folder sharers
                SharedUsersResponse sharedUsersResponse = metadata.GetFolderSharers(credentials.getClient_id(), folderId);
                // Check request status
                sharedUsersResponse = Utils.assertResponse(sharedUsersResponse);
                // Render view
                return ok(views.html.sample14.render(true, sharedUsersResponse.getResult().getShared_users(), form));
            } catch (Exception e) {
                return badRequest(views.html.sample14.render(false, null, form));
            }
        } else if (Utils.isGET(request())) {
            form = form.bind(session());
            session().put("server_type", "https://api.groupdocs.com/v2.0");
        }
        return ok(views.html.sample14.render(false, null, form));
    }
}