package test.bankrqrs;

import lombok.Data;

@Data
public class LogoutUserRQ {
	private String userName;
	private String password;
}
