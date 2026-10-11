package com.irctc.util;

import com.irctc.model.PageLink;

import java.util.ArrayList;
import java.util.List;

/** Page numbers for the tables. */
public final class Pagination {
    private Pagination() {
    }

    public static int clamp(int page, int totalPages) {
        if (page < 1) {
            return 1;
        }
        if (page > totalPages) {
            return totalPages;
        }
        return page;
    }

    public static int totalPages(int totalItems, int pageSize) {
        if (totalItems <= 0) {
            return 1;
        }
        return (totalItems + pageSize - 1) / pageSize;
    }

    public static int offset(int page, int pageSize) {
        return (page - 1) * pageSize;
    }

    public static List<PageLink> links(String path, int page, int totalPages, String extraQuery) {
        List<PageLink> links = new ArrayList<>();
        links.add(new PageLink("Previous", url(path, page - 1, extraQuery), false, page <= 1));
        int start = Math.max(1, page - 2);
        int end = Math.min(totalPages, start + 4);
        start = Math.max(1, end - 4);
        for (int number = start; number <= end; number++) {
            links.add(new PageLink(Integer.toString(number), url(path, number, extraQuery), number == page, false));
        }
        links.add(new PageLink("Next", url(path, page + 1, extraQuery), false, page >= totalPages));
        return links;
    }

    private static String url(String path, int page, String extraQuery) {
        StringBuilder builder = new StringBuilder(path).append("?page=").append(Math.max(page, 1));
        if (extraQuery != null && !extraQuery.isBlank()) {
            builder.append('&').append(extraQuery);
        }
        return builder.toString();
    }
}
