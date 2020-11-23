package com.egatetutor.backend.service;

import com.egatetutor.backend.model.UserInfo;
import com.egatetutor.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;


@Service
public class UserServiceImpl implements UserService, UserDetailsService {
    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    @Autowired
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserRepository userRepository;
    @Autowired
    private JavaMailSender javaMailSender;
    @Autowired
    private Environment env;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           BCryptPasswordEncoder bCryptPasswordEncoder) {
        super();
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.userRepository = userRepository;

    }

    public static String randomAlphaNumeric(int count) {
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }

        return builder.toString();
    }

    private void sendmail(UserInfo userDetails, String password) {
        try {
            SimpleMailMessage message1 = new SimpleMailMessage();
            String messageText = "Dear GATE aspirant, \r\n" +
                    "\r\n" +
                    "Congratulations!!! \r\n" +
                    "You have successfully registered eGATE Tutor. \r\n" +
                    "\r\n" +
                    "Login Credentials: \r\n" +
                    "User Name  :    " + userDetails.getEmailId() + "\r\n" +
                    "Password    :    " + password + "\r\n" +
                    "Click here to Login \r\n" +
                    "OR www.egatetutor.in \r\n" +
                    "\r\n" +
                    "Note: \r\n" +
                    "Visit FAQs section on www.egatetutor.in \r\n" +
                    "Admission issues related Queries: support@egatetutor.in \r\n" +
                    "Technical Issues related Queries: egatetutor@gmail.com \r\n" +
                    "(In case of queries call between 10:00 AM to 6:00 PM. Mon - Sun) \r\n" +
                    "For more information regarding GATE, iPATE, PSU prepration. Connect with us: \r\n" +
                    "Website: http://www.egatetutor.in/ \r\n" +
                    "Facebook: https://www.facebook.com/egate.tutor.18 \r\n" +
                    "Instagram: https://www.instagram.com/egatetutor/ \r\n" +
                    "eGATETutor \r\n" +
                    "Support Team eGATETutor";
            message1.setTo(userDetails.getEmailId());
            message1.setSentDate(new Date());
            message1.setCc("support@egatetutor.in");
            message1.setBcc("egatetutor@gmail.com");
            message1.setFrom(Objects.requireNonNull(env.getProperty("spring.mail.username")));
            message1.setSubject("eGATE Tutor Login Credential");
            message1.setText(messageText);
            javaMailSender.send(message1);
        } catch (Exception e) {
            System.out.println("Failed to send Email : " + e.getMessage() + " " + e);
        }
    }

    @Override
    public UserInfo createUser(UserInfo userDetails)
            throws Exception {
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
        if (userEntity.getIsAdmin()) {
            grantedAuth.add(new SimpleGrantedAuthority("ADMIN"));
        }
        if (!userEntity.getIsAdmin()) {
            grantedAuth.add(new SimpleGrantedAuthority("USER"));
        }
        return new User(userEntity.getEmailId(), userEntity.getPassword(),
                true, true, true, true, grantedAuth);
    }

    public UserInfo save(UserInfo user) {
        return userRepository.save(user);
    }

}
