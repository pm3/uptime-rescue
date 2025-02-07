package eu.aston.uptime;

import java.io.File;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("app")
public record AppConfig(File configDir,
                        File baseDir,
                        long watchdogInterval,
                        long checkInterval){}
