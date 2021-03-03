package test.bankrqrs;

import lombok.Data;

@Data
public class LogoutUserRS {
	private Error error;
	private String status;

}
