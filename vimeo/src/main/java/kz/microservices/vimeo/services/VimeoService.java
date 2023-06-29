package kz.microservices.vimeo.services;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface VimeoService {

    Map<String, Object> addVideo(long size, String name, String folderUri);

    Map<String, Object> createFolder(String name, String parentFolderUri);

    int deleteFolder(Long folderId);

    int updateFolder(Long folderId, String name);

    Map<String, Object> getFolder(Long folderId);

    int deleteVideo(Long videoId);

    int putVideoToFolder(Long folderId, Long videoId);

    int uploadThumbnail(Long id, MultipartFile file);

    int createThumbnail(Long id, String time);

    Map<String, Object> getSpecificVideo(Long id);
}
