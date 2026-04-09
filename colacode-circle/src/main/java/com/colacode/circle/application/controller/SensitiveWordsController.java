package com.colacode.circle.application.controller;

import com.colacode.circle.application.converter.CircleDTOConverter;
import com.colacode.circle.application.dto.SensitiveWordCreateDTO;
import com.colacode.circle.application.dto.SensitiveWordDTO;
import com.colacode.circle.domain.bo.SensitiveWordBO;
import com.colacode.circle.domain.service.CircleDomainService;
import com.colacode.common.Result;
import com.colacode.common.enums.ResultCodeEnum;
import com.colacode.common.exception.BusinessException;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/circle/sensitive")
public class SensitiveWordsController {

    private final CircleDomainService circleDomainService;

    public SensitiveWordsController(CircleDomainService circleDomainService) {
        this.circleDomainService = circleDomainService;
    }

    @PostMapping("/add")
    public Result<Void> addSensitiveWord(@Valid @RequestBody SensitiveWordCreateDTO createDTO) {
        circleDomainService.addSensitiveWord(CircleDTOConverter.INSTANCE.toSensitiveWordBO(createDTO));
        return Result.success();
    }

    @PostMapping("/delete")
    public Result<Void> deleteSensitiveWord(@RequestBody SensitiveWordDTO wordDTO) {
        if (wordDTO.getId() == null) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "敏感词ID不能为空");
        }
        circleDomainService.deleteSensitiveWord(wordDTO.getId());
        return Result.success();
    }

    @GetMapping("/list")
    public Result<List<SensitiveWordDTO>> listSensitiveWords() {
        List<SensitiveWordBO> boList = circleDomainService.listSensitiveWords();
        return Result.success(CircleDTOConverter.INSTANCE.toSensitiveWordDTOList(boList));
    }
}
