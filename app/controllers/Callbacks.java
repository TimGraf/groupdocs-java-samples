package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.groupdocs.sdk.api.AsyncApi;
import com.groupdocs.sdk.api.SignatureApi;
import com.groupdocs.sdk.api.AntApi;
import com.groupdocs.sdk.api.StorageApi;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.FileStream;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.*;
import com.typesafe.plugin.MailerAPI;
import com.typesafe.plugin.MailerPlugin;
import common.Utils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.lf5.util.StreamUtils;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.defaultpages.error;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
/**
 * Created with IntelliJ IDEA.
 * User: work
 * Date: 10.07.13
 * Time: 11:33
 * To change this template use File | Settings | File Templates.
 */
public class Callbacks extends Controller {

    public static Result convertCallback() {
        try {
            FileInputStream fileInputStream = new FileInputStream(Sample18.USER_INFO_FILE);
            DataInputStream dataInputStream = new DataInputStream(fileInputStream);
            String data = dataInputStream.readUTF();
            fileInputStream.close();

            String cid = data.split("\\|")[0];
            String pkey = data.split("\\|")[1];
            String burl = data.split("\\|")[2];
            if (StringUtils.isEmpty(cid) || StringUtils.isEmpty(pkey) || StringUtils.isEmpty(burl)) {
                return ok("ClientID or PrivateKEY or base path is not found!");
            }

            ApiInvoker.getInstance().setRequestSigner(new GroupDocsRequestSigner(pkey));
            Http.RawBuffer rawBuffer = request().body().asRaw();
            String jsonStr = new String(rawBuffer.asBytes());
            JsonNode json = Json.parse(jsonStr);

            String jobId = json.get("SourceId").asText();

            Thread.sleep(5);
            AsyncApi asyncApi = new AsyncApi();
            asyncApi.setBasePath(burl);
            GetJobDocumentsResponse jobDocumentsResponse = asyncApi.GetJobDocuments(cid, jobId, "");
            jobDocumentsResponse = Utils.assertResponse(jobDocumentsResponse);
            String resultGuid = jobDocumentsResponse.getResult().getInputs().get(0).getOutputs().get(0).getGuid();
            String resultName = jobDocumentsResponse.getResult().getInputs().get(0).getOutputs().get(0).getName();

            StorageApi storageApi = new StorageApi();
            storageApi.setBasePath(burl);
            FileStream fileStream = storageApi.GetFile(cid, burl);
            String outDir = "out";
            if (!new File(outDir).exists()) {
                new File(outDir).mkdir();
            }
            if (new File(outDir + "/" + resultName).exists()) {
                new File(outDir + "/" + resultName).delete();
            }
            new File(outDir + "/" + resultName).createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(outDir + "/" + resultName);
            StreamUtils.copy(fileStream.getInputStream(), fileOutputStream);
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ok("");
    }

    public static Result compareCallback() {
        try {
            FileInputStream fileInputStream = new FileInputStream(Sample19.USER_INFO_FILE);
            DataInputStream dataInputStream = new DataInputStream(fileInputStream);
            String data = dataInputStream.readUTF();
            fileInputStream.close();

            String cid = data.split("\\|")[0];
            String pkey = data.split("\\|")[1];
            String burl = data.split("\\|")[2];
            if (StringUtils.isEmpty(cid) || StringUtils.isEmpty(pkey) || StringUtils.isEmpty(burl)) {
                return ok("ClientID or PrivateKEY or base path is not found!");
            }

            ApiInvoker.getInstance().setRequestSigner(new GroupDocsRequestSigner(pkey));
            Http.RawBuffer rawBuffer = request().body().asRaw();
            String jsonStr = new String(rawBuffer.asBytes());
            JsonNode json = Json.parse(jsonStr);

            String jobId = json.get("SourceId").asText();
            Thread.sleep(5);
            AsyncApi asyncApi = new AsyncApi();
            asyncApi.setBasePath(burl);
            GetJobDocumentsResponse jobDocumentsResponse = asyncApi.GetJobDocuments(cid, jobId, "");
            jobDocumentsResponse = Utils.assertResponse(jobDocumentsResponse);
            String resultGuid = jobDocumentsResponse.getResult().getOutputs().get(0).getGuid();
            String resultName = jobDocumentsResponse.getResult().getOutputs().get(0).getName();

            StorageApi storageApi = new StorageApi();
            storageApi.setBasePath(burl);
            FileStream fileStream = storageApi.GetFile(cid, burl);
            String outDir = "out";
            if (!new File(outDir).exists()) {
                new File(outDir).mkdir();
            }
            if (new File(outDir + "/" + resultName).exists()) {
                new File(outDir + "/" + resultName).delete();
            }
            new File(outDir + "/" + resultName).createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(outDir + "/" + resultName);
            StreamUtils.copy(fileStream.getInputStream(), fileOutputStream);
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ok("");
    }


    public static Result signatureCallback() {
        try {
            FileInputStream fileInputStream = new FileInputStream(Sample21.USER_INFO_FILE);
            DataInputStream dataInputStream = new DataInputStream(fileInputStream);
            String data = dataInputStream.readUTF();
            fileInputStream.close();

            String cid = data.split("\\|")[0];
            String pkey = data.split("\\|")[1];
            String burl = data.split("\\|")[2];
            if (StringUtils.isEmpty(cid) || StringUtils.isEmpty(pkey) || StringUtils.isEmpty(burl)) {
                return ok("ClientID or PrivateKEY or base path is not found!");
            }

            ApiInvoker.getInstance().setRequestSigner(new GroupDocsRequestSigner(pkey));
            Http.RawBuffer rawBuffer = request().body().asRaw();
            String jsonStr = new String(rawBuffer.asBytes());
            JsonNode json = Json.parse(jsonStr);

            String envilopeId = json.get("SourceId").asText();
            Thread.sleep(5);

            SignatureApi signatureApi = new SignatureApi();
            signatureApi.setBasePath(burl);
            SignatureEnvelopeDocumentsResponse envelopeDocumentsResponse = signatureApi.GetSignatureEnvelopeDocuments(cid, envilopeId);
            envelopeDocumentsResponse = Utils.assertResponse(envelopeDocumentsResponse);

            String resultGuid = envelopeDocumentsResponse.getResult().getDocuments().get(0).getDocumentId();
            String resultName = envelopeDocumentsResponse.getResult().getDocuments().get(0).getDocumentId();

            StorageApi storageApi = new StorageApi();
            storageApi.setBasePath(burl);
            FileStream fileStream = storageApi.GetFile(cid, burl);
            String outDir = "out";
            if (!new File(outDir).exists()) {
                new File(outDir).mkdir();
            }
            if (new File(outDir + "/" + resultName).exists()) {
                new File(outDir + "/" + resultName).delete();
            }
            new File(outDir + "/" + resultName).createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(outDir + "/" + resultName);
            StreamUtils.copy(fileStream.getInputStream(), fileOutputStream);
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ok("");
    }

    public static Result publishCallback() throws Exception {
        FileInputStream fileInputStream = new FileInputStream(Sample32.USER_INFO_FILE);
        DataInputStream dataInputStream = new DataInputStream(fileInputStream);
        String data = dataInputStream.readUTF();
        fileInputStream.close();

        String cid = data.split("\\|")[0];
        String pkey = data.split("\\|")[1];
        String burl = data.split("\\|")[2];
        String email = data.split("\\|")[3];
        if (StringUtils.isEmpty(cid) || StringUtils.isEmpty(pkey) || StringUtils.isEmpty(burl) || StringUtils.isEmpty(email)) {
            return ok("ClientID or PrivateKEY or base path or email is not found!");
        }

        ApiInvoker.getInstance().setRequestSigner(new GroupDocsRequestSigner(pkey));
        // Get raw data
        Http.RawBuffer rawBuffer = request().body().asRaw();
        String jsonStr = new String(rawBuffer.asBytes());
        // Parse JSON
        JsonNode json = Json.parse(jsonStr);
        // Get form id from array
        String formId = json.get("SourceId").asText();

        SignatureApi signatureApi = new SignatureApi();
        // Get document from signature form
        SignatureFormDocumentsResponse signatureFormDocumentsResponse = signatureApi.GetSignatureFormDocuments(cid, formId);
        signatureFormDocumentsResponse = Utils.assertResponse(signatureFormDocumentsResponse);
        // Get document name
        String documentName = signatureFormDocumentsResponse.getResult().getDocuments().get(0).getName();
        // Create email with document name
        String subject = "Reminder: An envelope has to be signed on GroupDocs";
        String message =
                "<html>" +
                        "<head>" +
                        "<title>Sign form notification</title>" +
                        "</head>" +
                        "<body>" +
                        "<p>Document" + documentName + " is signed</p>" +
                        "</body>" +
                        "</html>";
        String from = "From: Remainder <noreply@groupdocs.com>";

        // Configure section "# Email configuration" in application.conf
        MailerAPI mail = play.Play.application().plugin(MailerPlugin.class).email();
        mail.setSubject(subject);
        mail.setCharset("utf-8");
        mail.setReplyTo(email);
        // sends html
        mail.sendHtml(message);
        return ok("");
    }


    public static Result checkGuidCallback() throws Exception {
        String result = "";
//counter to not wait forever
        int counter = 0;
//Check folder for downloaded file
        do {
            //Set max. iterations
            if (counter >= 10) {
                result = "Error";
                break;
            }
            Thread.sleep(5);
            //Check is downloads folder exist
            if (/*file_exists(__DIR__ . '/../../callback_info.txt')*/new File(Sample39.USER_INFO_FILE).exists()) {
                FileInputStream fileInputStream = new FileInputStream(Sample39.USER_INFO_FILE);
                DataInputStream dataInputStream = new DataInputStream(fileInputStream);
                String data = dataInputStream.readUTF();
                fileInputStream.close();

                String cid = data.split("\\|")[0];
                String pkey = data.split("\\|")[1];
                String burl = data.split("\\|")[2];
                if (StringUtils.isEmpty(cid) || StringUtils.isEmpty(pkey) || StringUtils.isEmpty(burl)) {
                    return ok("ClientID or PrivateKEY or base path is not found!");
                } else {
                    return ok(data);
                }

//                //If folder don't exist create it
//                $callbackInfo = __DIR__ . '/../../callback_info.txt';
//                //Local path to the text file with user data
//                $info = file($callbackInfo);
//                //Get user data from text file
//                $result = trim($info[0]);
            } else {
                counter++;
            }
        } while (true);
        return null;
    }

    public static Result formRedirectCallback() {
        try {
            FileInputStream fileInputStream = new FileInputStream(Sample40.USER_INFO_FILE);
            DataInputStream dataInputStream = new DataInputStream(fileInputStream);
            String data = dataInputStream.readUTF();
            fileInputStream.close();

            String cid = data.split("\\|")[0];
            String pkey = data.split("\\|")[1];
            String burl = data.split("\\|")[2];
            if (StringUtils.isEmpty(cid) || StringUtils.isEmpty(pkey) || StringUtils.isEmpty(burl)) {
                return ok("ClientID or PrivateKEY or base path is not found!");
            }

            ApiInvoker.getInstance().setRequestSigner(new GroupDocsRequestSigner(pkey));
            Http.RawBuffer rawBuffer = request().body().asRaw();
            String jsonStr = new String(rawBuffer.asBytes());
            JsonNode json = Json.parse(jsonStr);

            //Get job id from array
            String formId = json.get("SourceId").asText();
            String jobStatus = json.get("EventType").asText();

            JsonNode serializedData = Json.parse(json.get("SerializedData").asText());
            String participant = serializedData.get("ParticipantGuid").asText();
            if ("JobCompleted".equals(jobStatus)) {
                //Create AsyncApi object
                SignatureApi signatureApi = new SignatureApi();
                signatureApi.setBasePath(burl);
                //Create Storage Api object
                StorageApi storageApi = new StorageApi();
                storageApi.setBasePath(burl);
                //Get document from envelop
                SignatureFormParticipantResponse signatureFormParticipantResponse = signatureApi.GetSignatureFormParticipant(formId, participant);
                Utils.assertResponse(signatureFormParticipantResponse);

                //Get signed document GUID
                String guid = signatureFormParticipantResponse.getResult().getParticipant().getSignedDocuments().get(0).getDocumentGuid();
                return ok(guid);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ok(e.getMessage());
        }
        return ok("");
    }
	public static Result userRightsCallback() {
        try {
            FileInputStream fileInputStream = new FileInputStream(Sample41.USER_INFO_FILE);
            DataInputStream dataInputStream = new DataInputStream(fileInputStream);
            String data = dataInputStream.readUTF();
            fileInputStream.close();

            String cid = data.split("\\|")[0];
            String pkey = data.split("\\|")[1];
            String burl = data.split("\\|")[2];
            if (StringUtils.isEmpty(cid) || StringUtils.isEmpty(pkey) || StringUtils.isEmpty(burl)) {
                return ok("ClientID or PrivateKEY or base path is not found!");
            }

            ApiInvoker.getInstance().setRequestSigner(new GroupDocsRequestSigner(pkey));
            Http.RawBuffer rawBuffer = request().body().asRaw();
            String jsonStr = new String(rawBuffer.asBytes());
            JsonNode json = Json.parse(jsonStr);
            //Get job id from array
            JsonNode serializedData = Json.parse(json.get("SerializedData").asText());
            String documentGuid = serializedData.get("DocumentGuid").asText();
            String userGuid = serializedData.get("UserGuid").asText();

            AntApi antApi = new AntApi();
            antApi.setBasePath(burl);
            GetCollaboratorsResponse getCollaboratorsResponse = antApi.GetAnnotationCollaborators(cid, documentGuid);
            getCollaboratorsResponse = Utils.assertResponse(getCollaboratorsResponse);
            //Create ReviewerInfo array
            ReviewerInfo reviewerInfo = new ReviewerInfo();
            ArrayList<ReviewerInfo> reviewers = new ArrayList<ReviewerInfo>();
            int i = 1;
            while (i <= getCollaboratorsResponse.getResult().getCollaborators().size()){
                if (getCollaboratorsResponse.getResult().getCollaborators().get(i).getGuid().contains(userGuid)){
                    reviewerInfo.setId(getCollaboratorsResponse.getResult().getCollaborators().get(i).getId());
                    reviewerInfo.setAccess_rights("1");
                    reviewers.add(reviewerInfo);
                }
            }
            SetReviewerRightsResponse setReviewerRightsResponse = antApi.SetReviewerRights(cid, documentGuid, reviewers);


            if (new File("/../../callback_info.txt").exists()) {
                new File("/../../callback_info.txt").delete();
            }

            try {
                File makefile = new File("output.txt");
                FileWriter fwrite = new FileWriter(makefile);
                fwrite.write("User rights was set to view only");
                fwrite.flush();
                fwrite.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ok("");
    }
}
