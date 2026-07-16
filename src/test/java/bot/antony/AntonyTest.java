package bot.antony;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class AntonyTest {

    @Test
    void parseConfigArgumentShouldRecognizeValidConfigFile() throws Exception {
        Method method = Antony.class.getDeclaredMethod("parseConfigArgument", String.class);
        method.setAccessible(true);

        Optional<String> configPath = (Optional<String>) method.invoke(null, "-config=src/test/resources/antony.properties");

        assertTrue(configPath.isPresent());
        assertEquals("src/test/resources/antony.properties", configPath.get());
    }

    @Test
    void parseConfigArgumentShouldIgnoreInvalidArguments() throws Exception {
        Method method = Antony.class.getDeclaredMethod("parseConfigArgument", String.class);
        method.setAccessible(true);

        Optional<String> configPath = (Optional<String>) method.invoke(null, "--help");

        assertNotNull(configPath);
        assertTrue(configPath.isEmpty());
    }
}
