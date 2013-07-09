//###<i>This sample will show how to use <b>GetDocumentPagesImageUrls</b> method from Doc Api to return a URL representing a single page of a Document</i>
package controllers;
//Import of necessary libraries

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.groupdocs.sdk.api.StorageApi;
import com.groupdocs.sdk.common.FileStream;
import com.groupdocs.sdk.model.CompressResponse;
import com.groupdocs.sdk.model.UploadResponse;
import common.Utils;
import models.Credentials;

import org.apache.commons.lang3.StringUtils;

import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import scala.actors.threadpool.Arrays;

import com.groupdocs.sdk.api.DocApi;
import com.groupdocs.sdk.common.ApiException;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.ViewDocumentResponse;

public class Sample23 extends Controller {
    //###Set variables
    static String title = "GroupDocs Java SDK Samples";
    static String sample = "Sample23";
    static Form<Credentials> form = form(Credentials.class);

    public static Result index() {
        Form<Credentials> filledForm = form.bind(session());
        Credentials credentials = filledForm.get();
        Http.Request request = request();
        String frameUrl = null;

        if (StringUtils.isNotEmpty(credentials.client_id) || StringUtils.isNotEmpty(credentials.private_key)) {
            session().put("client_id", credentials.client_id);
            session().put("private_key", credentials.private_key);
            session().put("server_type", credentials.server_type);
        }

        if ("GET".equalsIgnoreCase(request.method())) {
            filledForm = form.bind(session());
            return ok(views.html.sample23.render(title, sample, null, filledForm));
        }
        if ("POST".equalsIgnoreCase(request.method())) {
            Http.MultipartFormData multipartFormData = request.body().asMultipartFormData();
            Map<String, String[]> formUrlEncodedData = multipartFormData.asFormUrlEncoded();

            String sourse = Utils.getFormValue(formUrlEncodedData, "sourse");
            String guid = null;
            if ("guid".equalsIgnoreCase(sourse)) {
                guid = Utils.getFormValue(formUrlEncodedData, "fileId");
            } else if ("url".equalsIgnoreCase(sourse)) {
                try {
                    String url = Utils.getFormValue(formUrlEncodedData, "url");
                    guid = Utils.getGuidByUrl(credentials.client_id, credentials.private_key, credentials.server_type, url);
                } catch (Exception e) {
                    filledForm.reject(e.getMessage());
                    e.printStackTrace();
                    return ok(views.html.sample23.render(title, sample, null, filledForm));
                }
            } else if ("local".equalsIgnoreCase(sourse)) {
                try {
                    Http.MultipartFormData.FilePart local = multipartFormData.getFile("file");
                    guid = Utils.getGuidByFile(credentials.client_id, credentials.private_key, credentials.server_type, local.getFilename(), new FileStream(new FileInputStream(local.getFile())));
                } catch (Exception e) {
                    filledForm.reject(e.getMessage());
                    e.printStackTrace();
                    return ok(views.html.sample23.render(title, sample, null, filledForm));
                }
            }
            if (StringUtils.isEmpty(guid)) {
                filledForm.reject("GUID is empty or null!");
                return ok(views.html.sample23.render(title, sample, null, filledForm));
            }

            try {
                ApiInvoker.getInstance().setRequestSigner(
                        new GroupDocsRequestSigner(credentials.private_key));
                //Create DocApi object
                DocApi api = new DocApi();
                api.setBasePath(credentials.server_type);
                //Get document metadata
                ViewDocumentResponse response = api.ViewDocument(credentials.client_id, guid, "0", "-1", "", "100", "");

                if (credentials.server_type.equals("https://api.groupdocs.com/v2.0")) {
                    frameUrl = "https://apps.groupdocs.com/document-viewer/embed/" + response.getResult().getGuid() + "?frameborder=0 width=500 height=650";
                } else if (credentials.server_type.equals("https://dev-api.groupdocs.com/v2.0")) {
                    frameUrl = "https://dev-apps.groupdocs.com/document-viewer/embed/" + response.getResult().getGuid() + "?frameborder=0 width=500 height=650";
                } else if (credentials.server_type.equals("https://stage-api.groupdocs.com/v2.0")) {
                    frameUrl = "https://stage-apps.groupdocs.com/document-viewer/embed/" + response.getResult().getGuid() + "?frameborder=0 width=500 height=650";
                }

            } catch (Exception e) {
                filledForm.reject(e.getMessage());
                return ok(views.html.sample23.render(title, sample, null, filledForm));
            }
        }
        return ok(views.html.sample23.render(title, sample, frameUrl, filledForm));
    }
}