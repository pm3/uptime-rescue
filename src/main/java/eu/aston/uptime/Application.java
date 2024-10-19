package eu.aston.uptime;

import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(
    info = @Info(
            title = "resource-flow",
            version = "1.0"
    )
)
public class Application {

    public static void main(String[] args) {
        Micronaut.build(args)
                 .mainClass(Application.class)
                .eagerInitSingletons(true)
                .start();
    }
}