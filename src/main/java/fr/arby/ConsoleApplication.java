package fr.arby;

import fr.arby.services.GraphService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnNotWebApplication
public class ConsoleApplication implements CommandLineRunner {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private GraphService graphService;

    @Override
    public void run(String... args) {
        try {
            if (args.length != 2) {
                LOGGER.error("Deux arguments sont attendus en paramètre : le chemin vers le fichier millennium-falcon puis le chemin vers le fichier empire");
                return;
            }
            String falconPath = args[0];
            String empirePath = args[1];
            double result = graphService.computeSuccessProbability(falconPath, empirePath);
            LOGGER.info("Probabilité de succès : " + result * 100 + "% !");
        } catch (Exception e) {
            LOGGER.error("Problème lors du calcul de la probabilité de succès :", e);
        }
    }
}
