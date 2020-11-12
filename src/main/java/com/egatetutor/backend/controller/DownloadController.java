package com.egatetutor.backend.controller;

import com.egatetutor.backend.model.Download;
import com.egatetutor.backend.repository.DownloadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/download")
public class DownloadController {

    @Autowired
    DownloadRepository downloadRepository;

    @GetMapping("/getDownloadByExamAndTopic")
    public ResponseEntity<List<Download>> getDownloadByExamAndTopic(@RequestParam("exam") String exam, @RequestParam("topic") String topic) {
        List<Download> downloadList = downloadRepository.findDownloadByExamAndTopic(exam, topic);
        return ResponseEntity.status(HttpStatus.OK).body(downloadList);
    }
}
