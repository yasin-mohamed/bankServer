package test.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RestController;

import test.controller.BankServerController;
@RestController
@EnableAutoConfiguration
public class App {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ApplicationContext ctx =SpringApplication.run(BankServerController.class, args);
	}

}
