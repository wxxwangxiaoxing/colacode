package com.colacode.subject.application.controller;

import com.colacode.common.PageResult;
import com.colacode.common.Result;
import com.colacode.subject.infra.es.SubjectEsDTO;
import com.colacode.subject.infra.es.SubjectEsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/subject/search")
public class SubjectSearchController {

    private final SubjectEsService subjectEsService;

    public SubjectSearchController(SubjectEsService subjectEsService) {
        this.subjectEsService = subjectEsService;
    }

    @GetMapping("/query")
    public Result<PageResult<SubjectEsDTO>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageResult<SubjectEsDTO> pageResult = subjectEsService.search(keyword, pageNo, pageSize);
        return Result.success(pageResult);
    }
}
