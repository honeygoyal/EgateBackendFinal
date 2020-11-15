package com.egatetutor.backend.controller;


import com.egatetutor.backend.model.Books;
import com.egatetutor.backend.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/books")
public class BookController {

    @Autowired
    BookRepository bookRepository;


    @GetMapping("/getAllBooks")
    public ResponseEntity<List<Books>> getAllBooks() {
        List<Books> bookList = bookRepository.findBooksByImageUrlExists();
        return ResponseEntity.status(HttpStatus.OK).body(bookList);
    }
}
