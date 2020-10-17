package com.egatetutor.backend.service;

import com.egatetutor.backend.enumType.CoursesStatus;
import com.egatetutor.backend.enumType.QuestionStatus;
import com.egatetutor.backend.enumType.QuestionType;
import com.egatetutor.backend.model.QuestionLayout;
import com.egatetutor.backend.model.ReportDetail;
import com.egatetutor.backend.model.ReportOverall;
import com.egatetutor.backend.model.responsemodel.QuestionAnalysis;
import com.egatetutor.backend.model.responsemodel.UserRank;
import com.egatetutor.backend.model.responsemodel.TestAnalytics;
import com.egatetutor.backend.repository.QuestionLayoutRepository;
import com.egatetutor.backend.repository.ReportDetailRepository;
import com.egatetutor.backend.repository.ReportOverallRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportGeneratorServiceImpl implements ReportGeneratorService {

    @Autowired
    ReportDetailRepository reportDetailRepository;

    @Autowired
    ReportOverallRepository reportOverallRepository;

    @Autowired
    QuestionLayoutRepository questionLayoutRepository;

    @Override
    public List<QuestionAnalysis> getQuestionAnalysis(Long userId, Long courseId) throws Exception {
        List<ReportDetail> reportDetailList = reportDetailRepository.findReportDetailByCompositeId(userId, courseId); /*User Report detail list for all question of courseId*/
        if (reportDetailList == null) {
            throw new Exception("QuestWise report is not yet generated");
        }
        List<QuestionLayout> questionLayoutList = questionLayoutRepository.findQuestionsById(courseId);        /*Question List of Course Id */
        List<QuestionAnalysis> questionAnalysesList = new ArrayList<>();
        for (QuestionLayout question : questionLayoutList) {
            QuestionAnalysis questionAnalysis = new QuestionAnalysis();
            questionAnalysis.setQuestion(calculatePath(question));
            questionAnalysis.setDifficultyLevel(question.getQuestionDifficulty());
            Optional<ReportDetail> reportDetail = reportDetailList.stream().                               /* finding question specific report for userId*/
                    filter(p -> p.getQuestion_id().equals(question)).
                    findFirst();
            if (!reportDetail.isPresent()) {
                questionAnalysis.setYourTime("0");
                questionAnalysis.setYourAttempt(QuestionStatus.NO_ANS.name());
                questionAnalysis.setCorrect(false);
                questionAnalysis.setMarkSecured(0d);
                questionAnalysis.setYourAnswer("");
            } else {
                questionAnalysis.setYourTime(reportDetail.get().getTimeTaken());
                if (reportDetail.get().getQuestionStatus().equals(QuestionStatus.NO_ANS.name()) ||
                        reportDetail.get().getQuestionStatus().equals(QuestionStatus.MARK_NOANS.name())) {
                    questionAnalysis.setMarkSecured(0d);
                    questionAnalysis.setCorrect(false);
                    questionAnalysis.setYourAttempt(QuestionStatus.NO_ANS.name());
                    questionAnalysis.setYourAnswer("");
                } else {
                    boolean isCorrect = checkCorrectAns(question.getQuestionType(), question.getAnswer(), reportDetail.get().getAnswerSubmitted());
                    double markSecured = (isCorrect) ? question.getMarks() : question.getNegativeMarks();
                    questionAnalysis.setMarkSecured(markSecured);
                    questionAnalysis.setCorrect(isCorrect);
                    questionAnalysis.setYourAttempt(reportDetail.get().getQuestionStatus());
                    questionAnalysis.setYourAnswer(reportDetail.get().getAnswerSubmitted());
                }
            }
            List<ReportDetail> questWiseAllUserReport = reportDetailRepository.findAllReportDetailByQuestion(question.getId(), courseId);
            questionAnalysis.setTotalAttempt(questWiseAllUserReport.size()); /* Number of Report generated for user*/

            List<ReportDetail> correctSolutionReport = questWiseAllUserReport.stream().filter(
                    p -> checkCorrectAns(question.getQuestionType(), question.getAnswer(), p.getAnswerSubmitted())
            ).collect(Collectors.toList());
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
            questionAnalysesList.add(questionAnalysis);
        }
        return questionAnalysesList;
    }

    @Override
    public TestAnalytics getTestAnalytics(Long userId, Long courseId)  throws Exception{
       ReportOverall reportOverall = reportOverallRepository.findReportByCompositeId(userId, courseId);
        if(reportOverall == null){
            throw new Exception("Composite Id of userId:"+ userId +"& courseId:"+courseId+" doesn't exist");
        }
        if(!reportOverall.getStatus().equals(CoursesStatus.COMPLETED.name())){
            throw new Exception("Exam is not complete");
        }
        TestAnalytics testAnalytics = new TestAnalytics();
        testAnalytics.setCorrect(reportOverall.getCorrect());
        testAnalytics.setInCorrect(reportOverall.getInCorrect());
        testAnalytics.setUnAttempt(reportOverall.getUnAttempt());
        testAnalytics.setMarksSecured(reportOverall.getScore());
        testAnalytics.setTotalTimeTaken(reportOverall.getTotalTime());
        return testAnalytics;
    }

    @Override
    public List<UserRank> getRankWiseReport(Long courseId) {
        double[] score = {Integer.MIN_VALUE};
        long[] no = {0L};
        long[] rank = {0L};
        List<ReportOverall> reportOverallList = reportOverallRepository.findRankByCourseId(courseId);
        List<UserRank> userRankList = reportOverallList.stream()
                .sorted((a, b) -> (int) (b.getScore() - a.getScore()))
                .map(p -> {
                    ++no[0];
                    if (score[0] != p.getScore()) rank[0] = no[0];
                    p.setUserRank(rank[0]);
                    score[0] = p.getScore();
                    return new UserRank(p.getUserId().getName(),p.getUserId().getId(), p.getCourseId().getTitle(),
                            p.getCourseId().getTotalMarks(),
                            p.getScore(), p.getTotalTime(), p.getCourseId().getDuration(), p.getUserRank());
                }).collect(Collectors.toList());

        return userRankList;
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
            default:
                throw new IllegalStateException("Unexpected value: " + questionType);
        }
        return isCorrect;
    }

    public QuestionLayout calculatePath(QuestionLayout questionLayout) throws IOException {
        Path quePath = Paths.get(questionLayout.getQuestion() + ".png");
        Path solPath = Paths.get(questionLayout.getSolution() + ".png");
        byte[] imageque = Files.readAllBytes(quePath);
        byte[] imagesol = Files.readAllBytes(solPath);
        String encodedQuestion = Base64.getEncoder().encodeToString(imageque);
        String encodedSolution = Base64.getEncoder().encodeToString(imagesol);
        questionLayout.setQuestion(encodedQuestion);
        questionLayout.setSolution(encodedSolution);
        return  questionLayout;
    }
}
