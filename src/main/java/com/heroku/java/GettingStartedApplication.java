package com.heroku.java;

import com.google.cloud.dialogflow.cx.v3.WebhookRequest;
import com.google.protobuf.util.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
@Controller
public class GettingStartedApplication {
    private final DataSource dataSource;

    @Autowired
    public GettingStartedApplication(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/database")
    String database(Map<String, Object> model) {
        try (Connection connection = dataSource.getConnection()) {
            final var statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS ticks (tick timestamp)");
            statement.executeUpdate("INSERT INTO ticks VALUES (now())");

            final var resultSet = statement.executeQuery("SELECT tick FROM ticks");
            final var output = new ArrayList<>();
            while (resultSet.next()) {
                output.add("Read from DB: " + resultSet.getTimestamp("tick"));
            }

            model.put("records", output);
            return "database";

        } catch (Throwable t) {
            model.put("message", t.getMessage());
            return "error";
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(GettingStartedApplication.class, args);
    }
}

@RestController
class HelloController {

    @GetMapping("/health-check")
    public String sayHello() {
        return "Hello, World! Bring it on!!";
    }

    @PostMapping(value = "/webhook")
    public ResponseEntity<Map<String, Object>> webhookResponse(@RequestBody String request) throws Exception {
        try {
            WebhookRequest webhookRequest = stringToWebhookRequest(request);

            List<Map<String, String>> resList;
            List<Map<String, String>> staticResList = new ArrayList<>();

            // Based on language code
            if (false) {
                resList = List.of(
                        Map.of("type", "list", "title", "स्वास्थ्य एवं चिकित्सा संबंधी जानकारी"),
                        Map.of("type", "list", "title", "प्रेसक्रिप्शन से संबंधित जानकारी")
                );
            } else {
                resList = List.of(
                        Map.of("type", "list", "title", "I have health queries", "subtitle", "", "innerText", "I have health queries"),
                        Map.of("type", "list", "title", "I have prescription related queries", "subtitle", "", "innerText", "I have prescription related queries")
                );
            }

            // Static list
            staticResList = List.of(
                    Map.of("type", "suggestion", "title", "I am having a headache", "subtitle", "", "innerText", "I am having a headache"),
                    Map.of("type", "suggestion", "title", "Side effects of crocin", "subtitle", "", "innerText", "Side effects of crocin"),
                    Map.of("type", "suggestion", "title", "End Conversation", "subtitle", "", "innerText", "End Conversation")
            );

            // Building the final JSON response
            Map<String, Object> jsonResponse = new HashMap<>();

            jsonResponse.put("fulfillmentResponse", Map.of(
                    "messages", List.of(
                            Map.of("payload", Map.of("richContent", List.of(resList))),
                            Map.of("payload", Map.of("richContent", List.of(staticResList)))
                    )
            ));

            jsonResponse.put("payload", Map.of(
                    "textFooterNote", "",
                    "fulfillFooterNote", "Consultation will provide a broader diagnosis which may not be accurate all the time"
            ));

            return ResponseEntity.ok(jsonResponse);
        }
        catch (Exception e){
            String x = "";
            throw new Exception("");
        }
    }

    @PostMapping(value = "/mock-webhook")
    public ResponseEntity<Map<String, Object>> webhookResponseMock(@RequestBody GreetingRequest request) {
        List<Map<String, String>> resList;
        List<Map<String, String>> staticResList = new ArrayList<>();

        // Based on language code
        if (request.getLanguageCode().equals("hi")) {
            resList = List.of(
                    Map.of("type", "list", "title", "स्वास्थ्य एवं चिकित्सा संबंधी जानकारी"),
                    Map.of("type", "list", "title", "प्रेसक्रिप्शन से संबंधित जानकारी")
            );
        } else {
            resList = List.of(
                    Map.of("type", "list", "title", "I have health queries", "subtitle", "", "innerText", "I have health queries"),
                    Map.of("type", "list", "title", "I have prescription related queries", "subtitle", "", "innerText", "I have prescription related queries")
            );
        }

        // Static list
        staticResList = List.of(
                Map.of("type", "suggestion", "title", "I am having a headache", "subtitle", "", "innerText", "I am having a headache"),
                Map.of("type", "suggestion", "title", "Side effects of crocin", "subtitle", "", "innerText", "Side effects of crocin"),
                Map.of("type", "suggestion", "title", "End Conversation", "subtitle", "", "innerText", "End Conversation")
        );

        // Building the final JSON response
        Map<String, Object> jsonResponse = new HashMap<>();

        jsonResponse.put("fulfillmentResponse", Map.of(
                "messages", List.of(
                        Map.of("payload", Map.of("richContent", List.of(resList))),
                        Map.of("payload", Map.of("richContent", List.of(staticResList)))
                )
        ));

        jsonResponse.put("payload", Map.of(
                "textFooterNote", "",
                "fulfillFooterNote", "Consultation will provide a broader diagnosis which may not be accurate all the time"
        ));

        return ResponseEntity.ok(jsonResponse);
    }

    @PostMapping("/greet")
    public String greet(@RequestBody GreetingRequest request) {
        return "Hello, " + request.getName() + "!";
    }

    public static WebhookRequest stringToWebhookRequest(String response) {

        WebhookRequest.Builder webhookRequestBuilder = WebhookRequest.newBuilder();

        try {
            JsonFormat.parser().ignoringUnknownFields().merge(response, webhookRequestBuilder);
        } catch (Exception e) {
            System.out.println("Failed to parse JSON to Protobuf: " + e.getMessage());
        }

        return webhookRequestBuilder.build();
    }
}
