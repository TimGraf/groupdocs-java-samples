//###<i>This sample will show how to use <b>Upload</b> method from Storage Api to upload file to GroupDocs Storage </i>
package controllers;
//Import of necessary libraries
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.Utils;
import org.apache.commons.io.IOUtils;

import net.sf.ehcache.search.aggregator.Count;

import models.Credentials;
import org.apache.commons.lang3.StringUtils;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import scala.actors.threadpool.Arrays;

import com.groupdocs.sdk.api.StorageApi;
import com.groupdocs.sdk.api.DocApi;
import com.groupdocs.sdk.api.AsyncApi;
import com.groupdocs.sdk.api.MergeApi;
import com.groupdocs.sdk.common.ApiException;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.FileStream;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.AddDatasourceResponse;
import com.groupdocs.sdk.model.Datasource;
import com.groupdocs.sdk.model.DatasourceField;
import com.groupdocs.sdk.model.GetJobDocumentsResponse;
import com.groupdocs.sdk.model.JobInfo;
import com.groupdocs.sdk.model.MergeTemplateResponse;
import com.groupdocs.sdk.model.TemplateFieldsResponse;
import com.groupdocs.sdk.model.UploadRequestResult;
import com.groupdocs.sdk.model.UploadResponse;

public class Sample25 extends Controller {
	//###Set variables
	static String title = "GroupDocs Java SDK Samples";
	static Form<Credentials> form = form(Credentials.class);
	
	public static Result index() {
		UploadRequestResult file = null;
		String guid = "";
		HashMap<String, String> iframe = new HashMap<String, String>();
		Form<Credentials> filledForm;
		String sample = "Sample25";
		Status status;
		//Check POST parameters
		if(request().method().equalsIgnoreCase("POST")){
			filledForm = form.bindFromRequest();
			if(filledForm.hasErrors()){
				status = badRequest(views.html.sample25.render(title, sample, iframe, filledForm));
			} else {
				//Get POST data
				Credentials credentials = filledForm.get();
				session().put("client_id", credentials.getClient_id());
				session().put("private_key", credentials.getPrivate_key());
				session().put("server_type", credentials.getServer_type());
				
				MultipartFormData body = request().body().asMultipartFormData();
		        FilePart filePart = body.getFile("file");
		       
		        //###Create ApiInvoker, Storage Api and FileInputStream objects
				try {
					String basePath = credentials.getServer_type();
					if (basePath.equals("")) {
						basePath = "https://api.groupdocs.com/v2.0";
					}
					//Create ApiInvoker object
					
					ApiInvoker.getInstance().setRequestSigner(
							new GroupDocsRequestSigner(credentials.getPrivate_key()));
					//Create Storage object
					StorageApi storageApi = new StorageApi();
					//Create DocApi object
					DocApi docApi = new DocApi();
					//Create Async object
					AsyncApi asyncApi = new AsyncApi();
					//Create MergeApi object
					MergeApi mergeApi = new MergeApi();
					//Set base path for all objects
					storageApi.setBasePath(basePath);
					docApi.setBasePath(basePath);
					asyncApi.setBasePath(basePath);
					mergeApi.setBasePath(basePath);
					//Create FileInputStream object

                    Http.MultipartFormData multipartFormData = request().body().asMultipartFormData();
                    Map<String, String[]> formUrlEncodedData = multipartFormData.asFormUrlEncoded();
                    String sourse = Utils.getFormValue(formUrlEncodedData, "sourse");
                    if ("guid".equalsIgnoreCase(sourse)){
                        guid = Utils.getFormValue(formUrlEncodedData, "fileId");
                    }
                    else if ("url".equalsIgnoreCase(sourse)) {
                        try {
                            String url = Utils.getFormValue(formUrlEncodedData, "url");
                            guid = Utils.getGuidByUrl(credentials.getClient_id(), credentials.getPrivate_key(), credentials.getServer_type(), url);
                        }
                        catch (Exception e) {
                            filledForm.reject(e.getMessage());
                            e.printStackTrace();
                            return ok(views.html.sample25.render(title, sample, iframe, filledForm));
                        }
                    }
                    else if ("local".equalsIgnoreCase(sourse)) {
                        try {
                            Http.MultipartFormData.FilePart local = multipartFormData.getFile("file");
                            guid = Utils.getGuidByFile(credentials.getClient_id(), credentials.getPrivate_key(), credentials.getServer_type(), local.getFilename(), new FileStream(new FileInputStream(local.getFile())));
                        }
                        catch (Exception e) {
                            filledForm.reject(e.getMessage());
                            e.printStackTrace();
                            return ok(views.html.sample25.render(title, sample, iframe, filledForm));
                        }
                    }
                    if (StringUtils.isEmpty(guid)) {
                        filledForm.reject("GUID is empty or null!");
                        return ok(views.html.sample25.render(title, sample, iframe, filledForm));
                    }



						//Get all fields from template
						TemplateFieldsResponse fields = docApi.GetTemplateFields(credentials.getClient_id(), guid, false);
						if (fields != null && fields.getStatus().trim().equalsIgnoreCase("Ok")) {
							//Create DataSource object
							Datasource dataSource = new Datasource();
							//Create empty list for DatasorceFiled
							List<DatasourceField> list = new ArrayList<DatasourceField>();
							//Create empty list for values
							List<String> values = new ArrayList<String>();
							//Put values to the list
							values.add("value1");
							values.add("value2");
							 //Loop for fields creataion
							for (int i = 0; i < fields.getResult().getFields().size(); i++) {
								DatasourceField field = new DatasourceField();
								field.setName(fields.getResult().getFields().get(i).getName());
								field.setType("text");
								field.setValues(values);
								list.add(field);
							}
							//Set fields list to the DataSource
							dataSource.setFields(list);
							//Add DataSource to the GroupDocs
							AddDatasourceResponse addDataSource = mergeApi.AddDataSource(credentials.getClient_id(), dataSource);



							if (addDataSource.getStatus().trim().equalsIgnoreCase("Ok")) {
								//Merge DataSource and convert to the pdf
								MergeTemplateResponse merge = mergeApi.MergeDatasource(credentials.getClient_id(), guid, Double.toString(addDataSource.getResult().getDatasource_id()), "pdf", "");
								if (merge.getStatus().trim().equalsIgnoreCase("Ok")) {
									Thread.sleep(2000);
									//Check job status
									GetJobDocumentsResponse job = new GetJobDocumentsResponse();
									for (int n = 0; n < 5; n++) {
										
										job = asyncApi.GetJobDocuments(credentials.getClient_id(), Double.toString(merge.getResult().getJob_id()), "");
										if (job.getStatus().trim().equalsIgnoreCase("Ok")) {
											if (job.getResult().getJob_status().trim().equalsIgnoreCase("Completed") || job.getResult().getJob_status().trim().equalsIgnoreCase("Archived")) {
												break;
											} else if (job.getResult().getJob_status().trim().equalsIgnoreCase("Postponed")) {
												throw new Exception("Job is failed");
											}
										} else {
											throw new Exception(job.getError_message());
										}
									
									}
									//Get result file guid and name
									guid = job.getResult().getInputs().get(0).getOutputs().get(0).getGuid();
									String name = job.getResult().getInputs().get(0).getOutputs().get(0).getName();
									
									//###Make a request to Storage API using clientId
									
									//Get file from storage
									FileStream resp = storageApi.GetFile(credentials.getClient_id(), guid);
									//Check request result
									FileStream  dowloadedFile = null;
									if(resp != null && resp.getInputStream() != null){
										dowloadedFile = resp;
									} else {
										throw new Exception("Not Found");
									}
									//Check file name
									if(dowloadedFile.getFileName() == null){
										dowloadedFile.setFileName(name);
									}
									//###Obtaining file stream of downloading file and definition of folder where to download file
									String separator = System.getProperty("file.separator");
					                String path = new File("").getAbsolutePath();
					                String downloadPath = path + separator + "public" + separator + "images" + separator;
					                FileOutputStream newFile = new FileOutputStream(downloadPath + dowloadedFile.getFileName());
					                //Write file to local folder
					                IOUtils.copy(dowloadedFile.getInputStream(), newFile);
					                IOUtils.closeQuietly(dowloadedFile.getInputStream());
                                    newFile.close();
					                iframe.put("message", "File was converted and downloaded to the " + downloadPath + dowloadedFile.getFileName());
					                //### If request was successfull
			                         //Generation of iframe URL using $pageImage->result->guid
			                         //iframe to prodaction server
			                         if (basePath.equalsIgnoreCase("https://api.groupdocs.com/v2.0")) {
			                             iframe.put("url", "https://apps.groupdocs.com/document-viewer/embed/" + guid);
			                             //iframe to dev server
			                         } else if (basePath.equalsIgnoreCase("https://dev-api.groupdocs.com/v2.0")) {
			                        	 iframe.put("url", "https://dev-apps.groupdocs.com/document-viewer/embed/" + guid);
			                             //iframe to test server
			                         } else if (basePath.equalsIgnoreCase("https://stage-api.groupdocs.com/v2.0")) {
			                        	 iframe.put("url", "https://stage-apps.groupdocs.com/document-viewer/embed/" + guid);
			                         } else if (basePath.equalsIgnoreCase("http://realtime-api.groupdocs.com")) {
			                        	 iframe.put("url", "http://realtime-apps.groupdocs.com/document-viewer/embed/" + guid);
			                         }									
								}
								
							} else {
								throw new Exception(addDataSource.getError_message());
							}
							
						} else {
							throw new Exception(fields.getError_message());
						}

					//If request was successfull - set file variable for template
					status = ok(views.html.sample25.render(title, sample, iframe, filledForm));
			    //###Definition of Api errors and conclusion of the corresponding message
				} catch (ApiException e) {
					if(e.getCode() == 401){
						List<Object> args = Arrays.asList(new Object[]{"https://apps.groupdocs.com/My/Manage", "Production Server"});
						filledForm.reject("Wrong Credentials. Please make sure to use credentials from", args);
					} else {
						filledForm.reject("Failed to access API: " + e.getMessage());
					}
					status = badRequest(views.html.sample25.render(title, sample, iframe, filledForm));
				//###Definition of filledForm errors and conclusion of the corresponding message
				} catch (Exception e) {
					if(filePart == null){
						filledForm.reject("file", "This field is required");
					} else {
						filledForm.reject("file", "Something wrong with your file: " + e.getMessage());
					}
					status = badRequest(views.html.sample25.render(title, sample, iframe, filledForm));
				}
			}
		} else {
			filledForm = form.bind(session());
			session().put("server_type", "https://api.groupdocs.com/v2.0");
			status = ok(views.html.sample25.render(title, sample, iframe, filledForm));
		}
		//Process template
		return status;
	}
	
}