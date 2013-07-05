package common;

import com.groupdocs.sdk.api.StorageApi;
import com.groupdocs.sdk.common.ApiInvoker;
import com.groupdocs.sdk.common.FileStream;
import com.groupdocs.sdk.common.GroupDocsRequestSigner;
import com.groupdocs.sdk.model.UploadResponse;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: work
 * Date: 19.04.13
 * Time: 10:56
 * To change this template use File | Settings | File Templates.
 */
public abstract class Utils {
    public static String getFormValue(Map<String, String[]> fieldsData, String fieldName) {
        String[] fieldsValue = fieldsData.get(fieldName);
        return ((fieldsValue == null || fieldsValue.length == 0 || fieldsValue[0] == null) ? null : (StringUtils.isBlank(fieldsValue[0]) ? null : fieldsValue[0].trim()));
    }

    /**
     * Upload file to server by Url and return file guid
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
        }
        else {
            throw new Exception(response.getError_message());
        }
        return guid;
    }

    /**
     * Upload local file and return file guid
     * @param cid
     * @param pkey
     * @param bpath
     * @param fileStream
     * @return
     * @throws Exception
     */
    public static String getGuidByFile(String cid, String pkey, String bpath, FileStream fileStream) throws Exception{
        return getGuidByFile(cid, pkey, bpath, fileStream, "", "");
    }

    /**
     * Upload local file and return file guid
     * @param cid
     * @param pkey
     * @param bpath
     * @param fileStream
     * @param path
     * @param callback
     * @return
     * @throws Exception
     */
    public static String getGuidByFile(String cid, String pkey, String bpath, FileStream fileStream, String path, String callback) throws Exception{
        String guid = null;
        ApiInvoker.getInstance().setRequestSigner(new GroupDocsRequestSigner(pkey));
        StorageApi storageApi = new StorageApi();
        storageApi.setBasePath(bpath);
        UploadResponse response = storageApi.Upload(cid, path, "Description", callback, fileStream);

        if (response != null && "Ok".equalsIgnoreCase(response.getStatus())) {
            guid = response.getResult().getGuid();
        }
        else {
            throw new Exception(response.getError_message());
        }
        return guid;
    }
}
