package com.sportzone.common.dto;

import java.util.List;

public class PageResult<T> {

    private long total;
    private long page;
    private long size;
    private long pages;
    private List<T> records;

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
    public long getPage() { return page; }
    public void setPage(long page) { this.page = page; }
    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }
    public long getPages() { return pages; }
    public void setPages(long pages) { this.pages = pages; }
    public List<T> getRecords() { return records; }
    public void setRecords(List<T> records) { this.records = records; }
}