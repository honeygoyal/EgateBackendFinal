package com.egatetutor.backend.service;

import com.egatetutor.backend.model.UserInfo;
import com.egatetutor.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;


@Service
public class UserServiceImpl implements UserService, UserDetailsService {
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	private UserRepository userRepository;
	@Autowired
	private Environment env;
	@Autowired
	public UserServiceImpl(UserRepository userRepository,
						   BCryptPasswordEncoder bCryptPasswordEncoder) {
		super();
		this.bCryptPasswordEncoder = bCryptPasswordEncoder;
		this.userRepository = userRepository;

	}

	private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

	public static String randomAlphaNumeric(int count) {
		StringBuilder builder = new StringBuilder();
		while (count-- != 0) {
			int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
			builder.append(ALPHA_NUMERIC_STRING.charAt(character));
		}

		return builder.toString();
	}

	private void sendmail(UserInfo userDetails, String password)
	{
			try {
				Properties props = System.getProperties();
				props.put("mail.smtp.auth", env.getProperty("spring.mail.properties.mail.smtp.auth"));
				props.put("mail.smtp.starttls.enable", env.getProperty("spring.mail.properties.mail.smtp.starttls.enable"));
				props.put("mail.host", env.getProperty("spring.mail.host"));
				props.put("mail.port", env.getProperty("spring.mail.port"));
				props.setProperty("mail.transport.protocol", "smtp");
				props.setProperty("mail.smtp.ssl.enable",env.getProperty("spring.mail.properties.mail.smtp.ssl.enable"));
				props.setProperty("mail.smtp.ssl.trust",env.getProperty("spring.mail.properties.mail.smtp.ssl.trust"));

				Session session = Session.getInstance(props, new javax.mail.Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(env.getProperty("spring.mail.username"), env.getProperty("spring.mail.password"));
					}
				});
				Message msg = new MimeMessage(session);
				msg.setFrom(new InternetAddress(env.getProperty("spring.mail.username"), false));
				String email = userDetails.getEmailId();

				msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
				msg.setSubject("eGATE Tutor Login Credential");
				String message ="Dear GATE aspirant, <br/>" +
						"<br/>" +
						"Congratulations!!! <br/>" +
						"You have successfully registered eGATE Tutor. <br/>" +
						"\r\n" +
						"Login Credentials: <br/>" +
						"User Name  :    " + email + "<br/>" +
						"Password    :    " + password + "<br/>" +
						"Click here to Login <br/>" +
						"OR www.egatetutor.in/ <br/>" +
						"<br/>" +
						"Note: <br/>" +
						"Visit FAQs section on www.egatetutor.in <br/>" +
						"Admission issues related Queries: support@egatetutor.in <br/>" +
						"Technical Issues related Queries: egatetutor@gmail.com <br/>" +
						"(In case of queries call between 10:00 AM to 6:00 PM. Mon - Sun) <br/>" +
						"For more information regarding GATE, iPATE, PSU prepration. Connect with us:\n" +
						"Website: http://www.egatetutor.in/ <br/>" +
						"Facebook: https://www.facebook.com/egate.tutor.18 <br/>" +
						"Instagram: https://www.instagram.com/egatetutor/ <br/>" +
						"eGATETutor<br/>" +
						"Support Team eGATETutor";

				msg.setContent(message, "text/html");

				msg.setSentDate(new Date());
				Transport transport = session.getTransport("smtp");
				transport.send(msg, msg.getRecipients(Message.RecipientType.TO));
				transport.close();
			}catch (Exception e){
				System.out.println("Failed to send Email : " + e.getMessage() +" "+ e);
			}
	}

	@Override
	public UserInfo createUser(UserInfo userDetails)
			throws  Exception {
		String password = randomAlphaNumeric(8);
		System.out.println(password);
		userDetails.setPassword(bCryptPasswordEncoder.encode(password));
		userDetails.setIsAdmin(false);
		userDetails.setVerified(false);
		UserInfo existingUser = userRepository.findByEmailId(userDetails.getEmailId());
		if (existingUser != null) {
			throw new Exception("User already exists with this email");
		}
		userRepository.save(userDetails);
		sendmail(userDetails, password);
		return userDetails;
	}

	@Override
	public UserDetails loadUserByUsername(String username) {
		UserInfo userEntity = userRepository.findByEmailId(username);
		if (userEntity == null)
			throw new UsernameNotFoundException(username);
		List<SimpleGrantedAuthority> grantedAuth = new ArrayList();
		if(userEntity.getIsAdmin()){
			grantedAuth.add(new SimpleGrantedAuthority("ADMIN"));
		}
		if(!userEntity.getIsAdmin()){
			grantedAuth.add(new SimpleGrantedAuthority("USER"));
		}
		return new User(userEntity.getEmailId(), userEntity.getPassword(),
				true, true, true, true, grantedAuth);
	}

	public UserInfo save (UserInfo user) {
		return userRepository.save(user);
	}

}
