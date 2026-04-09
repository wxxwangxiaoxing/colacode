package com.colacode.interview.application.dto.session;

import lombok.Data;

import java.io.Serializable;

@Data
public class SessionActionReqDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long sessionId;
}