package models;

import play.data.validation.Constraints.Required;

public class Credentials {

	@Required
	public String clientId;
	@Required
	public String privateKey;
	
}
