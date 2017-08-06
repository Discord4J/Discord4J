package discord4j.route;

import io.netty.handler.codec.http.HttpHeaders;
import reactor.core.publisher.Mono;
import reactor.ipc.netty.http.client.HttpClientRequest;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Provides a abstraction between route declaration and its execution with Reactor Netty-based clients.
 *
 * @since 3.0
 */
public interface Router {

    /**
     * Execute a given {@link Route}.
     *
     * @param route the route to use
     * @param <R>   the type of the response entity
     * @return a {@link Mono} that will emit the response entity when the route completes its execution.
     */
    <R> Mono<R> exchange(Route<R> route);

    /**
     * Execute a given {@link Route} with a request entity as body.
     *
     * @param route         the route to use
     * @param requestEntity the request entity for request body
     * @param <T>           the type of the request entity
     * @param <R>           the type of the response entity
     * @return a {@link Mono} that will emit the response entity when the route completes its execution.
     */
    <T, R> Mono<R> exchange(Route<R> route, @Nullable T requestEntity);

    /**
     * Execute a given {@link Route} with a request entity as body and a given execution {@link Context}.
     *
     * @param route         the route to use
     * @param requestEntity the request entity for request body
     * @param context       an execution context containing additional request parameters
     * @param <T>           the type of the request entity
     * @param <R>           the type of the response entity
     * @return a {@link Mono} that will emit the response entity when the route completes its execution.
     */
    <T, R> Mono<R> exchange(Route<R> route, @Nullable T requestEntity, @Nullable Context context);

    <T, R> Mono<R> exchangeForm(Route<R> route, Consumer<HttpClientRequest.Form> formConsumer, @Nullable Context context);


    /**
     * Contains additional information to perform a request
     *
     * @since 3.0
     */
    public class Context {

        private Consumer<HttpHeaders> headersConsumer = headers -> {
        };
        private Map<String, List<String>> queryParams = new LinkedHashMap<>();
        private Object[] uriVariables = {};

        public Context() {
        }

        public Context(Consumer<HttpHeaders> headersConsumer, Map<String, List<String>> queryParams, Object[] uriVariables) {
            this.headersConsumer = headersConsumer;
            this.queryParams = queryParams;
            this.uriVariables = uriVariables;
        }

        /**
         * Manipulate the request's headers with the given consumer. The
         * headers provided to the consumer are "live", so that the consumer can be used to
         * {@linkplain HttpHeaders#set(String, Object) overwrite} existing header values,
         * {@linkplain HttpHeaders#remove(String) remove} values, or use any of the other
         * {@link HttpHeaders} methods.
         *
         * @param headersConsumer a function that consumes the {@code HttpHeaders}
         * @return this context
         */
        public Context headersConsumer(Consumer<HttpHeaders> headersConsumer) {
            this.headersConsumer = headersConsumer;
            return this;
        }

        /**
         * Add the given query parameters.
         *
         * @param queryParams the params
         * @return this context
         */
        public Context queryParams(Map<String, List<String>> queryParams) {
            this.queryParams = queryParams;
            return this;
        }

        /**
         * Add the given URI template variables from an array.
         *
         * @param uriVariables the array of URI variables
         * @return this context
         */
        public Context uriVariables(Object... uriVariables) {
            this.uriVariables = uriVariables;
            return this;
        }

        public Consumer<HttpHeaders> getHeadersConsumer() {
            return headersConsumer;
        }

        public void setHeadersConsumer(Consumer<HttpHeaders> headersConsumer) {
            this.headersConsumer = headersConsumer;
        }

        public Map<String, List<String>> getQueryParams() {
            return queryParams;
        }

        public void setQueryParams(Map<String, List<String>> queryParams) {
            this.queryParams = queryParams;
        }

        public Object[] getUriVariables() {
            return uriVariables;
        }

        public void setUriVariables(Object[] uriVariables) {
            this.uriVariables = uriVariables;
        }
    }
}
