package discord4j.route;

import reactor.core.publisher.Mono;
import reactor.ipc.netty.http.client.HttpClientRequest;
import reactor.ipc.netty.http.client.HttpClientResponse;

import javax.annotation.Nullable;
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


    /**
     * Contains additional information to perform a request
     *
     * @since 3.0
     */
    class Context {

        @Nullable
        private Map<String, Object> queryParams;

        @Nullable
        private Object[] uriVariables;

        @Nullable
        private Consumer<HttpClientRequest> requestFilter;

        @Nullable
        private Consumer<HttpClientResponse> responseFilter;

        private Context() {
        }

        public static Context create() {
            return new Context();
        }

        public static Context ofRequest(Consumer<HttpClientRequest> requestFilter) {
            return new Context().requestFilter(requestFilter);
        }

        public static Context ofResponse(Consumer<HttpClientResponse> responseFilter) {
            return new Context().responseFilter(responseFilter);
        }

        public static Context ofContentType(String value) {
            return new Context().requestFilter(req -> req.header("content-type", value));
        }

        public Context requestFilter(Consumer<HttpClientRequest> requestFilter) {
            this.requestFilter = requestFilter;
            return this;
        }

        public Context responseFilter(Consumer<HttpClientResponse> responseFilter) {
            this.responseFilter = responseFilter;
            return this;
        }

        /**
         * Add the given query parameters.
         *
         * @param queryParams the params
         * @return this context
         */
        public Context queryParams(Map<String, Object> queryParams) {
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

        public Consumer<HttpClientRequest> getRequestFilter() {
            return requestFilter;
        }

        public void setRequestFilter(Consumer<HttpClientRequest> requestFilter) {
            this.requestFilter = requestFilter;
        }

        public Consumer<HttpClientResponse> getResponseFilter() {
            return responseFilter;
        }

        public void setResponseFilter(Consumer<HttpClientResponse> responseFilter) {
            this.responseFilter = responseFilter;
        }

        public Map<String, Object> getQueryParams() {
            return queryParams;
        }

        public void setQueryParams(Map<String, Object> queryParams) {
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
