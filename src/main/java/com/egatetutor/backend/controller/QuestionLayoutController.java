package com.egatetutor.backend.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import com.egatetutor.backend.enumType.AttributeType;
import com.egatetutor.backend.model.CoursesDescription;
import com.egatetutor.backend.model.QuestionLayout;
import com.egatetutor.backend.model.ReportDetail;
import com.egatetutor.backend.model.responsemodel.QuestionLayoutResponse;
import com.egatetutor.backend.repository.CoursesDescriptionRepository;
import com.egatetutor.backend.repository.QuestionLayoutRepository;
import com.egatetutor.backend.repository.ReportDetailRepository;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.io.FileUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.usermodel.*;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/questionLayout")
public class QuestionLayoutController {

    @Autowired
    private QuestionLayoutRepository questionRepository;

    @Autowired
    private CoursesDescriptionRepository coursesDescriptionRepository;

    @Autowired
    private ReportDetailRepository reportDetailRepository;

    @Autowired
    private Environment env;

    @Autowired
    private AmazonS3 s3client;

    @Value("${jsa.s3.bucket}")
    private String bucketName;

    @GetMapping("/getQuestions")
    public ResponseEntity<Map<String, List<QuestionLayoutResponse>>> getQuestionForTest(@RequestParam("courseId") String courseId,
                                                                                        @RequestParam("userId") Long userId)
            throws Exception {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        Optional<CoursesDescription> coursesDescription= coursesDescriptionRepository.findCoursesDescriptionByCourseId(courseId);
        if(!coursesDescription.isPresent()){
            throw new Exception("courseId is not present. Please create test");
        }
        List<QuestionLayout> questionList = questionRepository.findQuestionsById(coursesDescription.get().getId());
        if(questionList == null || questionList.size() == 0){
            throw new Exception("Questions doesn't exits");
        }
        List<QuestionLayoutResponse> questionLayoutResponseList = modelMapper.map(questionList, new TypeToken<List<QuestionLayoutResponse>>() {}.getType());

        Map<String, List<QuestionLayoutResponse>> questionMap  = new HashMap<>();
        List<ReportDetail> reportDetailList = reportDetailRepository.findReportDetailListByCompositeId(userId, coursesDescription.get().getId());
        String[] totalTime = new String[1];
        totalTime[0] = "0";
        for (QuestionLayoutResponse questionLayout : questionLayoutResponseList) {
            S3Object questObject = s3client.getObject(new GetObjectRequest(bucketName, questionLayout.getQuestion()));
            S3Object solObject = s3client.getObject(new GetObjectRequest(bucketName, questionLayout.getSolution()));
            byte[] imageque =   IOUtils.toByteArray(questObject.getObjectContent());  //Files.readAllBytes(quePath);
            byte[] imagesol = IOUtils.toByteArray(solObject.getObjectContent()); // Files.readAllBytes(solPath);
            String encodedQuestion = Base64.getEncoder().encodeToString(imageque);
            String encodedSolution = Base64.getEncoder().encodeToString(imagesol);
            List<QuestionLayoutResponse> tempList = new ArrayList<>();
            questionLayout.setQuestion(encodedQuestion);
            questionLayout.setSolution(encodedSolution);
            if(reportDetailList!=null){
                Optional<ReportDetail> reportDetail = reportDetailList.stream().filter(p->p.getQuestion_id().getId() == questionLayout.getId()).findFirst();
                reportDetail.ifPresent(detail ->
                {questionLayout.setAnswerSubmitted(detail.getAnswerSubmitted());
                questionLayout.setQuestionStatus(detail.getQuestionStatus());
                questionLayout.setTimeTaken(detail.getTimeTaken());
                questionLayout.setTotalTimeTaken(detail.getReportId().getTotalTime());
                totalTime[0] = detail.getReportId().getTotalTime();
                });
            }

            if (questionMap.containsKey(questionLayout.getSection())) {
                tempList = questionMap.get(questionLayout.getSection());
                tempList.add(questionLayout);
                questionMap.put(questionLayout.getSection(), tempList);
            } else {
                tempList.add(questionLayout);
                if(!totalTime[0].isEmpty()){
                    tempList.get(0).setTotalTimeTaken(totalTime[0]);
                }
                questionMap.put(questionLayout.getSection(), tempList);
            }
        }

        return ResponseEntity.status(HttpStatus.OK).body(questionMap);
    }

    @PostMapping("/upload")
    @ApiOperation(value = "Make a POST request to upload the file",
            produces = "text/plain", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequestMapping(value = "upload", method = RequestMethod.POST)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The POST call is Successful"),
            @ApiResponse(code = 500, message = "The POST call is Failed"),
            @ApiResponse(code = 404, message = "The API could not be found")
    })
    public ResponseEntity<String> uploadFile(
            @ApiParam(name = "file", value = "Select the file to Upload", required = true)
            @RequestPart("file") MultipartFile file,
            String courseId
    )  {
        try{
            File testFile = new File("test");
            FileUtils.writeByteArrayToFile(testFile, file.getBytes());
            String BASE_URL = env.getProperty("image_base_url");
            XWPFDocument xdoc = new XWPFDocument(OPCPackage.open(testFile));
            Iterator bodyElementIterator = xdoc.getBodyElementsIterator();
            List<QuestionLayout> questionsList = new ArrayList();
            QuestionLayout question = new QuestionLayout();
            Optional<CoursesDescription> t = coursesDescriptionRepository.findCoursesDescriptionByCourseId(courseId);
            CoursesDescription coursesDescription = null;
            if (!t.isPresent()) {
                throw new NoSuchElementException("No such course is present");
            }
            coursesDescription = t.get();
            question.setCourseId(coursesDescription);
            boolean isTableExist = false;
            while (bodyElementIterator.hasNext()) {
                IBodyElement element = (IBodyElement) bodyElementIterator.next();

                if ("TABLE".equalsIgnoreCase(element.getElementType().name())) {
                    isTableExist = true;
                    List<XWPFTable> tableList = element.getBody().getTables();
                    if(tableList == null || tableList.size() == 0){
                        throw new InvalidFormatException("Table is in invalid format");
                    }
                    for (XWPFTable table : tableList) {
                        int k = 1;
                        for (int i = 0; i < table.getRows().size(); i++) {
                            String typeString = table.getRow(i).getCell(0).getText();
                            String actualText = table.getRow(i).getCell(1).getText();
                            AttributeType type = AttributeType.find(typeString);
                            String s = "";
                            switch (type) {
                                case SECTION:
                                    question.setSection(actualText);
                                    break;
                                case MARKS:
                                    question.setMarks(Double.parseDouble(actualText));
                                    break;
                                case NEGATIVE_MARKS:
                                    question.setNegativeMarks(Double.parseDouble(actualText));
                                    break;
                                case QUESTION_TYPE:
                                    question.setQuestionType(actualText);
                                    break;
                                case QUESTION_LABEL:
                                    question.setQuestionLabel(Integer.parseInt(typeString));
                                    String fileName = BASE_URL+question.getCourseId().getCourseId() + "_" + question.getSection() + "_Question_" + question.getQuestionLabel();
                                    for (XWPFParagraph p : table.getRow(i).getCell(1).getParagraphs()) {
                                        for (XWPFRun run : p.getRuns()) {
                                            for (XWPFPicture pic : run.getEmbeddedPictures()) {
                                                byte[] pictureData = pic.getPictureData().getData();
                                                InputStream stream = new ByteArrayInputStream(pictureData);
                                                ObjectMetadata meta = new ObjectMetadata();
                                                meta.setContentLength(pictureData.length);
                                                meta.setContentType("image/png");
                                                s3client.putObject(new PutObjectRequest(
                                                        bucketName, fileName, stream, meta)
                                                        .withCannedAcl(CannedAccessControlList.Private));
                                                stream.close();
                                            }
                                        }
                                    }
                                    question.setQuestion(fileName);
                                    break;
                                case ANSWER:
                                    question.setAnswer(actualText);
                                    break;
                                case SOLUTION:
                                    String sFilename = BASE_URL+ question.getCourseId().getCourseId()  + "_" + question.getSection() + "_Solution_" + question.getQuestionLabel();
                                    for (XWPFParagraph p : table.getRow(i).getCell(1).getParagraphs()) {
                                        for (XWPFRun run : p.getRuns()) {
                                            for (XWPFPicture pic : run.getEmbeddedPictures()) {
                                                byte[] pictureData = pic.getPictureData().getData();
                                                InputStream stream = new ByteArrayInputStream(pictureData);
                                                ObjectMetadata meta = new ObjectMetadata();
                                                meta.setContentLength(pictureData.length);
                                                meta.setContentType("image/png");
                                                s3client.putObject(new PutObjectRequest(
                                                        bucketName, sFilename, stream, meta)
                                                        .withCannedAcl(CannedAccessControlList.Private));
                                                stream.close();
                                            }
                                        }
                                    }
                                    question.setSolution(sFilename);
                                    break;
                                case DIFFICULTY:
                                    question.setQuestionDifficulty(actualText);
                                    break;
                                case VIDEO_LINK:
                                    question.setVideoLink(actualText);
                                    break;
                            }

                            k++;
                            if (k == 10) {
                                questionsList.add(question);
                                k = 1;
                                question = new QuestionLayout();
                                question.setCourseId(coursesDescription);
                            }
                        }
                    }
                }
                if(!isTableExist){
                    throw new InvalidFormatException("Table doesn't exist");
                }

            }


            for(QuestionLayout q: questionsList){
                questionRepository.save(q);
            }

        } catch (IOException | InvalidFormatException e) {
            e.printStackTrace();
            return new ResponseEntity<String>("Failed ", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<String>("{}", HttpStatus.OK);
    }



}
