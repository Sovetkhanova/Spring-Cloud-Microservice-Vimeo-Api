package kz.microservices.vimeo.controllers;

import kz.microservices.vimeo.services.VimeoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
public class VimeoController {
    private final VimeoService vimeoService;

    @PostMapping("/videos")
    public Map<String, Object> getUploader(@RequestParam("size") long size,
                                           @RequestParam("name") String name,
                                           @RequestParam("folderUri") String folderUri) {
        return vimeoService.addVideo(size, name, folderUri);
    }

    @DeleteMapping("/videos/{id}")
    public int deleteVideo(@PathVariable("id") Long id) {
        return vimeoService.deleteVideo(id);
    }

    @PostMapping("/folders")
    public Map<String, Object> createFolder(@RequestParam("name") String name,
                                            @RequestParam(value = "parent_folder_uri", required = false) String parentFolderUri) {
        return vimeoService.createFolder(name, parentFolderUri);
    }

    @DeleteMapping("/folders/{id}")
    public int deleteFolder(@PathVariable("id") Long folderId) {
        return vimeoService.deleteFolder(folderId);
    }

    @PutMapping("/folders/{id}")
    public int updateFolder(@PathVariable("id") Long folderId,
                            @RequestParam(value = "name") String name) {
        return vimeoService.updateFolder(folderId, name);

    }

    @GetMapping("/folders/{id}")
    public Map<String, Object> getFolder(@PathVariable("id") Long folderId) {
        return vimeoService.getFolder(folderId);
    }

    @PutMapping("/folders/{folderId}/videos/{videoId}")
    public int putVideoToFolder(@PathVariable("folderId") Long folderId,
                                @PathVariable("videoId") Long videoId) {
        return vimeoService.putVideoToFolder(folderId, videoId);
    }

    @PostMapping("/videos/{id}/thumbnails")
    public int uploadThumbnail(@PathVariable Long id,
                               @RequestPart(value = "file") MultipartFile file) {
        return vimeoService.uploadThumbnail(id, file);
    }

    @PutMapping("/videos/{id}/thumbnails")
    public int createThumbnail(@PathVariable Long id,
                               @RequestParam String time) {
        return vimeoService.createThumbnail(id, time);
    }

    @GetMapping("/videos/{id}")
    public Map<String, Object> getSpecificVideo(@PathVariable Long id){
        return vimeoService.getSpecificVideo(id);
    }
}
