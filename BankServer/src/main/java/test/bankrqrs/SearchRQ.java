package test.bankrqrs;

import lombok.Data;

@Data
public class SearchRQ {
	
   private String fromDate;
   private String toDate;
   private String fromAmount;
   private String toAmount;
   private String echoToken;
   
	
}
