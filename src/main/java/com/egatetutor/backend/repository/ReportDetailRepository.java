package com.egatetutor.backend.repository;

import com.egatetutor.backend.model.ReportDetail;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReportDetailRepository extends CrudRepository<ReportDetail, Long> {

    @Query(value = "Select * from report_detail R where R.user_id=:user_id and R.course_id=:course_id",nativeQuery=true)
    List<ReportDetail> findReportDetailListByCompositeId(@Param("user_id")Long userId, @Param("course_id") Long course_id);

    @Query(value = "Select * from report_detail R where R.course_id=:course_id",nativeQuery=true)
    List<ReportDetail> findAllReportDetailOfAllUser(@Param("course_id") Long course_id);

    @Query(value = "Select * from report_detail R where R.user_id=:user_id and R.course_id=:course_id and R.question_id=:question_id",nativeQuery=true)
    Optional<ReportDetail> findReportDetailByCompositeId(@Param("user_id")Long userId, @Param("course_id") Long course_id,
                                                         @Param("question_id") Long question_id );
}
