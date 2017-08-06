package discord4j.http;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MiniTest {

    private static final Logger log = LoggerFactory.getLogger(MiniTest.class);

    @Test
    public void testX() {
        List<String> list = Stream.of(Optional.of("yes"), Optional.of("no"))
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
