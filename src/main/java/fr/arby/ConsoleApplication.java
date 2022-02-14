package fr.arby;

import fr.arby.utils.DataBaseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnNotWebApplication
public class ConsoleApplication implements CommandLineRunner {

    @Override
    public void run(String... args) {
        System.out.println("************************** CONSOLE APP *********************************");
        DataBaseUtils.getAllRoute("toto");
    }
}
