package com.egatetutor.backend.controller;


import com.egatetutor.backend.model.Banner;
import com.egatetutor.backend.repository.BannerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/banner")
public class BannerController {

    @Autowired
    BannerRepository bannerRepository;


    @GetMapping("/getAllBanner")
    public ResponseEntity<List<Banner>> getAllBooks() {
        List<Banner> bookList = bannerRepository.findBannerByImageUrlExists();
        return ResponseEntity.status(HttpStatus.OK).body(bookList);
    }
}
