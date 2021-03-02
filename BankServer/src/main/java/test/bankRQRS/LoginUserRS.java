package test.bankRQRS;

import lombok.Data;

@Data
public class LoginUserRS {

	private Error error;
	private String userName;
	private String password;
	private String echoToken;
	private String role;
}
