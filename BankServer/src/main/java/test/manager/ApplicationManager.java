package test.manager;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import test.bankRQRS.Error;
import test.bankRQRS.LoginUserRS;
import test.bankRQRS.SearchRQ;
import test.bankRQRS.SearchRS;
import test.dbAccess.DBAccess;
import test.entity.BankStatement;

public class ApplicationManager {
    
	public static String ADMIN_ROLE="Admin";
	public static String USER_ROLE="User";
	public static String TEST_ROLE="Test";
	static Logger log = Logger.getLogger(ApplicationManager.class.getName());

	public String addDays(SimpleDateFormat formatter1,String fromDate,int countOfDays) {
		String toDate=fromDate;
		log.info("Entering addDays");
		try {
			Calendar c = Calendar.getInstance();
			c.setTime(formatter1.parse(fromDate));
			c.add(Calendar.DAY_OF_MONTH, countOfDays);
			toDate = formatter1.format(c.getTime()); 
		} catch (Exception e) {
			// TODO: handle exception
			log.error("Error : "+e.getMessage());
		}
		log.info("Exiting addDays");
		return toDate;
	}
	public boolean checkIfEmptyString(String value) {
		return (value==null || value.isEmpty());
	}
	public String getFromDate(List<BankStatement> allStatement,SimpleDateFormat formatter1) {
		String fromDate=formatter1.format(new Date());
		allStatement.sort((BankStatement s1, BankStatement s2)->{
			try {
				return formatter1.parse(s1.getDatefield()).compareTo(formatter1.parse(s2.getDatefield()));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return 0;
		});
		if(allStatement!=null&&allStatement.size()>0) {
			fromDate=allStatement.get(allStatement.size()-1).getDatefield();
		}
		return fromDate;
	}
	public void checkSearchField(List<BankStatement> allStatement,SearchRQ searchRQ,LoginUserRS loggedUser,SimpleDateFormat formatter1) {
		log.info("Entering checkSearchField");
		try {
			
			String fromDate=getFromDate(allStatement, formatter1);
			String toDate=fromDate;
			fromDate=addDays(formatter1,fromDate,-90);
			if(loggedUser.getRole().equalsIgnoreCase(ADMIN_ROLE) && 
					(checkIfEmptyString(searchRQ.getFromDate()) || checkIfEmptyString(searchRQ.getToDate()))&& 
					(checkIfEmptyString(searchRQ.getFromAmount()) || checkIfEmptyString(searchRQ.getToAmount()))) {
				searchRQ.setFromDate(fromDate);
				searchRQ.setToDate(toDate);
				searchRQ.setFromAmount(null);
				searchRQ.setToAmount(null);
			}else if(loggedUser.getRole().equalsIgnoreCase(ADMIN_ROLE) && 
					(checkIfEmptyString(searchRQ.getFromAmount()) || checkIfEmptyString(searchRQ.getToAmount()))) {
				searchRQ.setFromAmount(null);
				searchRQ.setToAmount(null);
			}else if(loggedUser.getRole().equalsIgnoreCase(USER_ROLE)) {
				searchRQ.setFromDate(fromDate);
				searchRQ.setToDate(toDate);
				searchRQ.setFromAmount(null);
				searchRQ.setToAmount(null);
			}
		} catch (Exception e) {
			// TODO: handle exception
			log.error("Error : "+e.getMessage());
		}
		log.info("Exiting checkSearchField");
		
		
	}
	
	public boolean conditionCheck(BankStatement c,SearchRQ searchRQ,SimpleDateFormat formatter1,DecimalFormat decimalFormatter) {
		try {
			if(!checkIfEmptyString(c.getDatefield()) && !checkIfEmptyString(searchRQ.getFromDate()) &&
					!checkIfEmptyString(searchRQ.getToDate()) && !checkIfEmptyString(searchRQ.getFromAmount()) &&
					!checkIfEmptyString(searchRQ.getToAmount()) ) {
				return (formatter1.parse(c.getDatefield()).getTime()>= formatter1.parse(searchRQ.getFromDate()).getTime() &&
						   formatter1.parse(c.getDatefield()).getTime()<= formatter1.parse(searchRQ.getToDate()).getTime()) &&
						(decimalFormatter.parse(c.getAmount()).doubleValue()>=decimalFormatter.parse(searchRQ.getFromAmount()).doubleValue() &&
						decimalFormatter.parse(c.getAmount()).doubleValue()<=decimalFormatter.parse(searchRQ.getToAmount()).doubleValue());
				
			}else if(!checkIfEmptyString(searchRQ.getFromDate()) && !checkIfEmptyString(searchRQ.getToDate())) {
				return (formatter1.parse(c.getDatefield()).getTime()>= formatter1.parse(searchRQ.getFromDate()).getTime() &&
						   formatter1.parse(c.getDatefield()).getTime()<= formatter1.parse(searchRQ.getToDate()).getTime());
			}else if(!checkIfEmptyString(searchRQ.getFromAmount()) && !checkIfEmptyString(searchRQ.getToAmount())) {
				
				return (decimalFormatter.parse(c.getAmount()).doubleValue()>=decimalFormatter.parse(searchRQ.getFromAmount()).doubleValue() &&
						decimalFormatter.parse(c.getAmount()).doubleValue()<=decimalFormatter.parse(searchRQ.getToAmount()).doubleValue());
			}
		} catch (Exception e) {
			// TODO: handle exception
			log.error("Error : "+e.getMessage());
		}
		return false;
	}
	public List<BankStatement> filterSearchCriteria(List<BankStatement> allStatement,SearchRQ searchRQ,LoginUserRS loggedUser) {
		SimpleDateFormat formatter1=new SimpleDateFormat("dd.MM.yyyy"); 
		DecimalFormat decimalFormatter=new DecimalFormat("#.###");
		checkSearchField(allStatement,searchRQ,loggedUser,formatter1);
		return allStatement
				  .stream()
				  .filter(c -> {
					  return conditionCheck(c,searchRQ,formatter1,decimalFormatter);
				  }).collect(Collectors.toList());
	}
	public void generateSearchResponse(JdbcTemplate jdbcTemplate,SearchRQ searchRQ,LoginUserRS loggedUser,SearchRS searchRS,Error error) {
		log.info("Entering generateSearchResponse");
		try {
			DBAccess dBAccess=new DBAccess(); 
			List<BankStatement> allStatement=dBAccess.getSerachAllRecords(jdbcTemplate);
			List<BankStatement> allStatementBetweenDates =filterSearchCriteria(allStatement, searchRQ, loggedUser);
			if(allStatementBetweenDates!=null&&allStatementBetweenDates.size()!=0) {
				searchRS.setStatementList(allStatementBetweenDates);
			}else {
				error=new Error();
				error.setCode(401);
				error.setMessage("Results not available for criteria.");
			}
			searchRS.setError(error);
		} catch (Exception e) {
			// TODO: handle exception
			log.error("Error : "+e.getMessage());
		}
		log.info("Exiting generateSearchResponse");
	}

}
