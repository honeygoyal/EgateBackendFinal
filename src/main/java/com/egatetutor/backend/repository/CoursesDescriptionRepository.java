package com.egatetutor.backend.repository;
import com.egatetutor.backend.model.CoursesDescription;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CoursesDescriptionRepository extends CrudRepository<CoursesDescription, Long> {

    @Query(value="SELECT * FROM courses_description c where c.exam_Id=:id",nativeQuery=true)
	CoursesDescription[] findCoursesByExamId(@Param("id") Long id);

    @Query(value="SELECT * FROM courses_description c where c.course_Id = ?1",nativeQuery=true)
    Optional<CoursesDescription> findCoursesDescriptionByCourseId(@Param("courseId")String courseId);

    @Query(value="SELECT * FROM courses_description c where c.exam_id = ?",nativeQuery=true)
    List<CoursesDescription> findCoursesDescriptionByExamId(@Param("exam_id")Long exam_id);

}

