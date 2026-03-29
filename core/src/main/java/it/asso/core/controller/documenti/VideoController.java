package it.asso.core.controller.documenti;


import it.asso.core.dao.documenti.VideoDAO;
import it.asso.core.model.documenti.Video;
import it.asso.core.security.UserAuth;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/private/video")
public class VideoController {

    private final VideoDAO videoDao;

    public VideoController(VideoDAO videoDao) {
        this.videoDao = videoDao;
    }

    @GetMapping("/{idAnimale}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Video>> getVideo(@PathVariable String idAnimale) {
        return ResponseEntity.ok(videoDao.getVideoByIdAnimale(idAnimale));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> save(
            @RequestBody Video video,
            @AuthenticationPrincipal UserAuth user) {
        video.setAccount(user.getUsername());
        videoDao.saveOrUpdate(video);
        return ResponseEntity.ok(Map.of("messaggio", "Video salvato"));
    }

    @DeleteMapping("/{idVideo}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable String idVideo) {
        videoDao.delete(idVideo);
        return ResponseEntity.ok(Map.of("messaggio", "Video eliminato"));
    }
}