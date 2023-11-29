package com.heroku.java;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class GreetingRequest {
    @JsonProperty("name")
    String name;

    @JsonProperty("languageCode")
    String languageCode;

    public String getLanguageCode() {
        return languageCode;
    }

    public String getName(){
        return name;
    }
}
