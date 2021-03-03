package test.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import test.bankRQRS.CreateUserRQ;
import test.bankRQRS.CreateUserRS;
import test.bankRQRS.Error;
import test.bankRQRS.LoginUserRQ;
import test.bankRQRS.LoginUserRS;
import test.bankRQRS.LogoutUserRQ;
import test.bankRQRS.LogoutUserRS;
import test.bankRQRS.SearchRQ;
import test.bankRQRS.SearchRS;
import test.dbAccess.DBAccess;
import test.entity.User;
import test.manager.ApplicationManager;



@RestController
@SpringBootApplication
public class BankServerController {

	static Logger log = Logger.getLogger(BankServerController.class.getName());
	
	@Autowired
	JdbcTemplate jdbcTemplate;
	
	@PostMapping("/createUser")
	public CreateUserRS createUser(@RequestBody CreateUserRQ createUserRQ,HttpSession session) {
		log.info("Entering searchReport ");
		CreateUserRS createUserRS=new CreateUserRS();
		Error error=null;
		if(createUserRQ.getUserName()==null || createUserRQ.getUserName().isEmpty()) {
			error=new Error();
			error.setCode(400);
			error.setMessage("User name not valid");
		}else if(createUserRQ.getPassword()==null || createUserRQ.getPassword().isEmpty()) {
			error=new Error();
			error.setCode(400);
			error.setMessage("Password not valid");
		}else if((createUserRQ.getRole()==null || createUserRQ.getRole().isEmpty())) {
			error=new Error();
			error.setCode(400);
			error.setMessage("User role not valid");
		}else {
			DBAccess dBAccess=new DBAccess();
			dBAccess.createUserTable(jdbcTemplate);
			User user=dBAccess.checkUserExistOrNot(createUserRQ,jdbcTemplate);
			if(user!=null) {
				error=new Error();
				error.setCode(400);
				error.setMessage("Change username or password.");
			}else {
				user=dBAccess.insertNewUser(createUserRQ, jdbcTemplate);
				createUserRS.setId(user.getId());
				createUserRS.setUserName(user.getUsername());
				createUserRS.setPassword(user.getPassword());
				createUserRS.setRole(user.getRole());
			}
		}
		createUserRS.setError(error);
		log.info("Exiting createUser ");
		return createUserRS;
		
	}
	@PostMapping("/login")
	public LoginUserRS loginUser(@RequestBody LoginUserRQ loginUserRQ,HttpServletRequest request) {
		log.info("Entering loginUser ");
		DBAccess dBAccess=new DBAccess();
		LoginUserRS loginUserRS=new LoginUserRS();
		Error error=null;
		LoginUserRS oldUser=(LoginUserRS)request.getSession().getAttribute("UserDetails");
		if(oldUser!=null) {
			error=new Error();
			error.setCode(400);
			error.setMessage("User should logout before login.");
		}else if(loginUserRQ.getUserName()==null|| loginUserRQ.getUserName().isEmpty()) {
			error=new Error();
			error.setCode(400);
			error.setMessage("User name not valid");
		}else if(loginUserRQ.getPassword()==null||loginUserRQ.getPassword().isEmpty()) {
			error=new Error();
			error.setCode(400);
			error.setMessage("password not valid");
		}else {
			User user=dBAccess.getLoginUser(loginUserRQ,jdbcTemplate);
			if(user==null) {
				error=new Error();
				error.setCode(400);
				error.setMessage("username and password not valid");
			}else if(user.getLogin()==1) {
				error=new Error();
				error.setCode(400);
				error.setMessage("User should logout before login.");
			}else {
				loginUserRS.setRole(user.getRole());
				loginUserRS.setUserName(user.getUsername());
				loginUserRS.setEchoToken(request.getSession().getId());
				
				dBAccess.updateLoginUser(jdbcTemplate,true,loginUserRQ.getUserName(),loginUserRQ.getPassword());

				LoginUserRS sessionDetails=new LoginUserRS();
				sessionDetails.setRole(user.getRole());
				sessionDetails.setUserName(user.getUsername());
				sessionDetails.setEchoToken(request.getSession().getId());
				sessionDetails.setPassword(loginUserRQ.getPassword());
				request.getSession().setAttribute("UserDetails",sessionDetails);
			}
		}
		loginUserRS.setError(error);
		
		log.info("Exiting loginUser ");
		return loginUserRS;
	}
	@PostMapping("/search")
	public SearchRS searchReport(@RequestBody SearchRQ searchRQ,HttpSession session) {
		log.info("Entering searchReport ");
		SearchRS searchRS=new SearchRS();
		Error error=null;
		LoginUserRS loggedUser=(LoginUserRS) session.getAttribute("UserDetails");
		ApplicationManager applicationManager=new ApplicationManager();
		if(loggedUser==null) {
			error=new Error();
			error.setCode(401);
			error.setMessage("You have to login first.");
			searchRS.setError(error);
			return searchRS;
		}else if(loggedUser.getRole().equalsIgnoreCase(ApplicationManager.USER_ROLE) ) {
			searchRQ.setFromAmount(null);
			searchRQ.setToAmount(null);
		}else if(loggedUser.getRole().equalsIgnoreCase(ApplicationManager.TEST_ROLE) ) {
			error=new Error();
			error.setCode(401);
			error.setMessage("Unauthorized access.");
			searchRS.setError(error);
			return searchRS;
		}
		
		applicationManager.generateSearchResponse(jdbcTemplate, searchRQ, loggedUser, searchRS, error);
		log.info("Exiting searchReport ");
		return searchRS;
	}
	@PostMapping("/logout")
	public LogoutUserRS destroySession(@RequestBody LogoutUserRQ logoutUserRQ,HttpServletRequest request) {
		LogoutUserRS logoutUserRS=new LogoutUserRS();
		Error error=null;
		if(logoutUserRQ.getUserName()==null|| logoutUserRQ.getUserName().isEmpty()) {
			error=new Error();
			error.setCode(400);
			error.setMessage("User name not valid");
		}else if(logoutUserRQ.getPassword()==null||logoutUserRQ.getPassword().isEmpty()) {
			error=new Error();
			error.setCode(400);
			error.setMessage("password not valid");
		}else {
			DBAccess dBAccess=new DBAccess();
			dBAccess.updateLoginUser(jdbcTemplate,false,logoutUserRQ.getUserName(),logoutUserRQ.getPassword());
			request.getSession().invalidate();
			logoutUserRS.setStatus("logout successfully");
		}
		logoutUserRS.setError(error);
		return logoutUserRS;
	}
}
