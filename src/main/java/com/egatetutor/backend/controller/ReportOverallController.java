package com.egatetutor.backend.controller;


import com.egatetutor.backend.enumType.QuestionStatus;
import com.egatetutor.backend.model.*;
import com.egatetutor.backend.model.compositekey.ReportOverallPK;
import com.egatetutor.backend.model.responsemodel.QuestionAnalysis;
import com.egatetutor.backend.model.responsemodel.UserRank;
import com.egatetutor.backend.model.responsemodel.ReportOverallRequest;
import com.egatetutor.backend.model.responsemodel.TestAnalytics;
import com.egatetutor.backend.repository.*;
import com.egatetutor.backend.service.ReportGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;


@CrossOrigin(origins = "https://trusting-euclid-a0c1c2.netlify.app")
@RestController
@RequestMapping("/reportOverall")
public class ReportOverallController {

    @Autowired
    ReportOverallRepository reportOverallRepository;

    @Autowired
    ReportGeneratorService reportGeneratorService;
    @Autowired
    UserRepository userRepository;

    @Autowired
    CoursesDescriptionRepository coursesDescriptionRepository;

    @GetMapping("/getOverallReportByUserId")
    public ResponseEntity<TestAnalytics> getReportByUserId(@RequestParam("user_id") Long userId,
                                                           @RequestParam("course_id")Long courseId)
    throws Exception
    {

        ReportOverall reportOverall = reportOverallRepository.findReportByCompositeId(
               userId, courseId);
       TestAnalytics testAnalytics = reportGeneratorService.getTestAnalytics(userId, courseId);
        Optional<CoursesDescription> coursesDescription = coursesDescriptionRepository.findById(reportOverall.getCourseId().getId());
        if(!coursesDescription.isPresent()){
            throw new Exception("Course/Test doesn't exist");
        }
        testAnalytics.setTotalMarks(coursesDescription.get().getTotalMarks());
        List<UserRank> reportOverallList = reportGeneratorService.getRankWiseReport(courseId);
        Optional<UserRank> reportForRank = reportOverallList.stream().
                filter(p-> (p.getUserId().equals(userId))).findFirst();
        reportForRank.ifPresent(userRank -> testAnalytics.setRank(userRank.getRank()));
        return ResponseEntity.status(HttpStatus.OK).body(testAnalytics);
    }

    @GetMapping("/getTopRank")
    public ResponseEntity<List<UserRank>> getTopRank(@RequestParam("course_id")Long courseId)
    {
        return ResponseEntity.status(HttpStatus.OK).body(reportGeneratorService.getRankWiseReport(courseId));
    }
    @PostMapping("/saveOverallReport")
    public ResponseEntity<String> saveOverallReport (@Valid @RequestBody ReportOverallRequest reportOverallRequest)
    throws Exception
    {
        Optional<UserInfo> user =  userRepository.findById(reportOverallRequest.getUserId());
        Optional<CoursesDescription> coursesDescription = coursesDescriptionRepository.findById(reportOverallRequest.getCourseId());
        if(!user.isPresent()) {
            throw new Exception("User doesn't exist");
        }
        if(!coursesDescription.isPresent()){
            throw new Exception("Course/Test doesn't exist");
        }
        ReportOverall reportOverall = reportOverallRepository.findReportByCompositeId(
                reportOverallRequest.getUserId(), reportOverallRequest.getCourseId());
        if(reportOverall == null){
            reportOverall = new ReportOverall();
            reportOverall.setCourseId(coursesDescription.get());
            reportOverall.setUserId(user.get());
            reportOverall.setReportOverallPK(new ReportOverallPK(reportOverallRequest.getCourseId(), reportOverallRequest.getUserId()));
        }
        reportOverall.setStatus(reportOverallRequest.getStatus());
        reportOverall.setTotalTime(reportOverallRequest.getTotalTime());
        List<QuestionAnalysis> questionAnalysisList = reportGeneratorService.getQuestionAnalysis(user.get().getId(),
                coursesDescription.get().getId());
        double markSecured = questionAnalysisList.stream().mapToDouble(QuestionAnalysis::getMarkSecured)
                .sum();
        int correctAns = (int) questionAnalysisList.stream().filter(QuestionAnalysis::isCorrect).count();
        int unAttempt = (int) questionAnalysisList.stream().filter(p -> p.getYourAttempt().equals(QuestionStatus.NO_ANS.name())).count();
        int totalQ = questionAnalysisList.size();
        reportOverall.setScore(markSecured);
        reportOverall.setCorrect(correctAns);
        reportOverall.setInCorrect(totalQ - (correctAns + unAttempt));
        reportOverall.setUnAttempt(unAttempt);
        try{
        reportOverallRepository.save(reportOverall);}
        catch (DataIntegrityViolationException exp){
            reportOverallRepository.save(reportOverall);
        }
        return ResponseEntity.status(HttpStatus.OK).body("");
    }
}
