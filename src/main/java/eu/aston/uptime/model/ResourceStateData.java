package eu.aston.uptime.model;

import io.micronaut.core.annotation.Introspected;

@Introspected
public record ResourceStateData(String name, String type, int running) {
}
