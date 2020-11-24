package com.egatetutor.backend.service;

import com.egatetutor.backend.model.responsemodel.QuestionAnalysis;
import com.egatetutor.backend.model.responsemodel.UserRank;
import com.egatetutor.backend.model.responsemodel.TestAnalytics;

import java.util.List;

public interface ReportGeneratorService {
    List<QuestionAnalysis> getQuestionAnalysis(Long userId, Long courseId) throws Exception;
    List<QuestionAnalysis> getOverAllReportAnalysis(Long userId, Long courseId) throws Exception;
    TestAnalytics getTestAnalytics(Long userId, Long courseId) throws Exception;
    List<UserRank> getRankWiseReport(Long courseId);
}
