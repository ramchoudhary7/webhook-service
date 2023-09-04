package com.heroku.java;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GreetingRequest {
    @JsonProperty("name")
    String name;

    @JsonProperty("languageCode")
    String languageCode;
}
