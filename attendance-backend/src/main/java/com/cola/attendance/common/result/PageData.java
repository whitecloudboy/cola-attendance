package com.cola.attendance.common.result;

import lombok.Data;

import java.util.List;

@Data
public class PageData<T> {
    private List<T> list;
    private long total;

    public PageData(List<T> list, long total) {
        this.list = list;
        this.total = total;
    }
}
