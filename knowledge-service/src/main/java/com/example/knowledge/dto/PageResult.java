// PageResult.java - 如果不存在需要创建这个类
package com.example.knowledge.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult<T> {
    private List<T> data;
    private long total;
    private int page;
    private int size;

    public static <T> PageResult<T> of(List<T> data, long total, int page, int size) {
        return new PageResult<>(data, total, page, size);
    }
}