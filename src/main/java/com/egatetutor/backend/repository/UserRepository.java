package com.egatetutor.backend.repository;

import com.egatetutor.backend.model.UserInfo;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends CrudRepository<UserInfo, Long> {

	@Query(value = "SELECT * FROM user_info U where U.email_id = ?1",nativeQuery = true)
	UserInfo findByEmailId(String email);

	@Query(value = "SELECT * FROM user_info U where U.verified = 'PENDING'", nativeQuery = true)
	List<UserInfo> findAllUnVerifiedUser();
}
