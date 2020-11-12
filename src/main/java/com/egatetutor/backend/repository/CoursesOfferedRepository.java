package com.egatetutor.backend.repository;

import com.egatetutor.backend.model.CoursesOffered;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface CoursesOfferedRepository extends CrudRepository<CoursesOffered, Long> {

	@Query(value = "SELECT * FROM courses_offered e where e.exam_id = ?1",nativeQuery = true)
    CoursesOffered findByExamCode(String service);

	@Query(value = "SELECT * FROM courses_offered",nativeQuery = true)
	CoursesOffered[] findCoursesOffereds();

	@Query(value = "SELECT * from courses_offered e where e.branch=?", nativeQuery = true)
	CoursesOffered[]  findByBranch(@Param("branch")String branch);

	@Query(nativeQuery =true,value = "SELECT * FROM courses_offered as e WHERE e.exam_id IN (:examCodes)")   // 3. Spring JPA In cause using native query
	List<CoursesOffered> findByExamCodeList(@Param("examCodes") List<String> examCodeList);

	@Query(value = "SELECT * FROM courses_offered C WHERE C.exam_name = :examName AND C.branch = :branch", nativeQuery = true)
	List<CoursesOffered> findCoursesForCompletePackage(@Param("examName")String examName, @Param("branch") String branch);
}
