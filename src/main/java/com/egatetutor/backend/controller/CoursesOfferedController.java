package com.egatetutor.backend.controller;

import com.egatetutor.backend.model.CoursesOffered;
import com.egatetutor.backend.repository.CoursesOfferedRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/coursesOffered")
public class CoursesOfferedController {
    @Autowired
    CoursesOfferedRepository coursesOfferedRepository;

    @GetMapping("/getAllCoursesOffered")
    public ResponseEntity<CoursesOffered[]> getAllCoursesOffered(){

        return ResponseEntity.status(HttpStatus.OK).body(coursesOfferedRepository.findCoursesOffereds());
    }

    @GetMapping("/getAllCoursesOfferedByBranch")
    public ResponseEntity<CoursesOffered[]> getAllCoursesOfferedByBranch(@RequestParam("branch") String branch){
        return ResponseEntity.status(HttpStatus.OK).body(coursesOfferedRepository.findByBranch(branch));
    }


}
