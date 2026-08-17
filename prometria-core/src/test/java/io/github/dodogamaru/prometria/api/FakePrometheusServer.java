package io.github.dodogamaru.prometria.api;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Minimal in-process stand-in for the Prometheus HTTP API used by the unit
 * tests. Returns canned JSON per path and records the last request URI.
 */
public class FakePrometheusServer implements AutoCloseable {

    private final HttpServer server;
    private final Map<String, String> responses = new HashMap<>();
    private final Map<String, URI> requests = new ConcurrentHashMap<>();

    public FakePrometheusServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            requests.put(path, exchange.getRequestURI());
            byte[] body = responses.getOrDefault(path, "{\"status\":\"success\",\"data\":{}}")
                    .getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
            exchange.close();
        });
        server.start();
    }

    /**
     * Registers the canned JSON response for an exact request path.
     */
    public void respond(String path, String body) {
        responses.put(path, body);
    }

    /**
     * @return the base URL of the fake server, e.g. {@code http://localhost:4567}
     */
    public String baseUrl() {
        return "http://localhost:" + server.getAddress().getPort();
    }

    /**
     * @param path request path
     * @return the last request URI seen for the path, or {@code null}
     */
    public URI lastRequest(String path) {
        return requests.get(path);
    }

    @Override
    public void close() {
        server.stop(0);
    }
}
