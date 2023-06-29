package kz.microservices.vimeo.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import kz.microservices.vimeo.converters.FileInputStreamMessageConverter;
import kz.microservices.vimeo.exceptions.UnsupportedMediaTypeException;
import kz.microservices.vimeo.exceptions.VimeoException;
import kz.microservices.vimeo.dtos.VimeoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class VimeoServiceImpl implements VimeoService {

    @Value("${vimeo.service:}")
    private String service;
    @Value("${vimeo.token:}")
    private String token;
    private final RestTemplate restTemplate = new RestTemplateBuilder().build();
    private final ObjectMapper objectMapper;

    @Override
    public Map<String, Object> addVideo(long size, String name, String folderUri) {
        VimeoResponse response = beginUploadVideo(size, name, folderUri);
        if (response.getStatusCode() == 200) {
            try {
                return objectMapper.readValue(response.getJson().toString(), new TypeReference<>() {
            });
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        else throw new VimeoException("Video was not uploaded");
    }

    @Override
    public int uploadThumbnail(Long id, MultipartFile file) {
        HttpHeaders headers = createHeaders();
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        VimeoResponse getUriResponse = executeRequest(service + "/videos/" + id + "?fields=metadata.connections.pictures.uri", HttpMethod.GET, requestEntity);
        String uri = getUriResponse.getJson().getAsJsonObject("metadata").getAsJsonObject("connections").getAsJsonObject("pictures").get("uri").getAsString();
        JsonObject body = executeRequest(service + uri, HttpMethod.POST, requestEntity).getJson().getAsJsonObject();
        putThumbnail(body.get("link").getAsString(), file);
        return changeThumbnailStatus(body.get("uri").getAsString(), true);
    }

    @Override
    public int createThumbnail(Long id, String time) {
        HttpHeaders headers = createHeaders();
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("active", "true");
        body.add("time", time);
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);
        VimeoResponse response = executeRequest(service + "/videos/" + id + "/pictures", HttpMethod.POST, requestEntity);
        return response.getStatusCode();
    }

    @Override
    public Map<String, Object> getSpecificVideo(Long id) {
        HttpHeaders headers = createHeaders();
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        VimeoResponse response = executeRequest(service + "/videos/" + id, HttpMethod.GET, requestEntity);
        try {
            return objectMapper.readValue(response.getJson().toString(), new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private int changeThumbnailStatus(String uri, Boolean isActive) {
        HttpHeaders headers = createHeaders();
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("active", String.valueOf(isActive));
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);
        VimeoResponse vimeoResponse = executeRequest(service + uri, HttpMethod.PATCH, requestEntity);
        return vimeoResponse.getStatusCode();
    }

    private void putThumbnail(String link, MultipartFile file) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        String contentType = file.getContentType();
        if ((!Objects.equals(contentType, MediaType.IMAGE_JPEG_VALUE)) && (!Objects.equals(contentType, MediaType.IMAGE_GIF_VALUE)) && (!Objects.equals(contentType, MediaType.IMAGE_PNG_VALUE))) {
            throw new UnsupportedMediaTypeException("Try to choose another media type");
        }
        headers.add("Content-Type", contentType);
        try {
            HttpEntity<InputStream> requestEntity = new HttpEntity<>(file.getInputStream(), headers);
            executeRequest(link, HttpMethod.PUT, requestEntity);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public VimeoResponse beginUploadVideo(long fileSize, String name, String folderUri) {
        new VimeoResponse();
        VimeoResponse vimeoResponse;

        HttpHeaders headers = createHeaders();
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("upload.approach", "post");
        body.add("upload.size", String.valueOf(fileSize));
        if (name != null) {
            body.add("name", name);
        }
        body.add("folder_uri", folderUri);
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);
        vimeoResponse = executeRequest(service + "/me/videos", HttpMethod.POST, requestEntity);
        return vimeoResponse;
    }

    @Override
    public Map<String, Object> createFolder(String name, String parentFolderUri) {
        HttpHeaders headers = createHeaders();
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        if (name != null) {
            body.add("name", name);
        }
        if (parentFolderUri != null) {
            body.add("parent_folder_uri", parentFolderUri);
        }
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);
        VimeoResponse vimeoResponse = executeRequest(service + "/me/projects", HttpMethod.POST, requestEntity);
        try {
            return objectMapper.readValue(vimeoResponse.getJson().toString(), new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int deleteFolder(Long folderId) {
        HttpHeaders headers = createHeaders();
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        VimeoResponse response = executeRequest(service + "/me/projects/" + folderId + "?should_delete_clips=true", HttpMethod.DELETE, requestEntity);
        return response.getStatusCode();
    }

    @Override
    public int updateFolder(Long folderId, String name) {
        HttpHeaders headers = createHeaders();
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("name", name);
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);
        VimeoResponse vimeoResponse = executeRequest(service + "/me/projects/" + folderId, HttpMethod.PATCH, requestEntity);
        return vimeoResponse.getStatusCode();
    }

    @Override
    public Map<String, Object> getFolder(Long folderId) {
        HttpHeaders headers = createHeaders();
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        VimeoResponse vimeoResponse = executeRequest(service + "/me/projects/" + folderId, HttpMethod.GET, requestEntity);
        try {
            return objectMapper.readValue(vimeoResponse.getJson().toString(), new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int deleteVideo(Long videoId) {
        HttpHeaders headers = createHeaders();
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        VimeoResponse response = executeRequest(service + "/videos/" + videoId, HttpMethod.DELETE, requestEntity);
        return response.getStatusCode();
    }

    @Override
    public int putVideoToFolder(Long folderId, Long videoId) {
        HttpHeaders headers = createHeaders();
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        VimeoResponse response = executeRequest(service + "/me/projects/" + folderId + "/videos/" + videoId, HttpMethod.PUT, requestEntity);
        return response.getStatusCode();
    }

    public VimeoResponse executeRequest(String url, HttpMethod method, HttpEntity<?> requestEntity) {
        restTemplate.getMessageConverters().add(new FormHttpMessageConverter());
        restTemplate.getMessageConverters().add(new FileInputStreamMessageConverter());
        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, method, requestEntity, String.class);
            String responseBody = responseEntity.getBody();
            int statusCode = responseEntity.getStatusCodeValue();

            JsonObject responseJson;
            JsonObject responseHeaders = new JsonObject();
            if (responseBody != null) {
                responseJson = JsonParser.parseString(responseBody).getAsJsonObject();
            } else {
                return new VimeoResponse(responseHeaders, statusCode);
            }
            HttpHeaders headers = responseEntity.getHeaders();
            headers.forEach((key, value) -> responseHeaders.addProperty(key, value.get(0)));
            return new VimeoResponse(responseJson, responseHeaders, statusCode);
        } catch (RestClientException e) {
            throw new VimeoException(e.getMessage());
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return headers;
    }

}
