package com.example.agent.common.request;

/**
 * 分页请求工具类。
 */
public final class PageRequestUtils {

    private PageRequestUtils() {
    }

    /**
     * 规范化分页参数。
     *
     * @param pageRequest 原始分页参数
     * @return 规范化后的分页参数
     */
    public static PageRequest normalize(PageRequest pageRequest) {
        PageRequest normalized = new PageRequest();
        normalized.setPageNum(pageRequest == null || pageRequest.getPageNum() <= 0 ? 1 : pageRequest.getPageNum());
        int pageSize = pageRequest == null ? 10 : pageRequest.getPageSize();
        normalized.setPageSize(pageSize <= 0 ? 10 : Math.min(pageSize, 100));
        return normalized;
    }
}
