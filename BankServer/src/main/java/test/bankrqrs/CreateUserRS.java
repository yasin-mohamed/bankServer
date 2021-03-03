package test.bankrqrs;

import lombok.Data;

@Data
public class CreateUserRS {
	private Error error;
	private int id;
	private String userName;
	private String password;
	private String role;

}
