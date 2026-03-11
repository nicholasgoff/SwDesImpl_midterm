package nl.tudelft.jpacman.level;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import nl.tudelft.jpacman.PacmanConfigurationException;

/**
 * Converts and validates map text representations used by {@link MapParser}.
 */
final class MapTextParser {

    char[][] toMap(List<String> text) {
        checkMapFormat(text);

        int height = text.size();
        int width = text.get(0).length();

        char[][] map = new char[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                map[x][y] = text.get(y).charAt(x);
            }
        }
        return map;
    }

    List<String> readLines(InputStream source) throws IOException {
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(source, StandardCharsets.UTF_8))) {
            List<String> lines = new ArrayList<>();
            while (reader.ready()) {
                lines.add(reader.readLine());
            }
            return lines;
        }
    }

    List<String> readResourceLines(Class<?> resourceOwner, String mapName) throws IOException {
        try (InputStream boardStream = resourceOwner.getResourceAsStream(mapName)) {
            if (boardStream == null) {
                throw new PacmanConfigurationException("Could not get resource for: " + mapName);
            }
            return readLines(boardStream);
        }
    }

    private void checkMapFormat(List<String> text) {
        if (text == null) {
            throw new PacmanConfigurationException("Input text cannot be null.");
        }

        if (text.isEmpty()) {
            throw new PacmanConfigurationException("Input text must consist of at least 1 row.");
        }

        int width = text.get(0).length();

        if (width == 0) {
            throw new PacmanConfigurationException("Input text lines cannot be empty.");
        }

        for (String line : text) {
            if (line.length() != width) {
                throw new PacmanConfigurationException("Input text lines are not of equal width.");
            }
        }
    }
}
