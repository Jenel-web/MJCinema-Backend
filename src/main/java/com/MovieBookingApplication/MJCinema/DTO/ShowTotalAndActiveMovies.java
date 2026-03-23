package com.MovieBookingApplication.MJCinema.DTO;

public class ShowTotalAndActiveMovies {
    private Long totalCount;
    private Long activeCount;

    public ShowTotalAndActiveMovies(Long activeCount, Long totalCount) {
        this.totalCount = activeCount;
        this.activeCount = totalCount;
    }

    public Long getActiveCount() {
        return activeCount;
    }

    public void setActiveCount(Long activeCount) {
        this.activeCount = activeCount;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }
}
