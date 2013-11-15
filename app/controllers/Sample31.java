package controllers;

import com.groupdocs.sdk.api.AsyncApi;
import com.groupdocs.sdk.api.MergeApi;
import com.groupdocs.sdk.api.SignatureApi;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.*;
import common.Utils;
import models.Credentials;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.util.Arrays;
import java.util.Date;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: work
 * Date: 07.09.13
 * Time: 16:10
 * This sample will show how to dynamically create your own questionnaire using forms and build signature form from the result document
 */
public class Sample31 extends Controller {
    //
    protected static Form<Credentials> form = Form.form(Credentials.class);

    public static Result index() {

        if (Utils.isPOST(request())) {
            form = Form.form(Credentials.class).bindFromRequest();
            // Check errors
            if (form.hasErrors()) {
                return badRequest(views.html.sample31.render(false, null, form));
            }
            // Save credentials to session
            Credentials credentials = form.get();
            session().put("clientId", credentials.getClientId());
            session().put("privateKey", credentials.getPrivateKey());
            session().put("basePath", credentials.getBasePath());
            credentials.normalizeBasePath("https://api.groupdocs.com/v2.0");
            // Get request parameters
            Http.MultipartFormData body = request().body().asMultipartFormData();
            String templateGuid = Utils.getFormValue(body, "templateGuid");
            String callbackUrl = Utils.getFormValue(body, "callbackUrl");
            String email = Utils.getFormValue(body, "email");
            String name = Utils.getFormValue(body, "name");
            String country = Utils.getFormValue(body, "country");
            String city = Utils.getFormValue(body, "city");
            String street = Utils.getFormValue(body, "street");
            // Initialize SDK with private key
            ApiInvoker.getInstance().setRequestSigner(
                    new GroupDocsRequestSigner(credentials.getPrivateKey()));

            try {
                //
                DatasourceField datasourceField = null;
                Datasource datasource = new Datasource();

                datasourceField = new DatasourceField();
                datasourceField.setName("email");
                datasourceField.setType("text");
                datasourceField.setValues(Arrays.asList(email));
                datasource.getFields().add(datasourceField);

                datasourceField = new DatasourceField();
                datasourceField.setName("country");
                datasourceField.setType("text");
                datasourceField.setValues(Arrays.asList(country));
                datasource.getFields().add(datasourceField);

                datasourceField = new DatasourceField();
                datasourceField.setName("name");
                datasourceField.setType("text");
                datasourceField.setValues(Arrays.asList(name));
                datasource.getFields().add(datasourceField);

                datasourceField = new DatasourceField();
                datasourceField.setName("street");
                datasourceField.setType("text");
                datasourceField.setValues(Arrays.asList(street));
                datasource.getFields().add(datasourceField);

                datasourceField = new DatasourceField();
                datasourceField.setName("city");
                datasourceField.setType("text");
                datasourceField.setValues(Arrays.asList(city));
                datasource.getFields().add(datasourceField);

                MergeApi mergeApi = new MergeApi();
                // Initialize API with base path
                mergeApi.setBasePath(credentials.getBasePath());

                // Add DataSource to GroupDocs
                AddDatasourceResponse addDatasourceResponse = mergeApi.AddDataSource(credentials.getClientId(), datasource);
                // Check response status
                addDatasourceResponse = Utils.assertResponse(addDatasourceResponse);

                // If status ok merge Datasource to new pdf file
                MergeTemplateResponse mergeTemplateResponse = mergeApi.MergeDatasource(credentials.getClientId(), templateGuid, Double.toString(addDatasourceResponse.getResult().getDatasource_id()), "pdf", null);
                // Check response status
                mergeTemplateResponse = Utils.assertResponse(mergeTemplateResponse);

                AsyncApi asyncApi = new AsyncApi();
                // Initialize API with base path
                asyncApi.setBasePath(credentials.getBasePath());

                GetJobDocumentsResponse jobDocumentsResponse = null;
                for (int n = 0; n < 5; n++) {
                    // Delay necessary that the inquiry would manage to be processed
                    Thread.sleep(2000);
                    // Make request to api for get document info by job id
                    jobDocumentsResponse = asyncApi.GetJobDocuments(credentials.getClientId(), Double.toString(mergeTemplateResponse.getResult().getJob_id()), null);
                    // Check response status
                    jobDocumentsResponse = Utils.assertResponse(jobDocumentsResponse);
                    // Check job status, if status is Completed or Archived exit from cycle
                    String jobStatus = jobDocumentsResponse.getResult().getJob_status();
                    if ("Completed".equalsIgnoreCase(jobStatus) || "Archived".equalsIgnoreCase(jobStatus)) {
                        break;
                    }
                }
                // If job status Postponed throw exception with error
                if ("Postponed".equalsIgnoreCase(jobDocumentsResponse.getResult().getJob_status())) {
                    throw new Exception("Job is postponed!");
                } else if ("Pending".equalsIgnoreCase(jobDocumentsResponse.getResult().getJob_status())) {
                    throw new Exception("Job is pending!");
                }

                // Get file guid
                String guid = jobDocumentsResponse.getResult().getInputs().get(0).getOutputs().get(0).getGuid();
                guid = Utils.assertNotNull(guid);
                // Get file name
                String fileName = jobDocumentsResponse.getResult().getInputs().get(0).getOutputs().get(0).getName();
                fileName = Utils.assertNotNull(fileName);


                SignatureApi signatureApi = new SignatureApi();
                // Initialize API with base path
                signatureApi.setBasePath(credentials.getBasePath());

                SignatureEnvelopeResponse signatureEnvelopeResponse = signatureApi.CreateSignatureEnvelope(credentials.getClientId(), fileName, null, null, null, null, null);
                // Check response status
                signatureEnvelopeResponse = Utils.assertResponse(signatureEnvelopeResponse);
                String envilopeId = signatureEnvelopeResponse.getResult().getEnvelope().getId();

                Thread.sleep(5000);

                // Add uploaded document to envelope
                SignatureEnvelopeDocumentResponse signatureEnvelopeDocumentResponse = signatureApi.AddSignatureEnvelopeDocument(credentials.getClientId(), envilopeId, guid, null, null);
                // Check response status
                signatureEnvelopeDocumentResponse = Utils.assertResponse(signatureEnvelopeDocumentResponse);

                // Get role list for curent user
                SignatureRolesResponse signatureRolesResponse = signatureApi.GetRolesList(credentials.getClientId(), null);
                // Check response status
                signatureRolesResponse = Utils.assertResponse(signatureRolesResponse);

                String roleId = null;
                // Get id of role which can sign
                for (SignatureRoleInfo signatureRoleInfo : signatureRolesResponse.getResult().getRoles()){
                    if ("Signer".equalsIgnoreCase(signatureRoleInfo.getName())){
                        roleId = signatureRoleInfo.getId();
                        break;
                    }
                }
                roleId = Utils.assertNotNull(roleId);

                String fieldName = "SignatureSample" + new Random(new Date().getTime()).nextInt();
                SignatureFieldSettingsInfo signatureFieldSettings = new SignatureFieldSettingsInfo();
                signatureFieldSettings.setName(fieldName);

                SignatureFieldResponse signatureFieldResponse = signatureApi.CreateSignatureField(credentials.getClientId(), signatureFieldSettings);
                // Check response status
                signatureFieldResponse = Utils.assertResponse(signatureFieldResponse);

                // Add recipient to envelope
                SignatureEnvelopeRecipientResponse signatureEnvelopeRecipientResponse = signatureApi.AddSignatureEnvelopeRecipient(credentials.getClientId(), envilopeId, "sample@example.com", "test", "test", roleId, null);
                // Check response status
                signatureEnvelopeRecipientResponse = Utils.assertResponse(signatureEnvelopeRecipientResponse);

                // Get recipient id
                SignatureEnvelopeRecipientsResponse signatureEnvelopeRecipientsResponse = signatureApi.GetSignatureEnvelopeRecipients(credentials.getClientId(), envilopeId);
                // Check response status
                signatureEnvelopeRecipientsResponse = Utils.assertResponse(signatureEnvelopeRecipientsResponse);

                String recipientId = signatureEnvelopeRecipientsResponse.getResult().getRecipients().get(0).getId();
                SignatureEnvelopeDocumentsResponse envelopeDocumentsResponse = signatureApi.GetSignatureEnvelopeDocuments(credentials.getClientId(), envilopeId);
                // Check response status
                envelopeDocumentsResponse = Utils.assertResponse(envelopeDocumentsResponse);

                SignatureEnvelopeFieldSettingsInfo signatureEnvelopeFieldSettings = new SignatureEnvelopeFieldSettingsInfo();
                signatureEnvelopeFieldSettings.setLocationX(0.15);
                signatureEnvelopeFieldSettings.setLocationY(0.73);
                signatureEnvelopeFieldSettings.setLocationWidth(150.0);
                signatureEnvelopeFieldSettings.setLocationHeight(50.0);
                signatureEnvelopeFieldSettings.setName(fieldName);
                signatureEnvelopeFieldSettings.setForceNewField(true);
                signatureEnvelopeFieldSettings.setPage(1);

                SignatureEnvelopeFieldsResponse signatureEnvelopeFieldsResponse = signatureApi.AddSignatureEnvelopeField(credentials.getClientId(), envilopeId, envelopeDocumentsResponse.getResult().getDocuments().get(0).getDocumentId(), recipientId, "0545e589fb3e27c9bb7a1f59d0e3fcb9", signatureEnvelopeFieldSettings);
                // Check response status
                signatureEnvelopeFieldsResponse = Utils.assertResponse(signatureEnvelopeFieldsResponse);

                WebhookInfo webhookInfo = new WebhookInfo();
                webhookInfo.setCallbackUrl(callbackUrl);
                SignatureEnvelopeSendResponse signatureEnvelopeSendResponse = signatureApi.SignatureEnvelopeSend(credentials.getClientId(), envilopeId, webhookInfo);
                // Check response status
                signatureEnvelopeSendResponse = Utils.assertResponse(signatureEnvelopeSendResponse);

                String server = credentials.getBasePath().substring(0, credentials.getBasePath().indexOf(".com") + 4).replace("api", "apps");
                String frameUrl = server + "/signature2/signembed/" + envilopeId + "/" + recipientId;

                // Render view
                return ok(views.html.sample31.render(true, frameUrl, form));
            } catch (Exception e) {
                return badRequest(views.html.sample31.render(false, null, form));
            }
        } else if (Utils.isGET(request())) {
            form = form.bind(session());
        }
        return ok(views.html.sample31.render(false, null, form));
    }
}
