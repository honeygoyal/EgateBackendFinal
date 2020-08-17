package com.egatetutor.backend.repository;

import com.egatetutor.backend.model.ReportDetail;
import com.egatetutor.backend.model.ReportOverall;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReportDetailRepository extends CrudRepository<ReportDetail, Long> {

    @Query(value = "Select * from report_detail R where R.user_id=:user_id and R.course_id=:course_id",nativeQuery=true)
    List<ReportDetail> findReportDetailByCompositeId(@Param("user_id")Long userId, @Param("course_id") Long course_id);

//    @Query(value = "Select avg(time_taken) from report_detail R where R.question_id=:question_id and R.course_id=:course_id",nativeQuery=true)
//    String findAverageTimeTaken(@Param("question_id")Long question_id, @Param("course_id") Long course_id);
//
//    @Query(value = "Select count(*) from report_detail R where R.question_id=:question_id and R.course_id=:course_id",nativeQuery=true)
//    String findTotalAttempt(@Param("question_id")Long question_id, @Param("course_id") Long course_id);
//
//    @Query(value = "Select min(time_taken) from report_detail R where R.question_id=:question_id and R.course_id=:course_id and R.answer_submitted=:answer_submitted",nativeQuery=true)
//    String findMinimumTimeForMCQ(@Param("question_id")Long question_id, @Param("course_id") Long course_id, @Param("answer_submitted") String answer_submitted);

    @Query(value = "Select * from report_detail R where R.question_id=:question_id and R.course_id=:course_id",nativeQuery=true)
    List<ReportDetail> findAllReportDetailByQuestion(@Param("question_id")Long question_id, @Param("course_id") Long course_id);

    /*
TODO Question Analysis
    private int unAttempt;
    private int inCorrectAttempt;
    private int correctAttempt;
 */

}
