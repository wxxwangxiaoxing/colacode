package com.colacode.common;

import com.baomidou.mybatisplus.core.metadata.IPage;

public class PageUtil {

    public static <T, R> PageResult<R> toPageResult(IPage<T> page, java.util.List<R> records) {
        PageResult<R> result = new PageResult<>();
        result.setPageNo((int) page.getCurrent());
        result.setPageSize((int) page.getSize());
        result.setTotal(page.getTotal());
        result.setRecords(records);
        return result;
    }
}
