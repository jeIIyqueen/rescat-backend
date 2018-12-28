package com.sopt.rescat.web.api;

import com.sopt.rescat.domain.CarePost;
import com.sopt.rescat.service.CarePostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/care-posts")
public class CarePostController {
    private CarePostService carePostService;

    public CarePostController(CarePostService carePostService) {
        this.carePostService = carePostService;
    }

//    @GetMapping("")
//    public ResponseEntity<Iterable<CarePost>> list() {
//
//    }

    @GetMapping("/{type}")
    public ResponseEntity<Iterable<CarePost>> getAllBy(@PathVariable Integer type) {
        return ResponseEntity.status(HttpStatus.OK).body(carePostService.findAllBy(type));
    }
}
