package com.egatetutor.backend.repository;

import com.egatetutor.backend.model.Books;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;


@Repository
public interface BookRepository extends CrudRepository<Books, Long> {
    @Query(value = "SELECT * FROM books", nativeQuery = true)
    List<Books> findBooksByImageUrlExists();
}
