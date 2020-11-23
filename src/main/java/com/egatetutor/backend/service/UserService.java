package com.egatetutor.backend.service;
import com.egatetutor.backend.model.UserInfo;
import org.springframework.security.core.userdetails.UserDetails;


public interface UserService {
	UserInfo createUser(UserInfo userDto) throws  Exception;
	UserDetails loadUserByUsername(String username);
    void forgotPassword(String emailId) throws Exception;
}
