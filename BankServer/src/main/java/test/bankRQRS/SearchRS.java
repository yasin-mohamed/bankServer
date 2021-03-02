package test.bankRQRS;

import java.util.List;

import lombok.Data;
import test.entity.BankStatement;

@Data
public class SearchRS {
	private Error error;
	private List<BankStatement> statementList;

}
