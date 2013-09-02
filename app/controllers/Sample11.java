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
    protected static Form<Credentials> form = form(Credentials.class);

    public static Result index() {

        if (Utils.isPOST(request())) {
            form = form.bindFromRequest();
            // Check errors
            if (form.hasErrors()) {
                return badRequest(views.html.sample11.render(false, null, form));
            }
            // Save credentials to session
            Credentials credentials = form.get();
            session().put("client_id", credentials.getClient_id());
            session().put("private_key", credentials.getPrivate_key());
            session().put("server_type", credentials.getServer_type());
            // Get request parameters
            Http.MultipartFormData body = request().body().asMultipartFormData();
            String annotation_type = Utils.getFormValue(body, "annotation_type");
            String text = Utils.getFormValue(body, "text");
            String source = Utils.getFormValue(body, "sourse");
            double box_width = Double.parseDouble(Utils.getFormValue(body, "box_width"));
            double box_height = Double.parseDouble(Utils.getFormValue(body, "box_height"));
            int range_length = Integer.parseInt(Utils.getFormValue(body, "range_length"));
            int range_position = Integer.parseInt(Utils.getFormValue(body, "range_position"));
            double box_x = Double.parseDouble(Utils.getFormValue(body, "box_x"));
            double box_y = Double.parseDouble(Utils.getFormValue(body, "box_y"));
            double annotationPosition_x = Double.parseDouble(Utils.getFormValue(body, "annotationPosition_x"));
            double annotationPosition_y = Double.parseDouble(Utils.getFormValue(body, "annotationPosition_Y"));
            // Initialize SDK with private key
            ApiInvoker.getInstance().setRequestSigner(
                    new GroupDocsRequestSigner(credentials.getPrivate_key()));

            try {
                //
                String guid = null;
                //
                if ("guid".equals(source)) { // File GUID
                    guid = Utils.getFormValue(body, "fileId");
                }
                else if ("url".equals(source)) { // Upload file fron URL
                    String url = Utils.getFormValue(body, "fileUrl");
                    StorageApi storageApi = new StorageApi();
                    // Initialize API with base path
                    storageApi.setBasePath(credentials.getServer_type());
                    UploadResponse uploadResponse = storageApi.UploadWeb(credentials.getClient_id(), url);
                    // Check response status
                    uploadResponse = Utils.assertResponse(uploadResponse);
                    guid = uploadResponse.getResult().getGuid();
                }
                else if ("local".equals(source)) { // Upload local file
                    Http.MultipartFormData.FilePart file = body.getFile("filePart");
                    StorageApi storageApi = new StorageApi();
                    // Initialize API with base path
                    storageApi.setBasePath(credentials.getServer_type());
                    FileInputStream is = new FileInputStream(file.getFile());
                    UploadResponse uploadResponse = storageApi.Upload(credentials.getClient_id(), file.getFilename(), "uploaded", "", new FileStream(is));
                    // Check response status
                    uploadResponse = Utils.assertResponse(uploadResponse);
                    guid = uploadResponse.getResult().getGuid();
                }
                guid = Utils.assertNotNull(guid);
                //
                AntApi antApi = new AntApi();
                // Initialize API with base path
                antApi.setBasePath(credentials.getServer_type());
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
                requestBody.setType(annotation_type);
                // Set replies from replies list
                requestBody.setReplies(annotationReplyInfos);
                // Check what type it was chosen
                // If annotation type is text set all parameters
                if (annotation_type.equals("text")) {
                    // Create rectangle object
                    Rectangle box = new Rectangle();
                    // Set rectangle parameters
                    box.setX(box_x);
                    box.setY(box_y);
                    box.setWidth(box_width);
                    box.setHeight(box_height);
                    // Create range object
                    Range range = new Range();
                    // Set range parameters
                    range.setPosition(range_position);
                    range.setLength(range_length);
                    // Create point object
                    Point annotationPosition = new Point();
                    // Set point parameters
                    annotationPosition.setX(annotationPosition_x);
                    annotationPosition.setY(annotationPosition_y);
                    // Set annotation parameters to the AnnotationInfo object
                    requestBody.setBox(box);
                    requestBody.setRange(range);
                    requestBody.setAnnotationPosition(annotationPosition);
                    // If annotation type is area set only box and position parameters
                } else if (annotation_type.equals("area")) {
                    // Create rectangle object
                    Rectangle box = new Rectangle();
                    // Set rectangle parameters
                    box.setX(box_x);
                    box.setY(box_y);
                    box.setWidth(box_width);
                    box.setHeight(box_height);
                    // Create point object
                    Point annotationPosition = new Point();
                    // Set point parameters
                    annotationPosition.setX(0.0);
                    annotationPosition.setY(0.0);
                    // Set annotation parameters to the AnnotationInfo object
                    requestBody.setBox(box);
                    requestBody.setAnnotationPosition(annotationPosition);
                    // If annotation type is point set only box x,y coordinates
                } else if(annotation_type.equals("point")) {
                    // Create rectangle object
                    Rectangle box = new Rectangle();
                    // Set rectangle parameters
                    box.setX(box_width);
                    box.setY(box_height);
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
                //###Make a request to Annotation API using client_id, fileId and requestBody
                CreateAnnotationResponse annotationResponse = antApi.CreateAnnotation(credentials.getClient_id(), guid, requestBody);
                annotationResponse = Utils.assertResponse(annotationResponse);
                // Render view
                return ok(views.html.sample11.render(true, annotationResponse.getResult(), form));
            } catch (Exception e) {
                return badRequest(views.html.sample11.render(false, null, form));
            }
        } else if (Utils.isGET(request())) {
            form = form.bind(session());
            session().put("server_type", "https://api.groupdocs.com/v2.0");
        }
        return ok(views.html.sample11.render(false, null, form));
    }
}