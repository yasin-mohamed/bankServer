package test.bankRQRS;

import lombok.Data;

@Data
public class CreateUserRQ {
	
	private String userName;
	private String password;
	private String role;
}
