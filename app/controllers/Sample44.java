package controllers;


import com.groupdocs.sdk.api.*;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.FileStream;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.*;
import common.Utils;
import models.Credentials;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import com.google.common.base.Strings;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Sample44 extends Controller {
    //
    protected static Form<Credentials> form = Form.form(Credentials.class);

    public static Result index() {

        if (Utils.isPOST(request())) {
            form = Form.form(Credentials.class).bindFromRequest();
            // Check errors
            if (form.hasErrors()) {
                return badRequest(views.html.sample44.render(false, null, null, form));
            }
            // Save credentials to session
            Credentials credentials = form.get();
            session().put("clientId", credentials.getClientId());
            session().put("privateKey", credentials.getPrivateKey());
            session().put("basePath", credentials.getBasePath());
            credentials.normalizeBasePath("https://api.groupdocs.com/v2.0");
            // Get request parameters
            Http.MultipartFormData body = request().body().asMultipartFormData();

            String name = Utils.getFormValue(body, "firstName");
            String lastName = Utils.getFormValue(body, "lastName");
            String firstEmail = Utils.getFormValue(body, "firstEmail");
            String secondEmail = Utils.getFormValue(body, "secondEmail");
            String gender = Utils.getFormValue(body, "gender");
            String basePath = credentials.getBasePath();


            ApiInvoker.getInstance().setRequestSigner(
                    new GroupDocsRequestSigner(credentials.getPrivateKey()));

            try {
                //
                String guid = null;
                //
                Http.MultipartFormData.FilePart file = body.getFile("file");
                StorageApi storageApi = new StorageApi();
                // Initialize API with base path
                storageApi.setBasePath(credentials.getBasePath());
                FileInputStream is = new FileInputStream(file.getFile());
                UploadResponse uploadResponse = storageApi.Upload(credentials.getClientId(), file.getFilename(), "uploaded", "", false, new FileStream(is));
                // Check response status
                uploadResponse = Utils.assertResponse(uploadResponse);
                guid = uploadResponse.getResult().getGuid();
                guid = Utils.assertNotNull(guid);
                //
                MergeApi mergeApi = new MergeApi();
                // Initialize API with base path
                mergeApi.setBasePath(credentials.getBasePath());

                Datasource datasource = new Datasource();
                datasource.setFields(new ArrayList<DatasourceField>());

                DatasourceField datasourceField = null;

                datasourceField = new DatasourceField();
                datasourceField.setName("gender");
                datasourceField.setType("text");
                datasourceField.setValues(new ArrayList<String>());
                datasourceField.getValues().add(gender);
                datasource.getFields().add(datasourceField);

                datasourceField = new DatasourceField();
                datasourceField.setName("name");
                datasourceField.setType("text");
                datasourceField.setValues(new ArrayList<String>());
                datasourceField.getValues().add(name);
                datasource.getFields().add(datasourceField);

                AddDatasourceResponse datasourceResponse = mergeApi.AddDataSource(credentials.getClientId(), datasource);
                // Check response status
                datasourceResponse = Utils.assertResponse(datasourceResponse);

                MergeTemplateResponse mergeTemplateResponse = mergeApi.MergeDatasource(credentials.getClientId(), guid, Double.toString(datasourceResponse.getResult().getDatasource_id()), "pdf", null);
                // Check response status
                mergeTemplateResponse = Utils.assertResponse(mergeTemplateResponse);

                Thread.sleep(8000);

                AsyncApi asyncApi = new AsyncApi();
                // Initialize API with base path
                asyncApi.setBasePath(credentials.getBasePath());

                GetJobDocumentsResponse jobDocumentsResponse = asyncApi.GetJobDocuments(credentials.getClientId(), Double.toString(mergeTemplateResponse.getResult().getJob_id()), null);
                // Check response status
                jobDocumentsResponse = Utils.assertResponse(jobDocumentsResponse);

                if ("Postponed".equalsIgnoreCase(jobDocumentsResponse.getResult().getJob_status())) {
                    throw new Exception("Job is failed");
                }

                if ("Pending".equalsIgnoreCase(jobDocumentsResponse.getResult().getJob_status())) {
                    throw new Exception("Job is pending");
                }

                String resultGuid = jobDocumentsResponse.getResult().getInputs().get(0).getOutputs().get(0).getGuid();
                String resultName = jobDocumentsResponse.getResult().getInputs().get(0).getOutputs().get(0).getName();

                // Create Signature api object
                SignatureApi signatureApi = new SignatureApi();
                // Initialize API with base path
                signatureApi.setBasePath(basePath);
                // Make a requests to Signature Api to create an envelope
                SignatureEnvelopeSettingsInfo env = new SignatureEnvelopeSettingsInfo();
                env.setEmailSubject("Sign this!");
                SignatureEnvelopeResponse envelopeResponse = signatureApi.CreateSignatureEnvelope(credentials.getClientId(), resultName, null, null, resultGuid, true, env);
                envelopeResponse = Utils.assertResponse(envelopeResponse);
                // Get an ID of created envelope
                final String envelopeGuid = envelopeResponse.getResult().getEnvelope().getId();

                // Make a request to Signature Api to get all available roles
                SignatureRolesResponse signatureRolesResponse = signatureApi.GetRolesList(credentials.getClientId(), null);
                // Check response status
                signatureRolesResponse = Utils.assertResponse(signatureRolesResponse);
                List<SignatureRoleInfo> roles = signatureRolesResponse.getResult().getRoles();
                String roleGuid = null;
                for (SignatureRoleInfo role : roles) {
                    // Get an ID of Signer role
                    if ("Signer".equalsIgnoreCase(role.getName())) {
                        roleGuid = role.getId();
                        break;
                    }
                }

                // Check emptiness lastName string
                if(Strings.isNullOrEmpty(lastName)){
                    lastName = name;
                }
                // Make a request to Signature Api to add new first recipient to envelope
                SignatureEnvelopeRecipientResponse signatureEnvelopeRecipientResponse = signatureApi.AddSignatureEnvelopeRecipient(credentials.getClientId(), envelopeGuid, firstEmail, name, lastName, roleGuid, null);
                // Check response status
                signatureEnvelopeRecipientResponse = Utils.assertResponse(signatureEnvelopeRecipientResponse);
                String recipientGuid = signatureEnvelopeRecipientResponse.getResult().getRecipient().getId();

                // Make a request to Signature Api to add new second recipient to envelope
                SignatureEnvelopeRecipientResponse signatureEnvelopeSecondRecipientResponse = signatureApi.AddSignatureEnvelopeRecipient(credentials.getClientId(), envelopeGuid, secondEmail, name + "2", lastName + "2", roleGuid, null);
                // Check response status
                signatureEnvelopeSecondRecipientResponse = Utils.assertResponse(signatureEnvelopeSecondRecipientResponse);
                String recipientSecondGuid = signatureEnvelopeSecondRecipientResponse.getResult().getRecipient().getId();

                // Make a request to Signature Api to get all available fields
                SignatureEnvelopeDocumentsResponse getEnvelopDocument = signatureApi.GetSignatureEnvelopeDocuments(credentials.getClientId(), envelopeGuid);
                // Check response status
                getEnvelopDocument = Utils.assertResponse(getEnvelopDocument);
                // Create new first field called singlIndex1
                SignatureEnvelopeFieldSettingsInfo envField1 = new SignatureEnvelopeFieldSettingsInfo();
                envField1.setName("singlIndex1");
                envField1.setLocationX(0.15);
                envField1.setLocationY(0.23);
                envField1.setLocationWidth(150.0);
                envField1.setLocationHeight(50.0);
                envField1.setForceNewField(true);
                envField1.setPage(1);

                // Make a request to Signature Api to add city field to envelope
                SignatureEnvelopeFieldsResponse signatureEnvelopeFieldsResponse = signatureApi.AddSignatureEnvelopeField(credentials.getClientId(), envelopeGuid, getEnvelopDocument.getResult().getDocuments().get(0).getDocumentId(), recipientGuid, "0545e589fb3e27c9bb7a1f59d0e3fcb9", envField1);

                // Create new second field called singlIndex2
                SignatureEnvelopeFieldSettingsInfo envField2 = new SignatureEnvelopeFieldSettingsInfo();
                envField2.setName("singlIndex2");
                envField2.setLocationX(0.35);
                envField2.setLocationY(0.23);
                envField2.setLocationWidth(150.0);
                envField2.setLocationHeight(50.0);
                envField2.setForceNewField(true);
                envField2.setPage(1);

                // Make a request to Signature Api to add city field to envelope
                SignatureEnvelopeFieldsResponse signatureEnvelopeSecondFieldsResponse = signatureApi.AddSignatureEnvelopeField(credentials.getClientId(), envelopeGuid, getEnvelopDocument.getResult().getDocuments().get(0).getDocumentId(), recipientSecondGuid, "0545e589fb3e27c9bb7a1f59d0e3fcb9", envField2);

                // Check response status
                Utils.assertNotNull(signatureEnvelopeFieldsResponse);
                SignatureEnvelopeSendResponse signatureEnvelopeSendResponse = signatureApi.SignatureEnvelopeSend(credentials.getClientId(), envelopeGuid, null);
                Utils.assertResponse(signatureEnvelopeSendResponse);

                String server = credentials.getBasePath().substring(0, credentials.getBasePath().indexOf(".com") + 4).replace("api", "apps");
                String embedUrl = server + "/signature2/signembed/" + envelopeGuid + "/" + recipientGuid;
                String embedUrl2 = server + "/signature2/signembed/" + envelopeGuid + "/" + recipientSecondGuid;

                // Render view
                return ok(views.html.sample44.render(true, embedUrl, embedUrl2, form));
            } catch (Exception e) {
                return badRequest(views.html.sample44.render(false, null, null, form));
            }
        } else if (Utils.isGET(request())) {
            form = form.bind(session());
        }
        return ok(views.html.sample44.render(false, null, null, form));
    }
}