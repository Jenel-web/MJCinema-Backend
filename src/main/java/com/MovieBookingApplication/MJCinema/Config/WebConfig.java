package com.MovieBookingApplication.MJCinema.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    //the front end links to dashboard.html and bookseat.html without the html/ folder
    //(login redirects to "dashboard.html"), so these paths are forwarded to the real files.
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/dashboard.html").setViewName("forward:/html/dashboard.html");
        registry.addViewController("/bookseat.html").setViewName("forward:/html/bookseat.html");
    }
}
