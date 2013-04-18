package models;

import play.data.validation.Constraints.Required;

public class Credentials {

	@Required
	public String client_id;
	@Required
	public String private_key;
	@Required
	public String baseurl;
}
