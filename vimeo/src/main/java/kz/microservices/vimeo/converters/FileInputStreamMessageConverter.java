package kz.microservices.vimeo.converters;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;

import java.io.FileInputStream;
import java.io.IOException;

public class FileInputStreamMessageConverter extends AbstractHttpMessageConverter<FileInputStream> {

    public FileInputStreamMessageConverter() {
        super(MediaType.ALL);
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return FileInputStream.class.isAssignableFrom(clazz);
    }

    @Override
    protected FileInputStream readInternal(Class<? extends FileInputStream> clazz, HttpInputMessage inputMessage) {
        throw new UnsupportedOperationException("Reading FileInputStream is not supported");
    }

    @Override
    protected void writeInternal(FileInputStream fileInputStream, HttpOutputMessage outputMessage) throws IOException {
        fileInputStream.transferTo(outputMessage.getBody());
    }

    @Override
    protected Long getContentLength(FileInputStream fileInputStream, MediaType contentType) {
        try {
            return (long) fileInputStream.available();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    protected void addDefaultHeaders(HttpHeaders headers, FileInputStream fileInputStream, MediaType contentType) throws IOException {
        super.addDefaultHeaders(headers, fileInputStream, contentType);
        headers.setContentType(contentType);
    }
}
