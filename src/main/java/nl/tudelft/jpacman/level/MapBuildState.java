package nl.tudelft.jpacman.level;

import java.util.ArrayList;
import java.util.List;

import nl.tudelft.jpacman.board.Square;
import nl.tudelft.jpacman.npc.Ghost;

/**
 * Mutable state used while building the level grid from map characters.
 */
final class MapBuildState {

    private final int width;
    private final int height;
    private final Square[][] grid;
    private final List<Ghost> ghosts;
    private final List<Square> startPositions;

    MapBuildState(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new Square[width][height];
        this.ghosts = new ArrayList<>();
        this.startPositions = new ArrayList<>();
    }

    int getWidth() {
        return width;
    }

    int getHeight() {
        return height;
    }

    Square[][] getGrid() {
        return grid;
    }

    List<Ghost> getGhosts() {
        return ghosts;
    }

    List<Square> getStartPositions() {
        return startPositions;
    }
}
