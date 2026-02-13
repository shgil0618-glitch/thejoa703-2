package com.thejoa703;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
@EnableScheduling
public class Bug3Application { 
	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.load();
		//Dotenv dotenv = Dotenv.configure() .directory("/home/ubuntu/legacy-boot").load();
		dotenv.entries().forEach(entry ->
            System.setProperty(entry.getKey(), entry.getValue())
        );
		
		System.setProperty("https.protocols", "TLSv1.2,TLSv1.3");
		SpringApplication.run(Bug3Application.class, args);  
	}

}


/*
  insert into sboard2 ( ID    , APP_USER_ID , btitle, bcontent, bpass, bfile,  bip )
  select  sboard2_seq.nextval , APP_USER_ID , btitle, bcontent, bpass, bfile,  bip   from sboard2;

*/	