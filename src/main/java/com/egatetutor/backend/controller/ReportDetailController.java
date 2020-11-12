package com.egatetutor.backend.controller;


import com.egatetutor.backend.enumType.CoursesStatus;
import com.egatetutor.backend.model.*;
import com.egatetutor.backend.model.compositekey.ReportDetailPK;
import com.egatetutor.backend.model.compositekey.ReportOverallPK;
import com.egatetutor.backend.model.responsemodel.QuestionAnalysis;
import com.egatetutor.backend.model.responsemodel.ReportDetailRequest;
import com.egatetutor.backend.repository.*;
import com.egatetutor.backend.service.ReportGeneratorService;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.*;


@RestController
@RequestMapping("/reportDetail")
public class ReportDetailController {
    @Autowired
    ReportDetailRepository reportDetailRepository;

    @Autowired
    ReportOverallRepository reportOverallRepository;

    @Autowired
    QuestionLayoutRepository questionLayoutRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CoursesDescriptionRepository coursesDescriptionRepository;

    @Autowired
    ReportGeneratorService reportGeneratorService;


    @GetMapping("/getQuestionAnalysis")
    public List<QuestionAnalysis> getQuestionAnalysis(@RequestParam("user_id") Long userId,
                                                      @RequestParam("course_id") Long courseId) throws Exception {
        ReportOverall reportOverall = reportOverallRepository.findReportByCompositeId(userId, courseId);
        if (reportOverall == null || !reportOverall.getStatus().equals(CoursesStatus.COMPLETED.name()))
            throw new Exception("Exam is not finished yet");
        return reportGeneratorService.getQuestionAnalysis(userId, courseId);
    }

    @PostMapping("/saveReportQuestionWise")
    public ResponseEntity<ReportDetail> saveReportQuestionWise(@Valid @RequestBody ReportDetailRequest reportDetailRequest)
            throws Exception {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        Optional<ReportDetail> reportDetailOptional = reportDetailRepository.findReportDetailByCompositeId(reportDetailRequest.getUserId(), reportDetailRequest.getCourseId()
        , reportDetailRequest.getQuestionId());

        Optional<UserInfo> user = userRepository.findById(reportDetailRequest.getUserId());
        Optional<CoursesDescription> coursesDescription = coursesDescriptionRepository.findById(reportDetailRequest.getCourseId());
        Optional<QuestionLayout> questionLayout = questionLayoutRepository.findById(reportDetailRequest.getQuestionId());
        if (!questionLayout.isPresent()) {
            throw new Exception("Question id is invalid");
        }
        if (!user.isPresent()) {
            throw new Exception("User doesn't exist");
        }
        if (!coursesDescription.isPresent()) {
            throw new Exception("Course/Test doesn't exist");
        }
        ReportOverall reportOverall = reportOverallRepository.findReportByCompositeId(reportDetailRequest.getUserId(),
                reportDetailRequest.getCourseId());
        if (reportOverall == null) {
            reportOverall = new ReportOverall();
            reportOverall.setUserId(user.get());
            reportOverall.setCourseId(coursesDescription.get());
            reportOverall.setStatus(CoursesStatus.PENDING.name());
            reportOverall.setReportOverallPK(new ReportOverallPK(coursesDescription.get().getId(), user.get().getId()));
            reportOverall.setTotalTime(reportDetailRequest.getTimeTaken());
        }else{
            String timeString = reportOverall.getTotalTime();
            long prevTime = timeString.isEmpty() ? 0: Long.parseLong(timeString);
            long currTime = reportDetailRequest.getTimeTaken().isEmpty() ? 0 : Long.parseLong(reportDetailRequest.getTimeTaken());
            long totalTime = currTime + prevTime;
            reportOverall.setTotalTime( totalTime+ "");
        }
        reportOverallRepository.save(reportOverall);
        ReportDetail reportDetail;
        if(reportDetailOptional.isPresent()){
            reportDetail = reportDetailOptional.get();
            reportDetail.setAnswerSubmitted(reportDetailRequest.getAnswerSubmitted());
            reportDetail.setQuestionStatus(reportDetailRequest.getQuestionStatus());
            long prevTimeTaken = reportDetail.getTimeTaken().isEmpty() ? 0: Long.parseLong(reportDetail.getTimeTaken());
            long currTime = reportDetailRequest.getTimeTaken().isEmpty() ? 0 : Long.parseLong(reportDetailRequest.getTimeTaken());
            long totalTime = currTime + prevTimeTaken;
            reportDetail.setTimeTaken(totalTime + "");
        }else{
            reportDetail = modelMapper.map(reportDetailRequest, ReportDetail.class);
            reportDetail.setReportId(reportOverall);
            reportDetail.setQuestion_id(questionLayout.get());
            reportDetail.setReportDetailPK(new ReportDetailPK(reportOverall.getReportOverallPK(), questionLayout.get().getId()));
        }
        return ResponseEntity.status(HttpStatus.OK).body(reportDetailRepository.save(reportDetail));
    }


}
