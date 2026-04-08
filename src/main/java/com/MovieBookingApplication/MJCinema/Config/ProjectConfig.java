    package com.MovieBookingApplication.MJCinema.Config;

    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.web.client.RestTemplate;

    @Configuration
    public class ProjectConfig {
        @Bean //make it a bean so that our system can see it and process.
        public RestTemplate restTemplate() {
            return new RestTemplate();
        }
    }