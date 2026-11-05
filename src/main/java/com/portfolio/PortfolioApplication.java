package com.portfolio;

import com.portfolio.model.Project;
import com.portfolio.repo.ProjectRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootApplication
public class PortfolioApplication implements WebMvcConfigurer {

    @Value("${app.upload-dir}")
    private String uploadDir;

    public static void main(String[] args) {
        SpringApplication.run(PortfolioApplication.class, args);
    }

    /** Serves uploaded project photos from the upload folder at /uploads/** */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path dir = Path.of(uploadDir).toAbsolutePath();
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        registry.addResourceHandler("/uploads/**").addResourceLocations(dir.toUri().toString());
    }

    /** Two starter projects so the page is not empty on first run. Edit or delete them in the dashboard. */
    @Bean
    CommandLineRunner seed(ProjectRepository repo) {
        return args -> {
            if (repo.count() > 0) return;
            repo.save(sample("Doctor Appointment System",
                    "Full-stack booking app where patients find doctors and book slots, with login, online payments and a Docker setup.",
                    "React, Node.js, Express, MongoDB, Docker"));
            repo.save(sample("Instagram Clone",
                    "Social app with sign up, a photo feed and image uploads, built on the MERN stack.",
                    "React, Node.js, Express, MongoDB"));
        };
    }

    private static Project sample(String title, String description, String tech) {
        Project p = new Project();
        p.setTitle(title);
        p.setDescription(description);
        p.setTechStack(tech);
        return p;
    }
}
