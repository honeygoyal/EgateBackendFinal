package com.egatetutor.backend.repository;
import com.egatetutor.backend.model.CoursesDescription;
import com.egatetutor.backend.model.questionbank.CoursesDescriptionQB;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CoursesDescriptionQBRepository extends CrudRepository<CoursesDescriptionQB, Long> {

    @Query(value="SELECT * FROM courses_description_qb c where c.exam_Id=:id",nativeQuery=true)
	CoursesDescriptionQB[] findCoursesByExamId(@Param("id") Long id);

    @Query(value="SELECT * FROM courses_description_qb c where c.course_Id = ?1",nativeQuery=true)
    Optional<CoursesDescriptionQB> findCoursesDescriptionByCourseId(@Param("courseId")String courseId);

    @Query(value="SELECT * FROM courses_description_qb c where c.exam_id = ?",nativeQuery=true)
    List<CoursesDescriptionQB> findCoursesDescriptionByExamId(@Param("exam_id")Long exam_id);

}

