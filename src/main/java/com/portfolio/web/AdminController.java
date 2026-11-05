package com.portfolio.web;

import com.portfolio.model.Project;
import com.portfolio.repo.ProjectRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** Everything under /admin needs the owner login (see SecurityConfig). */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Set<String> IMAGE_TYPES = Set.of("png", "jpg", "jpeg", "webp", "gif");

    private final ProjectRepository repo;

    @Value("${app.upload-dir}")
    private String uploadDir;

    public AdminController(ProjectRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public String dashboard(@RequestParam(required = false) Long edit, Model model) {
        model.addAttribute("projects", repo.findAllByOrderByCreatedAtDesc());
        model.addAttribute("form", edit == null ? new Project() : repo.findById(edit).orElseGet(Project::new));
        return "admin";
    }

    @PostMapping("/projects")
    public String save(Project form,
                       @RequestParam(required = false) MultipartFile image,
                       RedirectAttributes flash) throws IOException {
        if (form.getTitle() == null || form.getTitle().isBlank()) {
            flash.addFlashAttribute("error", "Add a title for the project.");
            return "redirect:/admin";
        }
        Project p = form.getId() == null ? new Project() : repo.findById(form.getId()).orElseGet(Project::new);
        p.setTitle(form.getTitle().trim());
        p.setDescription(clean(form.getDescription()));
        p.setTechStack(clean(form.getTechStack()));
        p.setLiveUrl(clean(form.getLiveUrl()));
        p.setGithubUrl(clean(form.getGithubUrl()));

        if (image != null && !image.isEmpty()) {
            String ext = Optional.ofNullable(StringUtils.getFilenameExtension(image.getOriginalFilename()))
                    .orElse("").toLowerCase();
            if (!IMAGE_TYPES.contains(ext)) {
                flash.addFlashAttribute("error", "Upload a PNG, JPG, WEBP or GIF image.");
                return "redirect:/admin";
            }
            Path dir = Path.of(uploadDir).toAbsolutePath();
            Files.createDirectories(dir);
            String file = UUID.randomUUID() + "." + ext;
            image.transferTo(dir.resolve(file));
            p.setImageUrl("/uploads/" + file);
        } else if (clean(form.getImageUrl()) != null) {
            p.setImageUrl(clean(form.getImageUrl()));
        }

        repo.save(p);
        flash.addFlashAttribute("msg", "Project saved.");
        return "redirect:/admin";
    }

    @PostMapping("/projects/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes flash) {
        repo.deleteById(id);
        flash.addFlashAttribute("msg", "Project deleted.");
        return "redirect:/admin";
    }

    private static String clean(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
