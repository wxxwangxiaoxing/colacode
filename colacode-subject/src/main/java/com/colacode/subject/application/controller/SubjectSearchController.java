package com.colacode.subject.application.controller;

import com.colacode.common.PageResult;
import com.colacode.common.Result;
import com.colacode.subject.infra.es.SubjectEsDTO;
import com.colacode.subject.infra.es.SubjectEsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/subject/search")
@Tag(name = "题目搜索", description = "Elasticsearch题目搜索")
public class SubjectSearchController {

    private final SubjectEsService subjectEsService;

    public SubjectSearchController(SubjectEsService subjectEsService) {
        this.subjectEsService = subjectEsService;
    }

    @GetMapping("/query")
    @Operation(summary = "搜索题目", description = "使用Elasticsearch搜索题目")
    public Result<PageResult<SubjectEsDTO>> search(
            @Parameter(description = "搜索关键字") @RequestParam(required = false) String keyword,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNo,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize) {
        PageResult<SubjectEsDTO> pageResult = subjectEsService.search(keyword, pageNo, pageSize);
        return Result.success(pageResult);
    }
}
