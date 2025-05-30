package io.github.HeroGame.world;

import com.badlogic.gdx.math.Vector2; // Użyjemy Vector2i z LibGDX jeśli chcesz być super dokładny, ale dla equals/hashCode wystarczy int

/**
 * Reprezentuje kombinację 4 sąsiadów World Grid, którzy wpływają na wygląd
 * pojedynczego kafelka Display Grid.
 * Używana jako klucz do mapy grafik kafelków.
 */
public class NeighborCombination {
    // Sąsiedzi są z perspektywy kafelka Display Grid,
    // który 'siedzi' na narożniku czterech kafelków World Grid.
    // Standardowa konwencja: BL (bottom-left), BR (bottom-right), TL (top-left), TR (top-right)
    public TileType bottomLeft;
    public TileType bottomRight;
    public TileType topLeft;
    public TileType topRight;

    // Dodatkowo, jeśli chcemy mapować kombinację dla konkretnego typu terenu rysowanego,
    // możemy dodać ten typ do kombinacji (dla bardziej złożonych systemów warstwowych)
    public TileType displayTileType;

    // Konstruktor dla standardowego Dual-Grid (gdy displayTileType jest niepotrzebny, np. 16 kafelków przejścia)
    public NeighborCombination(TileType bottomLeft, TileType bottomRight, TileType topLeft, TileType topRight) {
        this(bottomLeft, bottomRight, topLeft, topRight, null); // Null dla displayTileType
    }

    // Konstruktor dla warstwowego auto-tilingu (gdy displayTileType jest potrzebny)
    public NeighborCombination(TileType bottomLeft, TileType bottomRight, TileType topLeft, TileType topRight, TileType displayTileType) {
        this.bottomLeft = bottomLeft;
        this.bottomRight = bottomRight;
        this.topLeft = topLeft;
        this.topRight = topRight;
        this.displayTileType = displayTileType; // Jeśli null, to oznacza, że nie jest używany jako klucz
    }

    // Ważne dla użycia w ObjectMap (odpowiednik HashMap w LibGDX)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NeighborCombination that = (NeighborCombination) o;
        return bottomLeft == that.bottomLeft &&
            bottomRight == that.bottomRight &&
            topLeft == that.topLeft &&
            topRight == that.topRight &&
            displayTileType == that.displayTileType; // Uwzględniamy displayTileType w equals
    }

    @Override
    public int hashCode() {
        int result = bottomLeft.hashCode();
        result = 31 * result + bottomRight.hashCode();
        result = 31 * result + topLeft.hashCode();
        result = 31 * result + topRight.hashCode();
        if (displayTileType != null) {
            result = 31 * result + displayTileType.hashCode();
        }
        return result;
    }

    @Override
    public String toString() {
        return "NC{" +
            "bl=" + bottomLeft +
            ", br=" + bottomRight +
            ", tl=" + topLeft +
            ", tr=" + topRight +
            (displayTileType != null ? ", dt=" + displayTileType : "") +
            '}';
    }
}
