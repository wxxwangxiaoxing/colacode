package com.colacode.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private int pageNo;
    private int pageSize;
    private long total;
    private java.util.List<T> records;

    public PageResult() {
    }

    public PageResult(int pageNo, int pageSize, long total, java.util.List<T> records) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.total = total;
        this.records = records;
    }
}
