package com.edduarte.vokter.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public class ServerConfiguration extends Configuration {

    @NotEmpty
    private String template;


    @JsonProperty
    public String getTemplate() {
        return template;
    }


    @JsonProperty
    public void setTemplate(String template) {
        this.template = template;
    }
}
