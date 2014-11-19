package common;

import com.groupdocs.sdk.api.DocApi;
import com.groupdocs.sdk.api.StorageApi;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.FileStream;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.UploadResponse;
import com.groupdocs.sdk.model.ViewDocumentResponse;
import org.apache.commons.lang3.StringUtils;
import play.mvc.Http;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: work
 * Date: 19.04.13
 * Time: 10:56
 * To change this template use File | Settings | File Templates.
 */
public abstract class Utils {

    /**
     * @param multipartFormData
     * @param fieldName
     * @return
     */
    public static String getFormValue(Http.MultipartFormData multipartFormData, String fieldName) {
        return getFormValue(multipartFormData.asFormUrlEncoded(), fieldName);
    }

    /**
     * @param fieldsData
     * @param fieldName
     * @return
     */
    public static String getFormValue(Map<String, String[]> fieldsData, String fieldName) {
        String[] fieldsValue = fieldsData.get(fieldName);
        return ((fieldsValue == null || fieldsValue.length == 0 || fieldsValue[0] == null) ? null : (StringUtils.isBlank(fieldsValue[0]) ? null : fieldsValue[0].trim()));
    }

    /**
     * Upload file to server by Url and return file guid
     *
     * @param cid
     * @param pkey
     * @param bpath
     * @param url
     * @return
     * @throws Exception
     */
    public static String getGuidByUrl(String cid, String pkey, String bpath, String url) throws Exception {
        String guid = null;
        ApiInvoker.getInstance().setRequestSigner(new GroupDocsRequestSigner(pkey));
        StorageApi storageApi = new StorageApi();
        storageApi.setBasePath(bpath);
        UploadResponse response = storageApi.UploadWeb(cid, url);

        if (response != null && "Ok".equalsIgnoreCase(response.getStatus())) {
            guid = response.getResult().getGuid();
        } else {
            throw new Exception(response.getError_message());
        }
        return guid;
    }

    /**
     * Upload local file and return file guid
     *
     * @param cid
     * @param pkey
     * @param bpath
     * @param fileStream
     * @return
     * @throws Exception
     */
    public static String getGuidByFile(String cid, String pkey, String bpath, String pathWithName, FileStream fileStream) throws Exception {
        return getGuidByFile(cid, pkey, bpath, fileStream, pathWithName, "");
    }

    /**
     * Upload local file and return file guid
     *
     * @param cid
     * @param pkey
     * @param bpath
     * @param fileStream
     * @param path
     * @param callback
     * @return
     * @throws Exception
     */
    public static String getGuidByFile(String cid, String pkey, String bpath, FileStream fileStream, String path, String callback) throws Exception {
        String guid = null;
        ApiInvoker.getInstance().setRequestSigner(new GroupDocsRequestSigner(pkey));
        StorageApi storageApi = new StorageApi();
        storageApi.setBasePath(bpath);
        UploadResponse response = storageApi.Upload(cid, path, "Description", callback, 1, fileStream);

        if (response != null && "Ok".equalsIgnoreCase(response.getStatus())) {
            guid = response.getResult().getGuid();
        } else {
            throw new Exception(response.getError_message());
        }
        return guid;
    }

    /**
     * Load document info by guid and return file name
     *
     * @param cid
     * @param pkey
     * @param bpath
     * @param guid
     * @return
     * @throws Exception
     */
    public static String getFileNameByGuid(String cid, String pkey, String bpath, String guid) throws Exception {
        ApiInvoker.getInstance().setRequestSigner(new GroupDocsRequestSigner(pkey));
        DocApi docApi = new DocApi();
        docApi.setBasePath(bpath);
        ViewDocumentResponse viewDocumentResponse = docApi.ViewDocument(cid, guid, null, null, null, null, null, null);
        viewDocumentResponse = assertResponse(viewDocumentResponse);
        return viewDocumentResponse.getResult().getName();
    }

    /**
     * Load document metadata by guid and return file ID
     *
     * @param cid
     * @param pkey
     * @param bpath
     * @param guid
     * @return
     * @throws Exception
     */
    public static Double getFileIdByGuid(String cid, String pkey, String bpath, String guid) throws Exception {
        ApiInvoker.getInstance().setRequestSigner(new GroupDocsRequestSigner(pkey));
        DocApi docApi = new DocApi();
        docApi.setBasePath(bpath);
        ViewDocumentResponse viewDocumentResponse = docApi.ViewDocument(cid, guid, null, null, null, null, null, null);
        viewDocumentResponse = assertResponse(viewDocumentResponse);
        return viewDocumentResponse.getResult().getId();
    }

    /**
     * @param r
     * @param <R>
     * @return
     * @throws Exception
     */
    public static <R> R assertResponse(R r) throws Exception {
        if (r == null) {
            throw new Exception("response is null!");
        }
        if (!"Ok".equalsIgnoreCase((String) r.getClass().getDeclaredMethod("getStatus").invoke(r))) {
            throw new Exception((String) r.getClass().getDeclaredMethod("getError_message").invoke(r));
        }
        return r;
    }

    /**
     * @param r
     * @param <R>
     * @return
     * @throws Exception
     */
    public static <R> R assertNotNull(R r) throws Exception {
        if (r == null) {
            throw new Exception("Utils.assertNotNull calls with null value");
        }
        return r;
    }

    /**
     * @param request
     * @return
     */
    public static boolean isPOST(Http.Request request) {
        try {
            assertNotNull(request);
            assertNotNull(request.method());
            if ("POST".equalsIgnoreCase(request.method()) == false) {
                throw new Exception();
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * @param request
     * @return
     */
    public static boolean isGET(Http.Request request) {
        try {
            assertNotNull(request);
            assertNotNull(request.method());
            if ("GET".equalsIgnoreCase(request.method()) == false) {
                throw new Exception();
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
