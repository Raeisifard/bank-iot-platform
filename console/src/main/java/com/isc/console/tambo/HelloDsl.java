package com.isc.console.tambo;

import dev.tamboui.toolkit.app.ToolkitApp;
import dev.tamboui.toolkit.element.Element;
import org.springframework.stereotype.Component;

import static dev.tamboui.toolkit.Toolkit.*;

@Component
public class HelloDsl extends ToolkitApp {

    @Override
    protected Element render() {

        return panel(
                "Bank IoT Platform",

                text("Notification Service").green(),
                text("Kafka Session Service").green(),
                text("MQTT Ingress Service").green(),

                spacer(),

                text("Connected Clients: 1,245,678").cyan(),
                text("TPS: 4,950").yellow(),

                spacer(),

                text("Press q to quit").dim()
        ).rounded();
    }

    public void runApp() throws Exception {
        run();
    }
}