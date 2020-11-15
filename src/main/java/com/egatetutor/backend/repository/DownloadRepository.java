package com.egatetutor.backend.repository;

import com.egatetutor.backend.model.Download;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DownloadRepository extends CrudRepository<Download, Long> {
    @Query(value = "SELECT * FROM download C WHERE C.exam = :exam AND C.subsection = :subsection AND C.branch = :branch", nativeQuery = true)
    List<Download> findDownloadByExamAndSubsectionAndBranch(@Param("exam")String exam, @Param("subsection") String subsection,
                                                       @Param("branch") String branch);
}
