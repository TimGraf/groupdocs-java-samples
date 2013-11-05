package models;

import play.data.validation.Constraints;

public class Credentials {
    @Constraints.Required
    private String client_id;
    @Constraints.Required
    private String private_key;
    @Constraints.Required
    private String server_type;

    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public String getPrivate_key() {
        return private_key;
    }

    public void setPrivate_key(String private_key) {
        this.private_key = private_key;
    }

    public String getServer_type() {
        return server_type;
    }

    public void setServer_type(String server_type) {
        this.server_type = server_type;
    }
}
