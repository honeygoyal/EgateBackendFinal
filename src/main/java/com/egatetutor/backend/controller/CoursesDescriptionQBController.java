package com.egatetutor.backend.controller;
import com.egatetutor.backend.enumType.CoursesStatus;
import com.egatetutor.backend.enumType.VerificationStatus;
import com.egatetutor.backend.model.CoursesOffered;
import com.egatetutor.backend.model.UserInfo;
import com.egatetutor.backend.model.questionbank.CourseDescQBStatusResponse;
import com.egatetutor.backend.model.questionbank.CoursesDescriptionQB;
import com.egatetutor.backend.repository.CoursesDescriptionQBRepository;
import com.egatetutor.backend.repository.CoursesOfferedRepository;
import com.egatetutor.backend.repository.SubscriptionRepository;
import com.egatetutor.backend.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;
@CrossOrigin("*")
@RestController
@RequestMapping("/coursesDetailForQB")
public class CoursesDescriptionQBController {

	@Autowired
	private CoursesOfferedRepository coursesOfferedRepository;

	@Autowired
	private CoursesDescriptionQBRepository coursesDescriptionQBRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private SubscriptionRepository subscriptionRepository;

	@Autowired
	private Environment env;

	@GetMapping("/getCoursesDescriptionQBByExamId")
	public ResponseEntity<List<CourseDescQBStatusResponse>> getCoursesDescriptionByExamId(@RequestParam("exam_id") Long examId,
													   @RequestParam("email")String email) throws Exception {
		return ResponseEntity.status(HttpStatus.OK).body(getCourses(examId, email));
	}


	@GetMapping("/getCoursesDescriptionQBByExamCode")
	public ResponseEntity<List<CourseDescQBStatusResponse>> getCoursesDescriptionByExamCode(@RequestParam("exam_code") String examCode,
																							@RequestParam("email")String email)throws Exception  {
		CoursesOffered coursesOffered = coursesOfferedRepository.findByExamCode(examCode);
		if(coursesOffered == null) return null;
		return ResponseEntity.status(HttpStatus.OK).body(getCourses(coursesOffered.getId(), email));
	}


	@GetMapping("/getCourseIdListQBForAdmin")
	public ResponseEntity<List<String>> getCourseIdListForAdmin(@RequestParam("exam_code") String examCode) {
		CoursesOffered coursesOffered = coursesOfferedRepository.findByExamCode(examCode);
		List<CoursesDescriptionQB> coursesDescriptionList = coursesDescriptionQBRepository.findCoursesDescriptionByExamId(coursesOffered.getId());
		List<String> courseIdList = coursesDescriptionList.stream().map(CoursesDescriptionQB::getCourseId).collect(Collectors.toList());
		return ResponseEntity.status(HttpStatus.OK).body(courseIdList);
	}
	@PostMapping("/createTestQB")
	public ResponseEntity<String> createTest(@Valid @RequestBody CoursesDescriptionQB coursesDescription)
	{
		try {
			Optional<CoursesDescriptionQB> course = coursesDescriptionQBRepository.findCoursesDescriptionByCourseId(coursesDescription.getCourseId());
			if(course.isPresent()) throw new Exception("CourseDescription/Test with courseId is already Exist");
			coursesDescriptionQBRepository.save(coursesDescription);
		} catch (Exception ex) {
			return  new ResponseEntity<String>( ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return  new ResponseEntity<String>("{}", HttpStatus.OK);
	}

	private List<CourseDescQBStatusResponse> getCourses(Long examId, String email) throws Exception{
		UserInfo userInfo = userRepository.findByEmailId(email);
		if(userInfo == null) {
			throw new Exception("User not found");
		}
		List<Long> examIdList =  subscriptionRepository.findByUserId(userInfo.getId());  //User subscription exam list
		Set<Long> examIdSet = new HashSet<>(examIdList);
		List<CoursesDescriptionQB> testList = coursesDescriptionQBRepository.findCoursesDescriptionByExamId(examId);
		List<CourseDescQBStatusResponse> testResponseArray = new ArrayList<>();

		for(CoursesDescriptionQB coursesDescription: testList) {
			ModelMapper modelMapper = new ModelMapper();
			modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
			CourseDescQBStatusResponse testResponseModel = modelMapper.map(coursesDescription, CourseDescQBStatusResponse.class);
			if(!examIdSet.contains(examId)){
				testResponseModel.setStatus(CoursesStatus.INACTIVE.name());
			}else{
				if(!userInfo.getIsVerified().equals(VerificationStatus.VERIFIED.name()) && !userInfo.getIsAdmin())
					testResponseModel.setStatus(CoursesStatus.INACTIVE.name());
				else{
					testResponseModel.setStatus(CoursesStatus.START.name());
				}
			}
			testResponseArray.add(testResponseModel);
		}
		return testResponseArray;
	}

}

