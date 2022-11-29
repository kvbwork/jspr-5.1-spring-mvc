package ru.netology.servlet;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

import static java.util.Optional.ofNullable;

public class HandlerMapping {
    private final Map<String, Map<String, RequestHandler>> staticPathHandlers;
    private final Map<String, List<Map.Entry<Pattern, RequestHandler>>> patternPathHandlers;

    public HandlerMapping() {
        this.staticPathHandlers = new ConcurrentHashMap<>();
        this.patternPathHandlers = new ConcurrentHashMap<>();

    }

    public void addStaticPathHandler(String method, String staticPath, RequestHandler handler) {
        staticPathHandlers.computeIfAbsent(method, k -> new ConcurrentHashMap<>())
                .put(staticPath, handler);
    }

    public void addPatternPathHandler(String method, String regex, RequestHandler handler) {
        patternPathHandlers.computeIfAbsent(method, k -> new CopyOnWriteArrayList<>())
                .add(Map.entry(Pattern.compile(regex), handler));
    }

    public Optional<RequestHandler> getHandler(String method, String path) {
        return ofNullable(staticPathHandlers.get(method))
                .map(pathMap -> pathMap.get(path))
                .or(() -> findHandlerByPatternMatching(method, path));
    }

    private Optional<RequestHandler> findHandlerByPatternMatching(String method, String path) {
        final var pathPatternList = patternPathHandlers.get(method);
        if (pathPatternList != null) {
            for (final var entry : pathPatternList) {
                final var pattern = entry.getKey();
                final var matcher = pattern.matcher(path);
                if (matcher.matches()) return Optional.of(entry.getValue());
            }
        }
        return Optional.empty();
    }
}
