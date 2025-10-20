package com.investment.metal.infrastructure.service;

import com.google.common.base.Charsets;
import io.micrometer.core.instrument.util.IOUtils;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * Infrastructure service for building mail parameters.
 * Follows Clean Architecture principles by keeping mail infrastructure concerns separate.
 */
@Slf4j
public class MailParameterBuilder {

    private static final Map<MailTemplates, String> mailTemplates = new HashMap<>();

    static {
        ClassLoader classLoader = MailParameterBuilder.class.getClassLoader();
        for (MailTemplates templateType : MailTemplates.values()) {
            final String template;
            final String path = "mail-templates/" + templateType.getFilename();
            try (InputStream is = classLoader.getResourceAsStream(path)) {
                if (is == null) {
                    log.error("Mail template not found on classpath: {}", path);
                    mailTemplates.put(templateType, "");
                    continue;
                }
                template = IOUtils.toString(is, Charsets.UTF_8);
            } catch (Exception ex) {
                log.error("Failed to load mail template {}", path, ex);
                mailTemplates.put(templateType, "");
                continue;
            }
            MailParameterBuilder.mailTemplates.put(templateType, template);
        }
    }

    private final String text;

    private final Map<String, Object> parameters = new HashMap<>();

    private MailParameterBuilder(String text) {
        this.text = text;
    }

    public static MailParameterBuilder newInstance(MailTemplates template) {
        String templateText = MailParameterBuilder.mailTemplates.get(template);
        if (templateText == null || templateText.isEmpty()) {
            throw new IllegalStateException("Mail template not loaded: " + template);
        }
        return new MailParameterBuilder(templateText);
    }

    public MailParameterBuilder replace(String name, Object value) {
        this.parameters.put(name, value);
        return this;
    }

    public MailParameterBuilder replaceDouble(String argumentName, Double value, int decimals) {
        String paramValue = String.format("%." + decimals + "f", value);
        return this.replace(argumentName, paramValue);
    }

    public String build() {
        String result = this.text;
        for (Map.Entry<String, Object> entry : this.parameters.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue().toString());
        }
        return result;
    }
}
