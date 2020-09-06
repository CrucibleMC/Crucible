package net.cubespace.Yamler.Config;

import java.io.File;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public interface IConfig {
    public void save() throws InvalidConfigurationException;
    public void save(File file) throws InvalidConfigurationException;

    public void init() throws InvalidConfigurationException;
    public void init(File file) throws InvalidConfigurationException;

    public void reload() throws InvalidConfigurationException;

    public void load() throws InvalidConfigurationException;
    public void load(File file) throws InvalidConfigurationException;
}
