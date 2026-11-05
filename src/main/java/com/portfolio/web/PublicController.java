package com.portfolio.web;

import com.portfolio.repo.ProjectRepository;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class PublicController {

    private final ProjectRepository repo;
    private final Environment env;

    public PublicController(ProjectRepository repo, Environment env) {
        this.repo = repo;
        this.env = env;
    }

    @GetMapping("/")
    public String home(Model model) {
        Map<String, Object> site = new HashMap<>();
        for (String key : List.of("name", "roles", "tagline", "email", "phone", "github", "linkedin")) {
            site.put(key, env.getProperty("portfolio." + key, ""));
        }
        site.put("skills", Arrays.stream(env.getProperty("portfolio.skills", "").split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).toList());
        model.addAttribute("site", site);
        model.addAttribute("projects", repo.findAllByOrderByCreatedAtDesc());
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
