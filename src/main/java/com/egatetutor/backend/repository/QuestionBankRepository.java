package com.egatetutor.backend.repository;

import com.egatetutor.backend.model.QuestionLayout;
import com.egatetutor.backend.model.questionbank.QuestionBank;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionBankRepository extends CrudRepository<QuestionBank, Long> {

    @Query(value = "SELECT * FROM question_bank q WHERE q.course_Id=:course_Id",nativeQuery = true)
    List<QuestionBank> findQuestionsById(@Param("course_Id") Long courseId);

}
