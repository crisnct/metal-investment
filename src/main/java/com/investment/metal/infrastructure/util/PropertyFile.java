package com.investment.metal.infrastructure.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.Set;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.PropertiesConfigurationLayout;
import org.apache.commons.configuration2.ex.ConfigurationException;

/**
 * Infrastructure utility for property file operations.
 * Follows Clean Architecture principles by keeping infrastructure utilities separate.
 */
public class PropertyFile {

    private PropertiesConfiguration config;

    private PropertiesConfigurationLayout layout;

    public void load(InputStream is) throws ConfigurationException {
        config = new PropertiesConfiguration();
        layout = new PropertiesConfigurationLayout();
        config.setLayout(layout);
        layout.load(config, new InputStreamReader(is));
    }

    public void save(Writer out) throws IOException, ConfigurationException {
        try (BufferedWriter bufferedWriter = new BufferedWriter(out)) {
            layout.save(config, bufferedWriter);
        }
    }

    public Set<String> getKeys() {
        return layout.getKeys();
    }

    public void setProperty(String propName, String propValue) {
        config.setProperty(propName, propValue);
    }

    public String getProperty(String propName) {
        return config.getString(propName);
    }

    public void addProperty(String propName, String propValue) {
        this.config.addProperty(propName, propValue);
    }
}
