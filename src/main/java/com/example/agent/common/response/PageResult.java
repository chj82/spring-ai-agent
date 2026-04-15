package com.example.agent.common.response;

import java.util.List;

/**
 * 通用分页返回对象。
 */
public class PageResult<T> {

    /** 当前页数据列表。 */
    private List<T> records;
    /** 总记录数。 */
    private long total;
    /** 当前页码。 */
    private int pageNum;
    /** 每页条数。 */
    private int pageSize;

    public PageResult() {
    }

    public PageResult(List<T> records, long total, int pageNum, int pageSize) {
        this.records = records;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

    /**
     * 创建分页结果对象。
     *
     * @param records 当前页数据
     * @param total 总记录数
     * @param pageNum 当前页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    public static <T> PageResult<T> of(List<T> records, long total, int pageNum, int pageSize) {
        return new PageResult<>(records, total, pageNum, pageSize);
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
