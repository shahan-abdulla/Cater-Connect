package com.catering.model;

import java.util.List;

public class ReportResult {
    private final List<String> headers;
    private final List<Object[]> rows;

    public ReportResult(List<String> headers, List<Object[]> rows) {
        this.headers = headers;
        this.rows = rows;
    }

    public List<String> getHeaders() { return headers; }
    public List<Object[]> getRows() { return rows; }
}
