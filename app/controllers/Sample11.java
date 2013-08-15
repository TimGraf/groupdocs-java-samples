//###<i>This sample will show how to use <b>CreateAnnotation</b> method from Annotation Api to annotate a document</i>
package controllers;
//Import of necessary libraries
import java.io.FileInputStream;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import com.groupdocs.sdk.api.StorageApi;
import com.groupdocs.sdk.common.FileStream;
import com.groupdocs.sdk.model.*;
import common.Utils;
import models.Credentials;

import org.apache.commons.lang3.StringUtils;

import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import scala.actors.threadpool.Arrays;

import com.groupdocs.sdk.common.ApiException;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.api.AntApi;

public class Sample11 extends Controller {
	//###Set variables
	static String title = "GroupDocs Java SDK Samples";
	static Form<Credentials> form = form(Credentials.class);
	
	public static Result index() {
		
		CreateAnnotationResult result = null;
		Form<Credentials> filledForm;
		String sample = "Sample11";
		Status status;
		//Check POST parameters
		if(request().method().equalsIgnoreCase("POST")){
			filledForm = form.bindFromRequest();
			if(filledForm.hasErrors()){
				//If filledForm have errors return to template
				status = badRequest(views.html.sample11.render(title, sample, result, filledForm));
			} else {
				//If filledForm have no errors get all parameters
				Credentials credentials = filledForm.get();
				session().put("client_id", credentials.getClient_id());
				session().put("private_key", credentials.getPrivate_key());
				session().put("server_type", credentials.getServer_type());
                String fileId = null;

                Http.MultipartFormData formData = request().body().asMultipartFormData();
                Map<String, String[]> fieldsData = formData.asFormUrlEncoded();
                String annotation_type = Utils.getFormValue(fieldsData, "annotation_type");
//				String delete_annotation = Utils.getFormValue(fieldsData, "delete_annotation");
                String box_x = Utils.getFormValue(fieldsData, "box_x");
                String box_y = Utils.getFormValue(fieldsData, "box_y");
                String box_width = Utils.getFormValue(fieldsData, "box_width");
                String box_height = Utils.getFormValue(fieldsData, "box_height");
                String annotationPosition_x = Utils.getFormValue(fieldsData, "annotationPosition_x");
                String annotationPosition_y = Utils.getFormValue(fieldsData, "annotationPosition_Y");
                String range_position = Utils.getFormValue(fieldsData, "range_position");
                String range_length = Utils.getFormValue(fieldsData, "range_length");
                String text = Utils.getFormValue(fieldsData, "text");

                try {
                    /////////////////////////////////////// -- //////////////////////////////////////

                    String sourse = Utils.getFormValue(fieldsData, "sourse");
                    if ("guid".equals(sourse)) { // File GUID
                        fileId = Utils.getFormValue(fieldsData, "fileId");
                    }
                    else if ("url".equals(sourse)) { // Upload file fron URL
                        String fileUrl = Utils.getFormValue(fieldsData, "fileUrl");
                        ApiInvoker.getInstance().setRequestSigner(
                                new GroupDocsRequestSigner(credentials.getPrivate_key()));
                        StorageApi storageApi = new StorageApi();
                        storageApi.setBasePath(credentials.getServer_type());
                        UploadResponse response = storageApi.UploadWeb(credentials.getClient_id(), fileUrl);
                        if(response != null && response.getStatus().trim().equalsIgnoreCase("Ok")){
                            fileId = response.getResult().getGuid();
                        }
                    }
                    else if ("local".equals(sourse)) { // Upload local file
                        Http.MultipartFormData.FilePart filePart = formData.getFile("filePart");
                        ApiInvoker.getInstance().setRequestSigner(
                                new GroupDocsRequestSigner(credentials.getPrivate_key()));
                        StorageApi storageApi = new StorageApi();
                        storageApi.setBasePath(credentials.getServer_type());
                        FileInputStream is = new FileInputStream(filePart.getFile());
                        UploadResponse response = storageApi.Upload(credentials.getClient_id(), filePart.getFilename(), "uploaded", "", new FileStream(is));
                        if(response != null && response.getStatus().trim().equalsIgnoreCase("Ok")){
                            fileId = response.getResult().getGuid();
                        }
                    }
                    /////////////////////////////////////// -- //////////////////////////////////////
                    // Sample:

					//### Check fileId amd annotation_type
					if(credentials.getClient_id() == null || credentials.getPrivate_key() == null || fileId == null || annotation_type == null){
						throw new Exception();
					}
					//### Create Signer, ApiClient and Annotation Api objects
					ApiInvoker.getInstance().setRequestSigner(
							new GroupDocsRequestSigner(credentials.getPrivate_key()));
					AntApi ant = new AntApi();
					ant.setBasePath(credentials.getServer_type());
					//Convert from String to Double entered parameters
					double x = Double.parseDouble(box_x);
					double y = Double.parseDouble(box_y);
					//###Create AnnotationReplyInfo object and fill it with data
					AnnotationReplyInfo ann_text = new AnnotationReplyInfo();
					//add entered text for annotation
					ann_text.setText(text);
					//Create List of replies
					List<AnnotationReplyInfo> replies_text = new ArrayList<AnnotationReplyInfo>();
					//add annotation text to the list
					replies_text.add(ann_text);
					//###Create AnnotationInfo object and fill it with data 
					AnnotationInfo requestBody = new AnnotationInfo();
					//Set type of annotation it can be text, area or point
					requestBody.setType(annotation_type);
					//Set replies from replies list
					requestBody.setReplies(replies_text);
					//###Check what type it was chosen
					//If annotation type is text set all parameters
					if (annotation_type.equals("text")) {
                        double w = Double.parseDouble(box_width);
                        double h = Double.parseDouble(box_height);
                        double ann_pos_x = Double.parseDouble(annotationPosition_x);
                        double ann_pos_y = Double.parseDouble(annotationPosition_y);
                        Integer range_pos = Integer.parseInt(range_position);
                        Integer range_len = Integer.parseInt(range_length);
						//Create rectangle object
						Rectangle box = new Rectangle();
						//Set rectangle parameters
						box.setX(x);
						box.setY(y);
						box.setWidth(w);
						box.setHeight(h);
						//Create range object
						Range range = new Range();
						//Set range parameters
						range.setPosition(range_pos);
						range.setLength(range_len);
						//Create point object
						Point annotationPosition = new Point();
						//Set point parameters
						annotationPosition.setX(ann_pos_x);
						annotationPosition.setY(ann_pos_y);
						//Set annotation parameters to the AnnotationInfo object
						requestBody.setBox(box);
						requestBody.setRange(range);
						requestBody.setAnnotationPosition(annotationPosition);
					//if annotation type is area set only box and position parameters	
					} else if (annotation_type.equals("area")) {
                        double w = Double.parseDouble(box_width);
                        double h = Double.parseDouble(box_height);
						//Create rectangle object
						Rectangle box = new Rectangle();
						//Set rectangle parameters
						box.setX(x);
						box.setY(y);
						box.setWidth(w);
						box.setHeight(h);
						//Create point object
						Point annotationPosition = new Point();
						//Set point parameters
						annotationPosition.setX(0.0);
						annotationPosition.setY(0.0);
						//Set annotation parameters to the AnnotationInfo object
						requestBody.setBox(box);
						requestBody.setAnnotationPosition(annotationPosition);
					//If annotation type is point set only box x,y coordinates 
					} else if(annotation_type.equals("point")) {
						//Create rectangle object
						Rectangle box = new Rectangle();
						//Set rectangle parameters
						box.setX(x);
						box.setY(y);
						box.setWidth(0.0);
						box.setHeight(0.0);
						//Create point object
						Point annotationPosition = new Point();
						//Set point parameters
						annotationPosition.setX(0.0);
						annotationPosition.setY(0.0);
						//Set annotation parameters to the AnnotationInfo object
						requestBody.setBox(box);
						requestBody.setAnnotationPosition(annotationPosition);
						
					}
					//###Make a request to Annotation API using client_id, fileId and requestBody
					CreateAnnotationResponse response = ant.CreateAnnotation(credentials.getClient_id(), fileId, requestBody);
					//Check request status
					if(response != null && response.getStatus().trim().equalsIgnoreCase("Ok")){
						//Get results of request
						result = response.getResult();
						
					} else {
						//If request failed throw exception massage   
						throw new Exception("It wasn't succeeded to create the annotation");
						
					}
					//If request was successful - set result variable for template
					status = ok(views.html.sample11.render(title, sample, result, filledForm));
				//###Definition of Api errors and conclusion of the corresponding message
				} catch (ApiException e) {
					if(e.getCode() == 401){
						List<Object> args = Arrays.asList(new Object[]{"https://apps.groupdocs.com/My/Manage", "Production Server"});
						filledForm.reject("Wrong Credentials. Please make sure to use credentials from", args);
					} else {
						filledForm.reject("Failed to access API: " + e.getMessage());
					}
					status = badRequest(views.html.sample11.render(title, sample, result, filledForm));
				//###Definition of filledForm errors and conclusion of the corresponding message
				} catch (Exception e) {
					e.printStackTrace();
					if(fileId == null){
						filledForm.reject("fileId", "This field is required");
					} else if(box_x == null){
						filledForm.reject("box_x", "This field is required");
					} else if(box_y == null){
						filledForm.reject("box_y", "This field is required");
					} else if(box_width == null){
						filledForm.reject("box_width", "This field is required");
					} else if(box_height == null){
						filledForm.reject("box_height", "This field is required");
					} else if(annotationPosition_x == null){
						filledForm.reject("annotationPosition_x", "This field is required");
					} else if(annotationPosition_y == null){
						filledForm.reject("annotationPosition_y", "This field is required");
					} else if(range_position == null){
						filledForm.reject("range_position", "This field is required");
					} else if(range_length == null){
						filledForm.reject("range_length", "This field is required");
					} else {
						filledForm.reject("fileId", "Something wrong with your file: " + e.getMessage());
					}
					status = badRequest(views.html.sample11.render(title, sample, result, filledForm));
				} 
			}
		} else {
			filledForm = form.bind(session());
			session().put("server_type", "https://api.groupdocs.com/v2.0");
			status = ok(views.html.sample11.render(title, sample, result, filledForm));
		}
		//Process template
		return status;
	}
	
}