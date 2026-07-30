package com.isc.console.tambo;

import dev.tamboui.inline.InlineDisplay;
import dev.tamboui.widgets.gauge.Gauge;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

//@Component
public class GaugeDemo implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {

        try (var display = InlineDisplay.create(2)) {

            for (int i = 0; i <= 100; i += 1) {

                int progress = i;

                display.render((area, buffer) -> {

                    var gauge = Gauge.builder()
                            .ratio(progress / 100.0)
                            .label("Progress: " + progress + "%")
                            .build();

                    gauge.render(area, buffer);
                });

                Thread.sleep(200);
            }

            display.println("Done!");
        }
    }
}