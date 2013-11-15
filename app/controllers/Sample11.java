//###<i>This sample will show how to use <b>CreateAnnotation</b> method from Annotation Api to annotate a document</i>
package controllers;
//Import of necessary libraries

import com.groupdocs.sdk.api.AntApi;
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

public class Sample11 extends Controller {
    //
    protected static Form<Credentials> form = Form.form(Credentials.class);

    public static Result index() {

        if (Utils.isPOST(request())) {
            form = Form.form(Credentials.class).bindFromRequest();
            // Check errors
            if (form.hasErrors()) {
                return badRequest(views.html.sample11.render(false, null, form));
            }
            // Save credentials to session
            Credentials credentials = form.get();
            session().put("clientId", credentials.getClientId());
            session().put("privateKey", credentials.getPrivateKey());
            session().put("basePath", credentials.getBasePath());
            credentials.normalizeBasePath("https://api.groupdocs.com/v2.0");
            // Get request parameters
            Http.MultipartFormData body = request().body().asMultipartFormData();
            String annotationType = Utils.getFormValue(body, "annotationType");
            String text = Utils.getFormValue(body, "text");
            String source = Utils.getFormValue(body, "sourse");
            double boxX = Double.parseDouble(Utils.getFormValue(body, "boxX"));
            double boxY = Double.parseDouble(Utils.getFormValue(body, "boxY"));
            // Initialize SDK with private key
            ApiInvoker.getInstance().setRequestSigner(
                    new GroupDocsRequestSigner(credentials.getPrivateKey()));

            try {
                //
                String guid = null;
                //
                if ("guid".equals(source)) { // File GUID
                    guid = Utils.getFormValue(body, "fileId");
                } else if ("url".equals(source)) { // Upload file fron URL
                    String url = Utils.getFormValue(body, "fileUrl");
                    StorageApi storageApi = new StorageApi();
                    // Initialize API with base path
                    storageApi.setBasePath(credentials.getBasePath());
                    UploadResponse uploadResponse = storageApi.UploadWeb(credentials.getClientId(), url);
                    // Check response status
                    uploadResponse = Utils.assertResponse(uploadResponse);
                    guid = uploadResponse.getResult().getGuid();
                } else if ("local".equals(source)) { // Upload local file
                    Http.MultipartFormData.FilePart file = body.getFile("filePart");
                    StorageApi storageApi = new StorageApi();
                    // Initialize API with base path
                    storageApi.setBasePath(credentials.getBasePath());
                    FileInputStream is = new FileInputStream(file.getFile());
                    UploadResponse uploadResponse = storageApi.Upload(credentials.getClientId(), file.getFilename(), "uploaded", "", new FileStream(is));
                    // Check response status
                    uploadResponse = Utils.assertResponse(uploadResponse);
                    guid = uploadResponse.getResult().getGuid();
                }
                guid = Utils.assertNotNull(guid);
                //
                AntApi antApi = new AntApi();
                // Initialize API with base path
                antApi.setBasePath(credentials.getBasePath());
                // Create AnnotationReplyInfo object and fill it with data
                AnnotationReplyInfo annotationReplyInfo = new AnnotationReplyInfo();
                // Add entered text for annotation
                annotationReplyInfo.setText(text);
                // Create List of replies
                List<AnnotationReplyInfo> annotationReplyInfos = new ArrayList<AnnotationReplyInfo>();
                // Add annotation text to the list of replies
                annotationReplyInfos.add(annotationReplyInfo);
                // Create AnnotationInfo object and fill it with data
                AnnotationInfo requestBody = new AnnotationInfo();
                // Set type of annotation it can be text, area or point
                requestBody.setType(annotationType);
                // Set replies from replies list
                requestBody.setReplies(annotationReplyInfos);
                // Check what type it was chosen
                // If annotation type is text set all parameters
                if (annotationType.equals("text")) {
                    double boxWidth = Double.parseDouble(Utils.getFormValue(body, "boxWidth"));
                    double boxHeight = Double.parseDouble(Utils.getFormValue(body, "boxHeight"));
                    int rangeLength = Integer.parseInt(Utils.getFormValue(body, "rangeLength"));
                    int rangePosition = Integer.parseInt(Utils.getFormValue(body, "rangePosition"));
                    double annotationPositionX = Double.parseDouble(Utils.getFormValue(body, "annotationPositionX"));
                    double annotationPositionY = Double.parseDouble(Utils.getFormValue(body, "annotationPositionY"));
                    // Create rectangle object
                    Rectangle box = new Rectangle();
                    // Set rectangle parameters
                    box.setX(boxX);
                    box.setY(boxY);
                    box.setWidth(boxWidth);
                    box.setHeight(boxHeight);
                    // Create range object
                    Range range = new Range();
                    // Set range parameters
                    range.setPosition(rangePosition);
                    range.setLength(rangeLength);
                    // Create point object
                    Point annotationPosition = new Point();
                    // Set point parameters
                    annotationPosition.setX(annotationPositionX);
                    annotationPosition.setY(annotationPositionY);
                    // Set annotation parameters to the AnnotationInfo object
                    requestBody.setBox(box);
                    requestBody.setRange(range);
                    requestBody.setAnnotationPosition(annotationPosition);
                    // If annotation type is area set only box and position parameters
                } else if (annotationType.equals("area")) {
                    double boxWidth = Double.parseDouble(Utils.getFormValue(body, "boxWidth"));
                    double boxHeight = Double.parseDouble(Utils.getFormValue(body, "boxHeight"));
                    // Create rectangle object
                    Rectangle box = new Rectangle();
                    // Set rectangle parameters
                    box.setX(boxX);
                    box.setY(boxY);
                    box.setWidth(boxWidth);
                    box.setHeight(boxHeight);
                    // Create point object
                    Point annotationPosition = new Point();
                    // Set point parameters
                    annotationPosition.setX(0.0);
                    annotationPosition.setY(0.0);
                    // Set annotation parameters to the AnnotationInfo object
                    requestBody.setBox(box);
                    requestBody.setAnnotationPosition(annotationPosition);
                    // If annotation type is point set only box x,y coordinates
                } else if (annotationType.equals("point")) {
                    // Create rectangle object
                    Rectangle box = new Rectangle();
                    // Set rectangle parameters
                    box.setX(boxX);
                    box.setY(boxY);
                    box.setWidth(0.0);
                    box.setHeight(0.0);
                    // Create point object
                    Point annotationPosition = new Point();
                    //Set point parameters
                    annotationPosition.setX(0.0);
                    annotationPosition.setY(0.0);
                    // Set annotation parameters to the AnnotationInfo object
                    requestBody.setBox(box);
                    requestBody.setAnnotationPosition(annotationPosition);
                }
                //###Make a request to Annotation API using clientId, fileId and requestBody
                CreateAnnotationResponse annotationResponse = antApi.CreateAnnotation(credentials.getClientId(), guid, requestBody);
                annotationResponse = Utils.assertResponse(annotationResponse);
                // Render view
                return ok(views.html.sample11.render(true, annotationResponse.getResult(), form));
            } catch (Exception e) {
                return badRequest(views.html.sample11.render(false, null, form));
            }
        } else if (Utils.isGET(request())) {
            form = form.bind(session());
        }
        return ok(views.html.sample11.render(false, null, form));
    }
}