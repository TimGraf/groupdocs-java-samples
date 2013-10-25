package controllers;

import com.groupdocs.sdk.api.AsyncApi;
import com.groupdocs.sdk.api.SignatureApi;
import com.groupdocs.sdk.api.StorageApi;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.FileStream;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.GetJobDocumentsResponse;
import com.groupdocs.sdk.model.SignatureEnvelopeDocumentsResponse;
import com.groupdocs.sdk.model.SignatureFormDocumentResponse;
import com.groupdocs.sdk.model.SignatureFormDocumentsResponse;
import com.typesafe.plugin.MailerAPI;
import com.typesafe.plugin.MailerPlugin;
import common.Utils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.lf5.util.StreamUtils;
import org.codehaus.jackson.JsonNode;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: work
 * Date: 10.07.13
 * Time: 11:33
 * To change this template use File | Settings | File Templates.
 */
public class Callbacks extends Controller {

    public static Result convert_callback() {
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

    public static Result compare_callback() {
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


    public static Result signature_callback() {
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

    public static Result publish_callback() throws Exception{
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
}
