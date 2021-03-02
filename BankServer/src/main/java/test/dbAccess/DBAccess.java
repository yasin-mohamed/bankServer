package test.dbAccess;

import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;import org.springframework.jdbc.IncorrectResultSetColumnCountException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import test.bankRQRS.CreateUserRQ;
import test.bankRQRS.LoginUserRQ;
import test.controller.BankServerController;
import test.entity.BankStatement;
import test.entity.User;

public class DBAccess {
	static Logger log = Logger.getLogger(BankServerController.class.getName());
	
	public User getUserObject(JdbcTemplate jdbcTemplate,String sql) {
		User user=null;
		List<User> allUser=jdbcTemplate.query( sql, new BeanPropertyRowMapper(User.class));
		if(allUser!=null&&allUser.size()>0) {
			user=allUser.get(0);
		}
		return user;
	}
	public void createUserTable(JdbcTemplate jdbcTemplate) {
		//log.info(" Entering createUserTable");
		try {
			String sql="CREATE TABLE Users (id AUTOINCREMENT, username varchar(255) NOT NULL, password varchar(255), role varchar(255),login boolean,PRIMARY KEY(id));";
			//log.info(" SQL:"+sql);
			jdbcTemplate.update(sql);
		} catch (UncategorizedSQLException e) {
			// TODO: handle exception
		}
		//log.info(" Exiting createUserTable");
	}
	public User checkUserExistOrNot(CreateUserRQ createUserRQ,JdbcTemplate jdbcTemplate) {
		log.info(" Entering checkUserExistOrNot");
		User user=null;
		String sql="Select * from Users where username='"+createUserRQ.getUserName()+"' AND password='"+createUserRQ.getPassword()+"'";
		log.info(" SQL:"+sql);
		user= getUserObject(jdbcTemplate,sql);
		log.info(" Exiting checkUserExistOrNot");
		return user;
	}
	public User insertNewUser(CreateUserRQ createUserRQ,JdbcTemplate jdbcTemplate) {
		log.info(" Entering insertNewUser");
		User user=null;
		String sql="Insert into Users (username,password,role) Values('"+createUserRQ.getUserName()+"','"+createUserRQ.getPassword()+"','"+createUserRQ.getRole()+"')";
		log.info(" SQL:"+sql);
		jdbcTemplate.update(sql);
		sql="Select * from Users where username='"+createUserRQ.getUserName()+"' AND password='"+createUserRQ.getPassword()+"'";
		log.info(" SQL:"+sql);
		user= getUserObject(jdbcTemplate,sql);
		log.info(" Exiting insertNewUser");
		return user;
		
	}
	public User getLoginUser(LoginUserRQ loginUserRQ,JdbcTemplate jdbcTemplate) {
		log.info(" Entering getLoginUser");
		User user=null;
		String sql="Select * from Users where username='"+loginUserRQ.getUserName()+"' AND password='"+loginUserRQ.getPassword()+"'";
		log.info(" SQL:"+sql);
		user= getUserObject(jdbcTemplate,sql);
		log.info(" Exiting getLoginUser");
		return user;
		
	}
	public void updateLoginUser(JdbcTemplate jdbcTemplate,boolean login,String userName,String password) {
		log.info(" Entering updateLoginUser");
		String sql="Update Users SET login="+(login)+" where username='"+userName+"' AND password='"+password+"'";
		log.info(" SQL:"+sql);
		jdbcTemplate.update(sql);
		log.info(" Exiting updateLoginUser");
	}
	public List<BankStatement> getSerachAllRecords(JdbcTemplate jdbcTemplate) {
		log.info(" Entering getSerachAllRecords");
		String sql="Select st.id as id,ac.account_number as accountNumber,ac.account_type as type,st.datefield, st.amount from statement st Left Join account ac on ac.ID=st.account_id ";
		log.info(" SQL:"+sql);
		log.info(" Exiting getSerachAllRecords");
		return jdbcTemplate.query( sql, new BeanPropertyRowMapper(BankStatement.class));
	}

}
