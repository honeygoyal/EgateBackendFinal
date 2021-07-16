package com.egatetutor.backend.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import com.egatetutor.backend.enumType.AttributeType;
import com.egatetutor.backend.model.questionbank.CoursesDescriptionQB;
import com.egatetutor.backend.model.questionbank.QuestionBank;
import com.egatetutor.backend.repository.*;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.io.FileUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.usermodel.*;
import org.modelmapper.ModelMapper;
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
import java.util.stream.Collectors;


@RestController
@RequestMapping("/questionBank")
public class QuestionBankController {

    @Autowired
    private QuestionBankRepository questionBankRepository;

    @Autowired
    private CoursesDescriptionQBRepository coursesDescriptionQBRepository;

    @Autowired
    private Environment env;

    @Autowired
    private AmazonS3 s3client;

    @Value("${jsa.s3.bucket}")
    private String bucketName;

    @GetMapping("/getQuestions")
    public ResponseEntity<Map<String, List<QuestionBank>>> getQuestionForTest(@RequestParam("courseId") String courseId,
                                                                                        @RequestParam("userId") Long userId)
            throws Exception {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        Optional<CoursesDescriptionQB> coursesDescription= coursesDescriptionQBRepository.findCoursesDescriptionByCourseId(courseId);
        if(!coursesDescription.isPresent()){
            throw new Exception("courseId is not present. Please create test");
        }
        List<QuestionBank> questionList = questionBankRepository.findQuestionsById(coursesDescription.get().getId());
        if(questionList == null || questionList.size() == 0){
            throw new Exception("Questions doesn't exits");
        }

        Map<String, List<QuestionBank>> questionMap  = new HashMap<>();

        for (QuestionBank questionLayout : questionList) {
            S3Object questObject = s3client.getObject(new GetObjectRequest(bucketName, questionLayout.getQuestion()));
            S3Object solObject = s3client.getObject(new GetObjectRequest(bucketName, questionLayout.getSolution()));
            byte[] imageque =   IOUtils.toByteArray(questObject.getObjectContent());  //Files.readAllBytes(quePath);
            byte[] imagesol = IOUtils.toByteArray(solObject.getObjectContent()); // Files.readAllBytes(solPath);
            String encodedQuestion = Base64.getEncoder().encodeToString(imageque);
            String encodedSolution = Base64.getEncoder().encodeToString(imagesol);
            List<QuestionBank> tempList = new ArrayList<>();
            questionLayout.setQuestion(encodedQuestion);
            questionLayout.setSolution(encodedSolution);

            if (questionMap.containsKey(questionLayout.getSection())) {
                tempList = questionMap.get(questionLayout.getSection());
                tempList.add(questionLayout);
                questionMap.put(questionLayout.getSection(), tempList);
            } else {
                tempList.add(questionLayout);
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
            String BASE_URL = env.getProperty("image_question_bank_url");
            XWPFDocument xdoc = new XWPFDocument(OPCPackage.open(testFile));
            Iterator bodyElementIterator = xdoc.getBodyElementsIterator();
            List<QuestionBank> questionsList = new ArrayList();
            QuestionBank question = new QuestionBank();
            Optional<CoursesDescriptionQB> t = coursesDescriptionQBRepository.findCoursesDescriptionByCourseId(courseId);
            CoursesDescriptionQB coursesDescription = null;
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
                            }

                            k++;
                            if (k == 7) {
                                questionsList.add(question);
                                k = 1;
                                question = new QuestionBank();
                                question.setCourseId(coursesDescription);
                            }
                        }
                    }
                }
                if(!isTableExist){
                    throw new InvalidFormatException("Table doesn't exist");
                }

            }
            for(QuestionBank q: questionsList){
                questionBankRepository.save(q);
            }

        } catch (IOException | InvalidFormatException e) {
            e.printStackTrace();
            return new ResponseEntity<String>("Failed ", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<String>("{}", HttpStatus.OK);
    }

}