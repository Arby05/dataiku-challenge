package fr.arby;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        if (args.length == 0) {
            // Aucun argument, on est en mode servlet, on doit lancer la webApp
            SpringApplication.run(Application.class, args);
        } else {
            // Sinon on est en mode CLI, on débranche la servlet et on utilise le ConsoleApplication
            new SpringApplicationBuilder(Application.class)
                    .web(WebApplicationType.NONE)
                    .run(args);
        }
    }

}