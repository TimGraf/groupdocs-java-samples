package models;

import org.apache.commons.lang.StringUtils;
import play.data.validation.Constraints;

public class Credentials {
    @Constraints.Required
    private String clientId;
    @Constraints.Required
    private String privateKey;
    private String basePath;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public void normalizeBasePath(String basePath) {
        this.basePath = (StringUtils.isEmpty(this.basePath)) ? basePath : this.basePath;
    }
}
