package discord4j.rest.service;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.common.jackson.PossibleModule;
import discord4j.rest.http.EmptyReaderStrategy;
import discord4j.rest.http.EmptyWriterStrategy;
import discord4j.rest.http.JacksonReaderStrategy;
import discord4j.rest.http.JacksonWriterStrategy;
import discord4j.rest.http.client.SimpleHttpClient;
import discord4j.rest.request.Router;
import discord4j.rest.route.Routes;
import org.junit.Test;

public class ApplicationServiceTest {

    private ApplicationService applicationService = null;

    private ApplicationService getApplicationService() {

        if (applicationService != null) {
            return applicationService;
        }

        String token = System.getenv("token");
        ObjectMapper mapper = getMapper();

        SimpleHttpClient httpClient = SimpleHttpClient.builder()
                .baseUrl(Routes.BASE_URL)
                .defaultHeader("Authorization", "Bot " + token)
                .defaultHeader("Content-Type", "application/json")
                .readerStrategy(new JacksonReaderStrategy<>(mapper))
                .readerStrategy(new EmptyReaderStrategy())
                .writerStrategy(new JacksonWriterStrategy(mapper))
                .writerStrategy(new EmptyWriterStrategy())
                .build();

        Router router = new Router(httpClient);

        return applicationService = new ApplicationService(router);
    }

    private ObjectMapper getMapper() {
        return new ObjectMapper()
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
                .registerModule(new PossibleModule());
    }

    @Test
    public void testGetCurrentApplicationInfo() {
        getApplicationService().getCurrentApplicationInfo().block();
    }

}
