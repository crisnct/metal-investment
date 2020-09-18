package com.investment.metal.common;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.PropertiesConfigurationLayout;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.*;
import java.util.Set;

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