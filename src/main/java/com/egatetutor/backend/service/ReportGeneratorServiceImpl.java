package com.egatetutor.backend.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import com.egatetutor.backend.enumType.CoursesStatus;
import com.egatetutor.backend.enumType.QuestionStatus;
import com.egatetutor.backend.enumType.QuestionType;
import com.egatetutor.backend.model.QuestionLayout;
import com.egatetutor.backend.model.ReportDetail;
import com.egatetutor.backend.model.ReportOverall;
import com.egatetutor.backend.model.responsemodel.QuestionAnalysis;
import com.egatetutor.backend.model.responsemodel.TestAnalytics;
import com.egatetutor.backend.model.responsemodel.UserRank;
import com.egatetutor.backend.repository.QuestionLayoutRepository;
import com.egatetutor.backend.repository.ReportDetailRepository;
import com.egatetutor.backend.repository.ReportOverallRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

import static java.util.stream.Collectors.*;

@Service
public class ReportGeneratorServiceImpl implements ReportGeneratorService {

    @Autowired
    ReportDetailRepository reportDetailRepository;

    @Autowired
    ReportOverallRepository reportOverallRepository;

    @Autowired
    QuestionLayoutRepository questionLayoutRepository;


    @Autowired
    private AmazonS3 s3client;

    @Value("${jsa.s3.bucket}")
    private String bucketName;

    @Override
    public List<QuestionAnalysis> getQuestionAnalysis(Long userId, Long courseId) throws Exception {
        List<ReportDetail> allUserReport = reportDetailRepository.findAllReportDetailOfAllUser(courseId);
        Map<QuestionLayout, List<ReportDetail>> questionToReportListMap =
                allUserReport.parallelStream().collect(groupingBy(ReportDetail::getQuestion_id));
        List<QuestionAnalysis> questionAnalysesList = getOverAllReportAnalysis(userId, courseId);
        return questionAnalysesList.parallelStream().peek(questionAnalysis -> {
            List<ReportDetail> questWiseAllUserReport = new ArrayList<>();
            if (questionToReportListMap.containsKey(questionAnalysis.getQuestion()))
                questWiseAllUserReport = questionToReportListMap.get(questionAnalysis.getQuestion());
            questionAnalysis.setTotalAttempt(questWiseAllUserReport.size()); /* Number of Report generated for user*/
            List<ReportDetail> correctSolutionReport = questWiseAllUserReport.stream().filter(
                    p -> checkCorrectAns(questionAnalysis.getQuestion().getQuestionType(), questionAnalysis.getQuestion().getAnswer(), p.getAnswerSubmitted())
            ).collect(toList());
            questionAnalysis.setCorrectAttempt(correctSolutionReport.size());
            int unAttemptQ = (int) questWiseAllUserReport.stream().filter(
                    p -> (p.getAnswerSubmitted() == null || p.getAnswerSubmitted().isEmpty())
            ).count();
            questionAnalysis.setUnAttempt(unAttemptQ);

            int inCorrect = questionAnalysis.getTotalAttempt() - (questionAnalysis.getCorrectAttempt() + unAttemptQ);
            questionAnalysis.setInCorrectAttempt(inCorrect);

            ReportDetail minTimeTakenReport = correctSolutionReport.stream().min(Comparator.comparing(ReportDetail::getTimeTaken))
                    .orElse(null);
            questionAnalysis.setTopperTime(minTimeTakenReport == null ? null : minTimeTakenReport.getTimeTaken());
            OptionalDouble averageTimeOptional = questWiseAllUserReport.stream().mapToDouble(p -> Double.parseDouble(p.getTimeTaken())).average();
            if (averageTimeOptional.isPresent()) {
                double averageTime = averageTimeOptional.getAsDouble();
                questionAnalysis.setAverageTime(averageTime + "");
            }
        }).collect(toList());
    }

    @Override
    public List<QuestionAnalysis> getOverAllReportAnalysis(Long userId, Long courseId) throws Exception {
        List<ReportDetail> reportDetailList = reportDetailRepository.findReportDetailListByCompositeId(userId, courseId); /*User Report detail list for all question of courseId*/
        if (reportDetailList == null) {
            throw new Exception("QuestWise report is not yet generated");
        }
        List<QuestionLayout> questionLayoutList = questionLayoutRepository.findQuestionsById(courseId);        /*Question List of Course Id */
        return questionLayoutList.parallelStream().map(question -> {
            QuestionAnalysis questionAnalysis = new QuestionAnalysis();
            questionAnalysis.setQuestion(calculatePath(question));
            questionAnalysis.setDifficultyLevel(question.getQuestionDifficulty());
            Optional<ReportDetail> reportDetail = reportDetailList.stream().                               /* finding question specific report for userId*/
                    filter(p -> p.getQuestion_id().equals(question)).
                    findFirst();
            if (!reportDetail.isPresent() || reportDetail.get().getQuestionStatus().equals(QuestionStatus.NO_ANS.name()) ||
                    reportDetail.get().getQuestionStatus().equals(QuestionStatus.MARK_NOANS.name())) {
                questionAnalysis.setYourAttempt(QuestionStatus.NO_ANS.name());
                questionAnalysis.setCorrect(false);
                questionAnalysis.setMarkSecured(0d);
                questionAnalysis.setYourAnswer("");
                if (!reportDetail.isPresent()) questionAnalysis.setYourTime("0");
                else {
                    questionAnalysis.setYourTime(reportDetail.get().getTimeTaken());
                }
            } else {
                questionAnalysis.setYourTime(reportDetail.get().getTimeTaken());
                boolean isCorrect = checkCorrectAns(question.getQuestionType(), question.getAnswer(), reportDetail.get().getAnswerSubmitted());
                double markSecured = (isCorrect) ? question.getMarks() : question.getNegativeMarks();
                questionAnalysis.setMarkSecured(markSecured);
                questionAnalysis.setCorrect(isCorrect);
                questionAnalysis.setYourAttempt(reportDetail.get().getQuestionStatus());
                questionAnalysis.setYourAnswer(reportDetail.get().getAnswerSubmitted());
            }
            return questionAnalysis;
        }).collect(toList());
    }

    @Override
    public TestAnalytics getTestAnalytics(Long userId, Long courseId) throws Exception {
        ReportOverall reportOverall = reportOverallRepository.findReportByCompositeId(userId, courseId);
        if (reportOverall == null) {
            throw new Exception("Composite Id of userId:" + userId + "& courseId:" + courseId + " doesn't exist");
        }
        if (!reportOverall.getStatus().equals(CoursesStatus.COMPLETED.name())) {
            throw new Exception("Exam is not complete");
        }
        List<ReportDetail> reportDetailList = reportDetailRepository.findReportDetailListByCompositeId(userId, courseId); /*User Report detail list for all question of courseId*/
        TestAnalytics testAnalytics = new TestAnalytics();
        testAnalytics.setCorrect(reportOverall.getCorrect());
        testAnalytics.setInCorrect(reportOverall.getInCorrect());
        testAnalytics.setUnAttempt(reportOverall.getUnAttempt());
        testAnalytics.setMarksSecured(reportOverall.getScore());
        testAnalytics.setTotalTimeTaken(reportOverall.getTotalTime());
        Map<Integer,String> questionToTimeMap =
                reportDetailList.parallelStream().
                collect(toMap(
                p->p.getQuestion_id().getQuestionLabel(),
                ReportDetail::getTimeTaken));
        testAnalytics.setQuestionToTimeTaken(questionToTimeMap);
        return testAnalytics;
    }

    @Override
    public List<UserRank> getRankWiseReport(Long courseId) {
        double[] score = {Integer.MIN_VALUE};
        long[] no = {0L};
        long[] rank = {0L};
        List<ReportOverall> reportOverallList = reportOverallRepository.findRankByCourseId(courseId);

        return reportOverallList.stream()
                .sorted(new Comparator<ReportOverall>() {
                    @Override
                    public int compare(ReportOverall o1, ReportOverall o2) {
                        return o2.getScore().compareTo(o1.getScore());
                    }
                })
                .map(p -> {
                    ++no[0];
                    if (score[0] != p.getScore()) rank[0] = no[0];
                    p.setUserRank(rank[0]);
                    score[0] = p.getScore();
                    String photo = p.getUserId().getPhoto();
                    String profileString = null;
                    if (photo != null) {
                        S3Object profileObj = s3client.getObject(new GetObjectRequest(bucketName, photo));
                        byte[] profileImage = new byte[0];
                        try {
                            profileImage = IOUtils.toByteArray(profileObj.getObjectContent());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        profileString = Base64.getEncoder().encodeToString(profileImage);
                    }
                    return new UserRank(p.getUserId().getName(), p.getUserId().getId(), p.getCourseId().getTitle(),
                            p.getCourseId().getTotalMarks(),
                            p.getScore(), p.getTotalTime(), p.getCourseId().getDuration(), p.getUserRank(), profileString

                    );
                }).collect(toList());
    }

    private boolean checkCorrectAns(String qType, String answer, String answerSubmitted) {
        if (answerSubmitted == null || answerSubmitted.isEmpty()) return false;
        QuestionType questionType = QuestionType.find(qType);
        boolean isCorrect;
        switch (questionType) {
            case MCQ:
                isCorrect = answer.equalsIgnoreCase(answerSubmitted);
                break;
            case MSQ:
                answer = answer.replaceAll("[\\[\\](){}]", "");
                answerSubmitted = answerSubmitted.replaceAll("[\\[\\](){}]", "");
                String[] a1 = answer.split("[,]", 0);
                String[] a2 = answerSubmitted.split("[,]", 0);
                Arrays.sort(a1);
                Arrays.sort(a2);
                isCorrect = Arrays.equals(a1, a2);
                break;
            case NAT:
                answer = answer.replaceAll("[\\[\\](){}]", "");
                String[] ans = answer.split("[,]", 0);
                double lowerLimit = Double.parseDouble(ans[0]);
                double upperLimit = Double.parseDouble(ans[1]);
                double ansSub = Double.parseDouble(answerSubmitted);
                isCorrect = ansSub <= upperLimit && ansSub >= lowerLimit;
                break;
            case TF:
                if(
                        answer.equalsIgnoreCase("true") ||
                        answer.equalsIgnoreCase("T") ||
                        answer.equalsIgnoreCase("Yes") ||
                                answer.equalsIgnoreCase("Y")
                ){
                    isCorrect = answerSubmitted.equalsIgnoreCase("true");
                }
                else if( answer.equalsIgnoreCase("false") ||
                        answer.equalsIgnoreCase("F") ||
                        answer.equalsIgnoreCase("No") ||
                        answer.equalsIgnoreCase("N")){
                    isCorrect = answerSubmitted.equalsIgnoreCase("false");
                }else{
                    isCorrect = false;
                }
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + questionType);
        }
        return isCorrect;
    }

    public QuestionLayout calculatePath(QuestionLayout questionLayout) {
        S3Object questObject = s3client.getObject(new GetObjectRequest(bucketName, questionLayout.getQuestion()));
        S3Object solObject = s3client.getObject(new GetObjectRequest(bucketName, questionLayout.getSolution()));
        try {
            byte[] imageque = IOUtils.toByteArray(questObject.getObjectContent());  //Files.readAllBytes(quePath);
            byte[] imagesol = IOUtils.toByteArray(solObject.getObjectContent()); // Files.readAllBytes(solPath);
            String encodedQuestion = Base64.getEncoder().encodeToString(imageque);
            String encodedSolution = Base64.getEncoder().encodeToString(imagesol);
            questionLayout.setQuestion(encodedQuestion);
            questionLayout.setSolution(encodedSolution);
        } catch (Exception exception) {
            System.out.println("" + exception);
        }
        return questionLayout;
    }
}
