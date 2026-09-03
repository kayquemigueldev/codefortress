package com.codefortress.identity.authentication.refresh;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.refresh-cookie")
public record RefreshCookieProperties(
        String name,
        boolean secure,
        String sameSite,
        String path
) {

    public RefreshCookieProperties {
        name = requireText(name, "name");
        sameSite = requireText(sameSite, "same-site");
        path = requireText(path, "path");
    }

    private static String requireText(
            String value,
            String propertyName
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "security.refresh-cookie."
                            + propertyName
                            + " must be configured"
            );
        }

        return value;
    }
}