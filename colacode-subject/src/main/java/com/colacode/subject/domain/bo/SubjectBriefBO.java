package com.colacode.subject.domain.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class SubjectBriefBO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String briefContent;
}
