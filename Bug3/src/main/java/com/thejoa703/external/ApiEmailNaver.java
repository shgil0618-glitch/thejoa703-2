package com.thejoa703.external;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Component;

@Component
public class ApiEmailNaver {
   
   public void sendMail(String subject, String content, String email) {
      //1. 보내는 쪽
      String host="smtp.naver.com";
      String user="gaeajang1@naver.com";
      String password="KLRFU6UKXTJJ"; // 2단계인증 비밀번호
      
      //2. 받는사람
      String to = email;
      //3. 인증세션 설정
         Properties props = new Properties();
         props.put("mail.smtp.host", host);   // naver
         props.put("mail.smtp.auth", "true"); // 인증  
         props.put("mail.smtp.port", "587");  // 
         props.put("mail.debug", "true");      // 
         
         props.put("mail.smtp.starttls.enable", "true");        //이메일 전송시 보안연결
         props.put("mail.smtp.ssl.trust", "smtp.naver.com");     // ssl 인증서 연결
         props.put("mail.smtp.ssl.protocols", "TLSv1.2"); 
         
         Session session = Session.getInstance(props,new Authenticator() {
         @Override
         protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(user, password);
         }  
      });
       //4. 메일 보내기 (Mime 텍스트 text/plain , html text/html, 이미지 image,png) 멀티미디어메세지
       MimeMessage message = new MimeMessage(session);
       try {
          message.setFrom(new InternetAddress(user));      //보내는 사람 jks7755@naver
          message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
          message.setSubject(subject);   //메일제목
          message.setContent(""          
                 + "<div style='max-width:600px; margin:auto; background-color:#ffffff; border:1px solid #e0e0e0; border-radius:8px; padding:30px; font-family:Segoe UI, sans-serif; color:#333;'>"
                 + "<h2 style='color:#005bac; border-bottom:1px solid #ddd; padding-bottom:10px;'>정기수신 메일 안내</h2>"
                 + "<p style='font-size:15px; line-height:1.8; margin-top:20px;'>"
                 + content
                 + "</p>"
                 + "<div style='margin-top:30px; text-align:center;'>"
                 + "<a href='https://example.com' style='display:inline-block; background-color:#005bac; color:#fff; padding:12px 24px; border-radius:4px; text-decoration:none; font-size:14px;'>홈페이지 바로가기</a>"
                 + "</div>"
                 + "<hr style='margin:40px 0; border:none; border-top:1px solid #eee;'>"
                 + "<p style='font-size:12px; color:#888; text-align:center;'>이 메일은 자동 발송된 안내 메일입니다.<br>문의: <a href='mailto:support@example.com' style='color:#005bac; text-decoration:none;'>support@example.com</a></p>"
                 + "</div>"
                 , "text/html; charset=euc-kr");
          
          Transport.send(message);
          System.out.println("..... successfully.....");
       }catch(Exception e) {
          e.printStackTrace();
       }

       
   }
}
