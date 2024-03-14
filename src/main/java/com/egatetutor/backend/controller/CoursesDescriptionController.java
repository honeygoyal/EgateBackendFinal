package com.egatetutor.backend.controller;


import com.egatetutor.backend.model.CoursesDescription;
import com.egatetutor.backend.model.responsemodel.CourseDescStatusResponse;
import com.egatetutor.backend.repository.CoursesOfferedRepository;
import com.egatetutor.backend.service.CoursesDescriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
@CrossOrigin("*")
@RestController
@RequestMapping("/coursesDetail")
public class CoursesDescriptionController {

	@Autowired
	private CoursesDescriptionService coursesDescriptionService;

	@Autowired
	private CoursesOfferedRepository coursesOfferedRepository;

	@Autowired
	private Environment env;

	@GetMapping("/getCoursesDescriptionByExamId")
	public ResponseEntity<List<CourseDescStatusResponse>> getCoursesDescriptionByExamId(@RequestParam("exam_id") Long examId,
													   @RequestParam("email")String email) throws Exception {
		List<CourseDescStatusResponse> coursesDescriptions = coursesDescriptionService.getCourses(examId,email);
		return ResponseEntity.status(HttpStatus.OK).body(coursesDescriptions);
	}


	@GetMapping("/getCoursesDescriptionByExamCode")
	public ResponseEntity<List<CourseDescStatusResponse>> getCoursesDescriptionByExamCode(@RequestParam("exam_code") String examCode,
																				@RequestParam("email")String email)throws Exception  {
		List<CourseDescStatusResponse> coursesDescriptions = coursesDescriptionService.getCoursesByExamCode(examCode,email);
		return ResponseEntity.status(HttpStatus.OK).body(coursesDescriptions);
	}


	@GetMapping("/getCourseIdListForAdmin")
	public ResponseEntity<List<String>> getCourseIdListForAdmin(@RequestParam("exam_code") String examCode)throws Exception  {
		List<String> courseIdList = coursesDescriptionService.getCourseIdListForAdmin(examCode);
		return ResponseEntity.status(HttpStatus.OK).body(courseIdList);
	}
	@PostMapping("/createTest")
	public ResponseEntity<String> createTest(@Valid @RequestBody CoursesDescription coursesDescription)
	{
		try {
			coursesDescriptionService.createTest(coursesDescription);
		} catch (Exception ex) {
			return  new ResponseEntity<String>( ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return  new ResponseEntity<String>("{}", HttpStatus.OK);
	}


}

