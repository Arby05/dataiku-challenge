import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.arby.services.GraphService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

//@SpringBootTest(classes = AbstractMarsBatchTest.ApplicationTest.class)
@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
@SpringBootApplication
@ComponentScan(basePackages = "fr.arby")
public class ApplicationTest {

    @Autowired
    private GraphService graphService;

    private ObjectMapper mapper = new ObjectMapper();

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4})
    void testScenario(int schenario) throws IOException {
        URL falcon = getClass().getResource("example" + schenario + "/millennium-falcon.json");
        URL empire = getClass().getResource("example" + schenario + "/empire.json");
        URL answer = getClass().getResource("example" + schenario + "/answer.json");
        List<URL> nullTestingFiles = Stream.of(falcon, empire, answer).filter(url -> url == null).collect(Collectors.toList());
        if (nullTestingFiles.size() > 0) {
            throw new IOException("Testing file(s) not found : " + nullTestingFiles);
        }
        Double result = graphService.computeSuccessProbability(falcon.getPath().replaceFirst("/", ""),
                empire.getPath().replaceFirst("/", ""));
        // Lecture du JSON de réponse
        Double expected = mapper.readValue(new File(answer.getPath().replaceFirst("/", "")), ObjectNode.class).get("odds").asDouble();
        assertEquals(expected, result);
    }

}
