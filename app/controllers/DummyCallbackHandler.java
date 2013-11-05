package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DummyCallbackHandler extends Controller {

    @BodyParser.Of(value = BodyParser.TolerantText.class)
    public static Result index() {
        System.out.println("Method: " + request().method());
        System.out.println("URL: " + request().uri());
        System.out.println("Headers: " + flattenMap(request().headers()));
        String rawBody = request().body().asText();
        System.out.println("Body: " + rawBody);
        System.out.println("");

        JsonNode json = (JsonNode) Json.parse(rawBody);
        if (json != null && json.findPath("SourceId") != null) {
            String sourceId = json.findPath("SourceId").asText();
            try {
                File tempFile = new File(".", sourceId);
                tempFile.createNewFile();
                System.out.println("File created: " + tempFile.getAbsolutePath());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return ok();
    }

    private static Map<String, String> flattenMap(Map<String, String[]> headers) {
        Map<String, String> temp = new HashMap<String, String>();

        for (String header : headers.keySet()) {
            temp.put(header, request().getHeader(header));
        }

        return temp;
    }
}