package eu.aston.uptime;

import static org.mockito.ArgumentMatchers.any;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.aston.uptime.model.ResourceFile;
import eu.aston.uptime.utils.ExecProcess;
import eu.aston.uptime.utils.FlowRunner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;


public class FlowRunnerTest {

    @Test
    public void testRunWithoutResourceFiles() throws IOException {

        String script = """
                echo "##start-param var1"
                echo 'Hello, World!'
                echo "##end"
                echo "##set-param var2=test next cmd"
                """;

        String resp = """
                ##start-param var1
                Hello, World!
                ##end
                
                ##set-param var2=test next cmd
                """;

        ExecProcess execProcess = Mockito.mock(ExecProcess.class);
        Mockito.when(execProcess.execBuilder(any(), any())).thenReturn(resp);
        FlowRunner flowRunner = new FlowRunner(execProcess);

        // Pripravíme testovacie dáta
        String resourceName = "testResource";
        Map<String, String> parameters = new HashMap<>();
        File workDir = File.createTempFile("testWorkDir", "");
        List<ResourceFile> files = null;

        // Vykonáme metódu
        flowRunner.run(resourceName, script, parameters, workDir, files);
        System.out.println(parameters);

        Assertions.assertEquals("Hello, World!", parameters.get("var1"));
        Assertions.assertEquals("test next cmd", parameters.get("var2"));
    }
}
