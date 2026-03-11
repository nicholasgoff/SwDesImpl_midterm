package nl.tudelft.jpacman;

import nl.tudelft.jpacman.board.BoardFactory;
import nl.tudelft.jpacman.game.GameFactory;
import nl.tudelft.jpacman.level.LevelFactory;
import nl.tudelft.jpacman.level.MapParser;
import nl.tudelft.jpacman.level.PlayerFactory;
import nl.tudelft.jpacman.npc.ghost.GhostFactory;
import nl.tudelft.jpacman.points.PointCalculator;
import nl.tudelft.jpacman.points.PointCalculatorLoader;
import nl.tudelft.jpacman.sprite.PacManSprites;

/**
 * Centralized factory wiring for launcher dependencies.
 */
final class LauncherFactoryProvider {

    private final PacManSprites spriteStore;

    LauncherFactoryProvider(PacManSprites spriteStore) {
        this.spriteStore = spriteStore;
    }

    MapParser createMapParser() {
        return new MapParser(createLevelFactory(), createBoardFactory());
    }

    GameFactory createGameFactory() {
        return new GameFactory(createPlayerFactory());
    }

    LevelFactory createLevelFactory() {
        return new LevelFactory(spriteStore, createGhostFactory(), loadPointCalculator());
    }

    private BoardFactory createBoardFactory() {
        return new BoardFactory(spriteStore);
    }

    private GhostFactory createGhostFactory() {
        return new GhostFactory(spriteStore);
    }

    private PlayerFactory createPlayerFactory() {
        return new PlayerFactory(spriteStore);
    }

    PointCalculator loadPointCalculator() {
        return new PointCalculatorLoader().load();
    }
}
