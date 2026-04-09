package com.colacode.subject.domain.bo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SubjectCategoryBO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String categoryName;

    private Long parentId;

    private Integer categoryType;

    private String extJson;

    private List<SubjectCategoryBO> children;
}
