package com.egatetutor.backend.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.egatetutor.backend.model.Download;
import com.egatetutor.backend.model.responsemodel.DownloadRequest;
import com.egatetutor.backend.repository.DownloadRepository;
import io.swagger.annotations.ApiOperation;
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
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("/download")
public class DownloadController {

    @Autowired
    DownloadRepository downloadRepository;
    @Autowired
    private AmazonS3 s3client;

    @Value("${aws.download.bucket}")
    private String bucketName;

    @Autowired
    private Environment env;
    @GetMapping("/getDownloadByExamAndTopicAndBranch")
    public ResponseEntity<List<Download>> getDownloadByExamAndTopicAndBranch(@RequestParam("exam") String exam, @RequestParam("subsection") String subsection,
                                                                    @RequestParam("branch") String branch) {
        List<Download> downloadList = downloadRepository.findDownloadByExamAndSubsectionAndBranch(exam, subsection, branch);
        return ResponseEntity.status(HttpStatus.OK).body(downloadList);
    }

    @PostMapping("/uploadMaterial")
    @ApiOperation(value = "Make a POST request to upload the file",
            produces = "text/plain", consumes = MediaType.MULTIPART_MIXED_VALUE)
    @RequestMapping(value = "uploadMaterial", method = RequestMethod.POST)
    public ResponseEntity uploadMaterial(
            @RequestPart(value = "label1File", required = true) MultipartFile label1File,
            @RequestPart(value = "label2File", required = true) MultipartFile label2File,
            DownloadRequest downloadRequest
            ) throws IOException {
        String BASE_URL = env.getProperty("download_url");
        InputStream stream = new ByteArrayInputStream(label1File.getBytes());
        ObjectMetadata meta = new ObjectMetadata();
        String fileName1 = downloadRequest.getExam() +"_"+downloadRequest.getBranch()+"_"
                +downloadRequest.getSubsection()+"_"+downloadRequest.getTopic()
                +"_"+downloadRequest.getLabel1();
        s3client.putObject(new PutObjectRequest(
                bucketName, fileName1, stream, meta)
                .withCannedAcl(CannedAccessControlList.Private));
        stream.close();
        InputStream stream2 = new ByteArrayInputStream(label2File.getBytes());
        ObjectMetadata meta2 = new ObjectMetadata();
        String fileName2 = downloadRequest.getExam() +"_"+downloadRequest.getBranch()+"_"
                +downloadRequest.getSubsection()+"_"+downloadRequest.getTopic()
                +"_"+downloadRequest.getLabel2();
        s3client.putObject(new PutObjectRequest(
                bucketName, fileName2, stream2, meta2)
                .withCannedAcl(CannedAccessControlList.Private));
        stream2.close();
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        Download download = modelMapper.map(downloadRequest, Download.class);
        download.setURL1(BASE_URL+"/"+fileName1);
        download.setURL2(BASE_URL+"/"+fileName2);
        downloadRepository.save(download);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}

