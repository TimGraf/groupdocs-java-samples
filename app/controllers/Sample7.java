package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.reflect.Array;
import java.io.File;
import java.io.FileWriter;
import java.io.FileOutputStream;


import models.Credentials;

import org.apache.commons.lang3.StringUtils;



import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import scala.actors.threadpool.Arrays;
import scala.actors.remote.*;

import com.groupdocs.sdk.api.DocApi;
import com.groupdocs.sdk.api.StorageApi;
import com.groupdocs.sdk.common.ApiException;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.GetDocumentInfoResponse;
import com.groupdocs.sdk.model.GetImageUrlsResponse;
import com.groupdocs.sdk.model.ListEntitiesResponse;
import com.groupdocs.sdk.model.FileSystemDocument;
import com.groupdocs.sdk.model.UserInfo;
import com.groupdocs.sdk.model.UserInfoResponse;
import com.groupdocs.sdk.model.GetDocumentInfoResponse;
import com.groupdocs.sdk.model.GetDocumentInfoResult;

public class Sample7 extends Controller {

	static String title = "GroupDocs Java SDK Samples";
	static Form<Credentials> form = form(Credentials.class);
	
	public static Result index() {
		List<String> thumbnailUrls = new ArrayList<>();
		List<FileSystemDocument> documents = null;
		Form<Credentials> filledForm;
		String sample = "Sample7";
		Status status;
		if(request().method().equalsIgnoreCase("POST")){
			filledForm = form.bindFromRequest();
			if(filledForm.hasErrors()){
				status = badRequest(views.html.sample7.render(title, sample, thumbnailUrls, filledForm));
			} else {
				Credentials credentials = filledForm.get();
				session().put("client_id", credentials.client_id);
				session().put("private_key", credentials.private_key);
				
				Map<String, String[]> formData = request().body().asFormUrlEncoded();

				try {
					if (credentials.private_key == null
							|| credentials.client_id == null) {
						throw new Exception();
					}

					ApiInvoker.getInstance()
							.setRequestSigner(
									new GroupDocsRequestSigner(
											credentials.private_key));

					StorageApi api = new StorageApi();

					ListEntitiesResponse response = api.ListEntities(
							credentials.client_id, "", 0, null, null, null,
							null, null, true);
					if (response != null
							&& response.getStatus().trim()
									.equalsIgnoreCase("Ok")) {
						documents = response.getResult().getFiles();

						for (int i = 0; i <= documents.size(); i++) {

							FileSystemDocument document = documents.get(i);
							if (document.getThumbnail() != null) {

								DocApi Docapi = new DocApi();
								GetDocumentInfoResponse info = Docapi
										.GetDocumentMetadata(
												credentials.client_id,
												document.getGuid());
								Integer pageCount = null;
								if (info != null
										&& info.getStatus().trim()
												.equalsIgnoreCase("Ok")) {
									pageCount = info.getResult()
											.getPage_count();
								} else {
									throw new Exception("Not Found");
								}
								String dimention = "65x65";
								List<String> temp = Docapi
										.GetDocumentPagesImageUrls(
												credentials.client_id,
												document.getGuid(), 0,
												pageCount, dimention, null,
												null, null).getResult()
										.getUrl();
								thumbnailUrls.addAll(temp);

							}

						}
			                
			                status = ok(views.html.sample7.render(title, sample, thumbnailUrls, filledForm));
			            } else {
			                throw new Exception("Result error!");
			            }
				} catch (ApiException e) {
					if(e.getCode() == 401){
						List<Object> args = Arrays.asList(new Object[]{"https://apps.groupdocs.com/My/Manage", "Production Server"});
						filledForm.reject("Wrong Credentials. Please make sure to use credentials from", args);
					} else {
						filledForm.reject("Failed to access API: " + e.getMessage());
					}
					status = badRequest(views.html.sample7.render(title, sample, thumbnailUrls, filledForm));
				} catch (Exception e) {
					e.printStackTrace();
					status = badRequest(views.html.sample7.render(title, sample, thumbnailUrls, filledForm));
				}
			}
		} else {
			filledForm = form.bind(session());
			status = ok(views.html.sample7.render(title, sample, thumbnailUrls, filledForm));
		}
		return status;
	}

}