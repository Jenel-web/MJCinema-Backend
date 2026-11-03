package com.MovieBookingApplication.MJCinema.Filters;

import jakarta.persistence.EntityManagerFactory;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Logs how long every request took, e.g.  "GET /schedule/seats?id=5 -> 200 in 12 ms, 4 queries".
 * It runs first, so the time includes security, the controller and the database.
 *
 * Turn it off with   mjcinema.timing.enabled=false
 * Count the SQL statements too with   mjcinema.timing.count-queries=true
 * (the counter is shared by the whole app, so the number is only exact while one request runs at a time).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnProperty(name = "mjcinema.timing.enabled", havingValue = "true", matchIfMissing = true)
public class TimingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TimingFilter.class);

    private final Statistics statistics; //null when query counting is off

    public TimingFilter(EntityManagerFactory entityManagerFactory,
                        @Value("${mjcinema.timing.count-queries:false}") boolean countQueries) {
        if (countQueries) {
            this.statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
            this.statistics.setStatisticsEnabled(true);
        } else {
            this.statistics = null;
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.matches(".*\\.(js|css|png|jpg|svg|ico|html)$"); //no need to time static files
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        long startQueries = statistics == null ? 0 : statistics.getPrepareStatementCount();
        long start = System.nanoTime(); //start the stopwatch
        try {
            chain.doFilter(request, response); //let the request do its normal work
        } finally { //finally runs even if the request failed
            long ms = (System.nanoTime() - start) / 1_000_000;
            String query = request.getQueryString() == null ? "" : "?" + request.getQueryString();
            String queries = statistics == null ? "" : ", " + (statistics.getPrepareStatementCount() - startQueries) + " queries";
            log.info("{} {}{} -> {} in {} ms{}", request.getMethod(), request.getRequestURI(), query,
                    response.getStatus(), ms, queries);
        }
    }
}
