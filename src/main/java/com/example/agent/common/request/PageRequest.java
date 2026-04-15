package com.example.agent.common.request;

/**
 * 通用分页请求对象。
 */
public class PageRequest {

    /** 当前页码，从1开始。 */
    private int pageNum;
    /** 每页条数。 */
    private int pageSize;

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

    /**
     * 计算数据库分页偏移量。
     *
     * @return 偏移量
     */
    public int getOffset() {
        return Math.max(pageNum - 1, 0) * pageSize;
    }
}
