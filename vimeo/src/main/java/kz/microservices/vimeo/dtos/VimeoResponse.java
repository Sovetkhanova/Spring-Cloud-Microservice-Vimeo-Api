package kz.microservices.vimeo.dtos;

import com.google.gson.JsonObject;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Setter
@Data
@ToString
public class VimeoResponse {

    private JsonObject json;
    private JsonObject headers;
    private int statusCode;

    public VimeoResponse(JsonObject json, JsonObject headers, int statusCode) {
        this.json = json;
        this.headers = headers;
        this.statusCode = statusCode;
    }

    public VimeoResponse(JsonObject headers, int statusCode) {
        this.headers = headers;
        this.statusCode = statusCode;
    }

    public JsonObject getJson() {
        return json;
    }

    public int getStatusCode() {
        return statusCode;
    }

}
