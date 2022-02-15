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
@ComponentScan(basePackages = "fr.arby")
public class ApplicationTest {

    @Autowired
    private GraphService graphService;

    @Test
    void testScenario1() throws IOException {
        graphService.launchPathComputing(getClass().getResource("example1/millennium-falcon.json").getFile(),
                getClass().getResource("example1/empire.json").getFile());
    }

}
