package fr.arby;

import fr.arby.services.GraphService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnNotWebApplication
public class ConsoleApplication implements CommandLineRunner {

    @Autowired
    private GraphService graphService;

    @Override
    public void run(String... args) {
        System.out.println("************************** CONSOLE APP *********************************");
        try {
            String falconPath = args[0];
            String empirePath = args[1];
            graphService.computeSuccessProbability(falconPath, empirePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
