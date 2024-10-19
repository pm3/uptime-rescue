package eu.aston.uptime.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ExecProcess {

    private final static Logger LOGGER = LoggerFactory.getLogger("exec");

    public static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().startsWith("windows");

    public String execBuilder(File workDir, List<String> arguments){
        StringBuilder out = new StringBuilder();
        execStream(workDir, arguments, (c)->out.append(c).append('\n'));
        return out.toString();
    }

    public void execStream(File workDir, List<String> arguments, Consumer<String> consumer){

        Process p = null;
        try {
            ProcessBuilder builder = new ProcessBuilder();
            List<String> osargs = new ArrayList<>();
            if (IS_WINDOWS) {
                osargs.add("cmd.exe");
                osargs.add("/c");
            }
            osargs.addAll(arguments);
            builder.command(osargs);
            builder.directory(workDir);
            builder.redirectOutput(ProcessBuilder.Redirect.PIPE);
            builder.redirectError(ProcessBuilder.Redirect.PIPE);
            p = builder.start();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                while (true) {
                    String l = br.readLine();
                    if (l == null)
                        break;
                    if (consumer != null) {
                        consumer.accept(l);
                    } else {
                        LOGGER.debug(l);
                    }
                }
            }
            try (BufferedReader br2 = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
                while (true) {
                    String l = br2.readLine();
                    if (l == null)
                        break;
                    LOGGER.info("stderr: {}", l);
                }
            }
            int exitCode = p.waitFor();
            if (exitCode != 0) {
                LOGGER.warn("exit code {}", exitCode);
                throw new RuntimeException("exit code "+exitCode);
            }
            p.destroy();
        }catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.warn("call process error {} - {}", String.join(" ", arguments), e.getMessage());
            LOGGER.warn("call process stack", e);
        } finally {
            try {
                if(p!=null) p.destroy();
            } catch (Exception ignore) {
            }
        }
    }

    public static void deepDelete(File dir, boolean includeDir) {
        for (File f : Optional.ofNullable(dir.listFiles()).orElse(new File[0])) {
            if (f.isDirectory()) {
                deepDelete(f, true);
            } else if (f.isFile()) {
                f.delete();
            }
        }
        if (includeDir) dir.delete();
    }

}
