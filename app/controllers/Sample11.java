//###<i>This sample will show how to use <b>CreateAnnotation</b> method from Annotation Api to annotate a document</i>
package controllers;
//Import of necessary libraries
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import models.Credentials;

import org.apache.commons.lang3.StringUtils;

import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import scala.actors.threadpool.Arrays;

import com.groupdocs.sdk.common.ApiException;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.AnnotationReplyInfo;
import com.groupdocs.sdk.model.CreateAnnotationResponse;
import com.groupdocs.sdk.model.CreateAnnotationResult;
import com.groupdocs.sdk.model.Point;
import com.groupdocs.sdk.model.Range;
import com.groupdocs.sdk.model.Rectangle;
import com.groupdocs.sdk.api.AntApi;
import com.groupdocs.sdk.model.AnnotationInfo;

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
				session().put("client_id", credentials.client_id);
				session().put("private_key", credentials.private_key);
				session().put("baseurl", credentials.baseurl);
				
				Map<String, String[]> formData = request().body().asFormUrlEncoded();
				String fileId = formData.get("fileId") != null ? formData.get("fileId")[0] : null;
				fileId = StringUtils.isBlank(fileId) ? null : fileId.trim();
				String annotation_type = formData.get("annotation_type") != null ? formData.get("annotation_type")[0] : null;
				annotation_type = StringUtils.isBlank(annotation_type) ? null : annotation_type.trim();
//				String delete_annotation = formData.get("delete_annotation") != null ? formData.get("delete_annotation")[0] : null;
//				String annotationId = formData.get("annotationId") != null ? formData.get("annotationId")[0] : null;
				String box_x = formData.get("box_x") != null ? formData.get("box_x")[0] : "0.0";
				box_x = StringUtils.isBlank(box_x) ? "0.0" : box_x.trim();
				String box_y = formData.get("box_y") != null ? formData.get("box_y")[0] : "0.0";
				box_y = StringUtils.isBlank(box_y) ? "0.0" : box_y.trim();
				String box_width = formData.get("box_width") != null ? formData.get("box_width")[0] : "0.0";
				box_width = StringUtils.isBlank(box_width) ? "0.0" : box_width.trim();
				String box_height = formData.get("box_height") != null ? formData.get("box_height")[0] : "0.0";
				box_height = StringUtils.isBlank(box_height) ? "0.0" : box_height.trim();
				String annotationPosition_x = formData.get("annotationPosition.x") != null ? formData.get("annotationPosition.x")[0] : "0.0";
				annotationPosition_x = StringUtils.isBlank(annotationPosition_x) ? "0.0" : annotationPosition_x.trim();
				String annotationPosition_y = formData.get("annotationPosition.y") != null ? formData.get("annotationPosition.y")[0] : "0.0";
				annotationPosition_y = StringUtils.isBlank(annotationPosition_y) ? "0.0" : annotationPosition_y.trim();
				String range_position = formData.get("range.position") != null ? formData.get("range.position")[0] : "0";
				range_position = StringUtils.isBlank(range_position) ? "0" : range_position.trim();
				String range_length = formData.get("range.length") != null ? formData.get("range.length")[0] : "0";
				range_length = StringUtils.isBlank(range_length) ? "0" : range_length.trim();
				String text = formData.get("text") != null ? formData.get("text")[0] : "";
				text = StringUtils.isBlank(text) ? "" : text.trim();
				
				
				try {
					//### Check fileId amd annotation_type
					if(credentials.client_id == null || credentials.private_key == null || fileId == null || annotation_type == null){
						throw new Exception();
					}
					//### Create Signer, ApiClient and Annotation Api objects
					ApiInvoker.getInstance().setRequestSigner(
							new GroupDocsRequestSigner(credentials.private_key));
					AntApi ant = new AntApi();
					ant.setBasePath(credentials.baseurl);
					//Convert from String to Double entered parameters
					double x = Double.parseDouble(box_x);
					double y = Double.parseDouble(box_y);
					double w = Double.parseDouble(box_width);
					double ann_pos_x = Double.parseDouble(annotationPosition_x);
					double ann_pos_y = Double.parseDouble(annotationPosition_y);
					double h = Double.parseDouble(box_height);
					Integer range_pos = Integer.parseInt(range_position);
					Integer range_len = Integer.parseInt(range_length);
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
					CreateAnnotationResponse response = ant.CreateAnnotation(credentials.client_id, fileId, requestBody);
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
			session().put("baseurl", "https://api.groupdocs.com/v2.0");
			status = ok(views.html.sample11.render(title, sample, result, filledForm));
		}
		//Process template
		return status;
	}
	
}