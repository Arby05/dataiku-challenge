import fr.arby.services.GraphService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;

//@SpringBootTest(classes = AbstractMarsBatchTest.ApplicationTest.class)
@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
@SpringBootApplication
@ComponentScan(basePackages = "fr.mmb.mars")
public class ApplicationTest {

    @Autowired
    private GraphService graphService;

    @Test
    void testScenario1() throws IOException {
        graphService.launchPathComputing(getClass().getResource("millenium-falcon.json").toString(),
                getClass().getResource("empire.json").toString());
    }

}
