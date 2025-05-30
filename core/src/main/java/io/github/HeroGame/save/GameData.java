package io.github.HeroGame.save;

import io.github.HeroGame.world.TileType;
// Import Array jest niepotrzebny, ponieważ TileType[][] to tablica wbudowana w Javie, nie libGDX Array.
// Ale jeśli w przyszłości będziesz używać Array<TileType> dla wierszy, to będzie przydatny.

public class GameData {
    public TileType[][] worldGridData;
    public int mapWidthTiles;
    public int mapHeightTiles;
    // TODO: Tutaj w przyszłości można dodać więcej danych stanu gry, np.
    // public PlayerState playerState;
    // public Array<Quest> activeQuests;
    // public long gameTime;

    // Pusty konstruktor wymagany przez Json dla deserializacji
    public GameData() {}

    public GameData(TileType[][] worldGridData, int mapWidthTiles, int mapHeightTiles) {
        this.worldGridData = worldGridData;
        this.mapWidthTiles = mapWidthTiles;
        this.mapHeightTiles = mapHeightTiles;
    }

    // Gettery do bezpiecznego dostępu do danych
    public TileType[][] getWorldGridData() {
        return worldGridData;
    }

    public int getMapWidthTiles() {
        return mapWidthTiles;
    }

    public int getMapHeightTiles() {
        return mapHeightTiles;
    }
}
