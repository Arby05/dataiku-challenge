import fr.arby.services.GraphService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        Double result = graphService.computeSuccessProbability(getClass().getResource("example1/millennium-falcon.json").getPath().replaceFirst("/", ""),
                getClass().getResource("example1/empire.json").getPath().replaceFirst("/", ""));
        assertEquals(0d, result);
    }

    @Test
    void testScenario2() throws IOException {
        Double result = graphService.computeSuccessProbability(getClass().getResource("example2/millennium-falcon.json").getPath().replaceFirst("/", ""),
                getClass().getResource("example2/empire.json").getPath().replaceFirst("/", ""));
        assertEquals(0.81d, result);
    }

    @Test
    void testScenario3() throws IOException {
        Double result = graphService.computeSuccessProbability(getClass().getResource("example3/millennium-falcon.json").getPath().replaceFirst("/", ""),
                getClass().getResource("example3/empire.json").getPath().replaceFirst("/", ""));
        assertEquals(0.9d, result);
    }

    @Test
    void testScenario4() throws IOException {
        Double result = graphService.computeSuccessProbability(getClass().getResource("example4/millennium-falcon.json").getPath().replaceFirst("/", ""),
                getClass().getResource("example4/empire.json").getPath().replaceFirst("/", ""));
        assertEquals(1d ,result);
    }

}
