package com.egatetutor.backend.service;



import com.egatetutor.backend.enumType.CoursesStatus;
import com.egatetutor.backend.model.CoursesDescription;
import com.egatetutor.backend.model.CoursesOffered;
import com.egatetutor.backend.model.ReportOverall;
import com.egatetutor.backend.model.UserInfo;
import com.egatetutor.backend.model.responsemodel.CourseDescStatusResponse;
import com.egatetutor.backend.repository.*;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CoursesDescServiceImpl implements CoursesDescriptionService {

	@Autowired
	UserRepository userRepository;
	@Autowired
	private CoursesOfferedRepository coursesOfferedRepository;
	@Autowired
	private CoursesDescriptionRepository coursesDescriptionRepository;

	@Autowired
	private SubscriptionRepository subscriptionRepository;

	@Autowired
	private ReportOverallRepository reportOverallRepository;

	@Override
	public List<CourseDescStatusResponse>  getCourses(Long examId, String email)  throws Exception {
	   UserInfo userInfo = userRepository.findByEmailId(email);
		if(userInfo == null) {
			throw new Exception("User not found");
		}
	   List<Long> examIdList =  subscriptionRepository.findByUserId(userInfo.getId());  //User subscription exam list
		Set<Long> examIdSet = new HashSet<>(examIdList);
		List<CoursesDescription> testList = coursesDescriptionRepository.findCoursesDescriptionByExamId(examId);
    	List<CourseDescStatusResponse> testResponseArray = new ArrayList<>();

		for(CoursesDescription coursesDescription: testList) {
			ModelMapper modelMapper = new ModelMapper();
			modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
			CourseDescStatusResponse testResponseModel = modelMapper.map(coursesDescription, CourseDescStatusResponse.class);
			if(!examIdSet.contains(examId)){
				testResponseModel.setStatus(CoursesStatus.INACTIVE.name());
			}else{
				if(!userInfo.isVerified() && !userInfo.getIsAdmin())
					testResponseModel.setStatus(CoursesStatus.USER_UNVERIFIED.name());
				else{
				ReportOverall reportOverall = reportOverallRepository.findReportByCompositeId(userInfo.getId(), coursesDescription.getId());
				String status = (reportOverall != null)? reportOverall.getStatus(): CoursesStatus.START.name();
				testResponseModel.setStatus(status);
				}
			}
			testResponseArray.add(testResponseModel);
		}
		return testResponseArray;
	}

	@Override
	public void createTest(CoursesDescription coursesDescription) throws Exception {
		 Optional<CoursesDescription> course = coursesDescriptionRepository.findCoursesDescriptionByCourseId(coursesDescription.getCourseId());
		 if(course.isPresent()) throw new Exception("CourseDescription/Test with courseId is already Exist");
		coursesDescriptionRepository.save(coursesDescription);
	}

	@Override
	public List<CourseDescStatusResponse> getCoursesByExamCode(String examCode, String email) throws Exception {
		CoursesOffered coursesOffered = coursesOfferedRepository.findByExamCode(examCode);
		if(coursesOffered == null) return null;
		return getCourses(coursesOffered.getId(), email);
	}

	@Override
	public List<String> getCourseIdListForAdmin(String examCode) throws Exception {
		CoursesOffered coursesOffered = coursesOfferedRepository.findByExamCode(examCode);
		List<CoursesDescription> coursesDescriptionList = coursesDescriptionRepository.findCoursesDescriptionByExamId(coursesOffered.getId());
		List<String> courseIdList = coursesDescriptionList.stream().map(CoursesDescription::getCourseId).collect(Collectors.toList());
		return courseIdList;
	}


}
