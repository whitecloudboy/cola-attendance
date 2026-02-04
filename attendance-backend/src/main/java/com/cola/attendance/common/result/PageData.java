package com.cola.attendance.common.result;

import lombok.Data;

import java.util.List;

/**
 * 分页结果封装。
 *
 * @param <T> 当前页数据元素类型
 */
@Data
public class PageData<T> {
    /** 当前页数据列表 */
    private List<T> list;
    /** 总记录数 */
    private long total;

    public PageData(List<T> list, long total) {
        this.list = list;
        this.total = total;
    }
}
