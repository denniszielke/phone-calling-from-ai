package com.communication.callautomation;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties(prefix = "acs")
@Getter
public class AppConfig {
    private final String connectionString;
    private final String basecallbackuri;
    private final String callerphonenumber;

    @ConstructorBinding
    AppConfig(final String connectionString,
              final String basecallbackuri,
              final String callerphonenumber) {
        this.connectionString = connectionString;
        this.basecallbackuri = basecallbackuri;
        this.callerphonenumber = callerphonenumber;
    }

    public String getCallerPhoneNumber() {
        return callerphonenumber;
    }

    public String getConnectionString() {
        return connectionString;
    }

    public String getBaseUri() {
        return basecallbackuri;
    }
}
