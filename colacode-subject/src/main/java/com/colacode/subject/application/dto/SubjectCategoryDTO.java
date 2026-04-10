package com.colacode.subject.application.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class SubjectCategoryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String categoryName;

    private Long parentId;

    private Integer categoryType;

    private List<SubjectCategoryDTO> children;

    private List<SubjectLabelDTO> labels;
}
