package test.entity;

import lombok.Data;

@Data
public class BankStatement {

	private int id;
	private String accountNumber;
	private String type;
	private String datefield;
	private String amount;
	
	
}
