package nl.tudelft.jpacman.level;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import nl.tudelft.jpacman.PacmanConfigurationException;
import nl.tudelft.jpacman.board.Board;
import nl.tudelft.jpacman.board.BoardFactory;
import nl.tudelft.jpacman.board.Square;
import nl.tudelft.jpacman.npc.Ghost;

/**
 * Creates new {@link Level}s from text representations.
 *
 * @author Jeroen Roosen
 */
public class MapParser {

    /**
     * The factory that creates the levels.
     */
    private final LevelFactory levelCreator;

    /**
     * The factory that creates the squares and board.
     */
    private final BoardFactory boardCreator;

    /**
     * Utility dedicated to map text validation and conversion.
     */
    private final MapTextParser textParser;

    /**
     * Creates a new map parser.
     *
     * @param levelFactory
     *            The factory providing the NPC objects and the level.
     * @param boardFactory
     *            The factory providing the Square objects and the board.
     */
    public MapParser(LevelFactory levelFactory, BoardFactory boardFactory) {
        this.levelCreator = levelFactory;
        this.boardCreator = boardFactory;
        this.textParser = new MapTextParser();
    }

    /**
     * Parses the text representation of the board into an actual level.
     *
     * <ul>
     * <li>Supported characters:
     * <li>' ' (space) an empty square.
     * <li>'#' (bracket) a wall.
     * <li>'.' (period) a square with a pellet.
     * <li>'P' (capital P) a starting square for players.
     * <li>'G' (capital G) a square with a ghost.
     * </ul>
     *
     * @param map
     *            The text representation of the board, with map[x][y]
     *            representing the square at position x,y.
     * @return The level as represented by this text.
     */
    public Level parseMap(char[][] map) {
        int width = map.length;
        int height = map[0].length;

        MapBuildState buildState = new MapBuildState(width, height);
        makeGrid(map, buildState);

        Board board = boardCreator.createBoard(buildState.getGrid());
        return levelCreator.createLevel(board, buildState.getGhosts(), buildState.getStartPositions());
    }

    private void makeGrid(char[][] map, MapBuildState buildState) {
        for (int x = 0; x < buildState.getWidth(); x++) {
            for (int y = 0; y < buildState.getHeight(); y++) {
                char c = map[x][y];
                addSquare(buildState.getGrid(), buildState.getGhosts(),
                    buildState.getStartPositions(), x, y, c);
            }
        }
    }

    /**
     * Adds a square to the grid based on a given character. These
     * character come from the map files and describe the type
     * of square.
     *
     * @param grid
     *            The grid of squares with board[x][y] being the
     *            square at column x, row y.
     * @param ghosts
     *            List of all ghosts that were added to the map.
     * @param startPositions
     *            List of all start positions that were added
     *            to the map.
     * @param x
     *            x coordinate of the square.
     * @param y
     *            y coordinate of the square.
     * @param c
     *            Character describing the square type.
     */
    protected void addSquare(Square[][] grid, List<Ghost> ghosts,
                             List<Square> startPositions, int x, int y, char c) {
        switch (c) {
            case ' ':
                grid[x][y] = boardCreator.createGround();
                break;
            case '#':
                grid[x][y] = boardCreator.createWall();
                break;
            case '.':
                Square pelletSquare = boardCreator.createGround();
                grid[x][y] = pelletSquare;
                levelCreator.createPellet().occupy(pelletSquare);
                break;
            case 'G':
                Square ghostSquare = makeGhostSquare(ghosts, levelCreator.createGhost());
                grid[x][y] = ghostSquare;
                break;
            case 'P':
                Square playerSquare = boardCreator.createGround();
                grid[x][y] = playerSquare;
                startPositions.add(playerSquare);
                break;
            default:
                throw new PacmanConfigurationException("Invalid character at "
                    + x + "," + y + ": " + c);
        }
    }

    /**
     * creates a Square with the specified ghost on it
     * and appends the placed ghost into the ghost list.
     *
     * @param ghosts all the ghosts in the level so far, the new ghost will be appended
     * @param ghost the newly created ghost to be placed
     * @return a square with the ghost on it.
     */
    protected Square makeGhostSquare(List<Ghost> ghosts, Ghost ghost) {
        Square ghostSquare = boardCreator.createGround();
        ghosts.add(ghost);
        ghost.occupy(ghostSquare);
        return ghostSquare;
    }

    /**
     * Parses the list of strings into a 2-dimensional character array and
     * passes it on to {@link #parseMap(char[][])}.
     *
     * @param text
     *            The plain text, with every entry in the list being a equally
     *            sized row of squares on the board and the first element being
     *            the top row.
     * @return The level as represented by the text.
     * @throws PacmanConfigurationException If text lines are not properly formatted.
     */
    public Level parseMap(List<String> text) {
        return parseMap(textParser.toMap(text));
    }

    /**
     * Parses the provided input stream as a character stream and passes it
     * result to {@link #parseMap(List)}.
     *
     * @param source
     *            The input stream that will be read.
     * @return The parsed level as represented by the text on the input stream.
     * @throws IOException
     *             when the source could not be read.
     */
    public Level parseMap(InputStream source) throws IOException {
        return parseMap(textParser.readLines(source));
    }

    /**
     * Parses the provided input stream as a character stream and passes it
     * result to {@link #parseMap(List)}.
     *
     * @param mapName
     *            Name of a resource that will be read.
     * @return The parsed level as represented by the text on the input stream.
     * @throws IOException
     *             when the resource could not be read.
     */
    public Level parseMap(String mapName) throws IOException {
        return parseMap(textParser.readResourceLines(MapParser.class, mapName));
    }

    /**
     * @return the BoardCreator
     */
    protected BoardFactory getBoardCreator() {
        return boardCreator;
    }
}
