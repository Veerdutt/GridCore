package warehouse;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Warehouse {
    public static final int ROWS = 10;
    public static final int COLS = 10;

    public static final char EMPTY = '.';
    public static final char RACK = '#';

    private final char[][] grid;
    private final Map<String, Item> itemsByName;
    private final Position pickerInitialPosition;

    public Warehouse() {
        this.grid = new char[ROWS][COLS];
        this.itemsByName = new LinkedHashMap<>();
        this.pickerInitialPosition = new Position(2, 0);
        initializeDefaultLayout();
    }

    private void initializeDefaultLayout() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                grid[r][c] = EMPTY;
            }
        }

        // Predefined rack / obstacle coordinates
        int[][] racks = {
            {1, 2}, {1, 3}, {1, 4}, {1, 7}, {1, 8},
            {2, 2}, {2, 4}, {2, 7},
            {4, 1}, {4, 2}, {4, 5}, {4, 6}, {4, 8},
            {6, 2}, {6, 3}, {6, 6}, {6, 7},
            {7, 4}, {7, 5}, {8, 7}, {8, 8}
        };

        for (int[] rack : racks) {
            int r = rack[0];
            int c = rack[1];
            if (isValidCoordinate(r, c)) {
                grid[r][c] = RACK;
            }
        }

        // Predefined grocery items
        addItem(new Item("ITM-1", "Milk", 'M', new Position(0, 3)));
        addItem(new Item("ITM-2", "Bread", 'B', new Position(2, 6)));
        addItem(new Item("ITM-3", "Apple", 'A', new Position(3, 8)));
        addItem(new Item("ITM-4", "Rice", 'R', new Position(7, 1)));
        addItem(new Item("ITM-5", "Juice", 'J', new Position(9, 6)));
    }

    private void addItem(Item item) {
        Position p = item.getPosition();
        if (!isValidPosition(p) || isObstacle(p)) {
            throw new IllegalArgumentException(
                "Invalid item placement for " + item.getName() + " at " + p
            );
        }
        String key = item.getName().toLowerCase();
        itemsByName.put(key, item);
        grid[p.row][p.col] = item.getSymbol();
    }

    public final boolean isValidCoordinate(int row, int col) {
        return row >= 0 && row < ROWS && col >= 0 && col < COLS;
    }

    public final boolean isValidPosition(Position p) {
        return p != null && isValidCoordinate(p.row, p.col);
    }

    public final boolean isObstacle(Position p) {
        if (!isValidPosition(p)) {
            return true;
        }
        return grid[p.row][p.col] == RACK;
    }

    public char getCell(Position p) {
        if (!isValidPosition(p)) {
            throw new IndexOutOfBoundsException("Position out of bounds: " + p);
        }
        return grid[p.row][p.col];
    }

    public Item getItem(String itemName) {
        if (itemName == null) return null;
        return itemsByName.get(itemName.trim().toLowerCase());
    }

    public Position getItemPosition(String itemName) {
        Item item = getItem(itemName);
        return item != null ? item.getPosition() : null;
    }

    public Map<String, Item> getAllItems() {
        return Collections.unmodifiableMap(itemsByName);
    }

    public Position getPickerInitialPosition() {
        return pickerInitialPosition;
    }

    public void printMap() {
        System.out.println("\n================== WAREHOUSE GRID LAYOUT ==================");
        printMapWithOverlay(null, null, null, null, null);
        System.out.println("Legend: [.] Aisle  [#] Rack  [M] Milk  [B] Bread  [A] Apple  [R] Rice  [J] Juice");
    }

    public void printMapWithPickers(List<Position> pickerPositions) {
        System.out.println("\n================== WAREHOUSE GRID LAYOUT ==================");
        Set<Position> pickerSet = pickerPositions != null ? new HashSet<>(pickerPositions) : null;
        printMapWithOverlay(null, null, pickerSet, null, null);
        System.out.println("Legend: [.] Aisle  [#] Rack  [M/B/A/R/J] Item  [P] Picker");
    }

    public void printMapWithPath(List<Position> path, Position currentPicker) {
        Set<Position> pathSet = new HashSet<>();
        if (path != null) {
            pathSet.addAll(path);
        }
        System.out.println("\n================== PLANNED PATH VISUALIZATION ==================");
        printMapWithOverlay(pathSet, currentPicker, null, null, null);
        System.out.println("Legend: [P] Picker  [*] Path Step  [M/B/A/R/J] Item  [#] Rack  [.] Aisle");
    }

    public void printMapWithRoute(List<Position> path, Position startPos, Position endPos) {
        Set<Position> pathSet = new HashSet<>();
        if (path != null) {
            pathSet.addAll(path);
        }
        System.out.println("\n================== PLANNED ROUTE VISUALIZATION ==================");
        printMapWithOverlay(pathSet, null, null, startPos, endPos);
        System.out.println("Route Legend: [S] Start  [E] End Destination  [*] Traversal Step  [M/B/A/R/J] Item  [#] Rack");
    }

    private void printMapWithOverlay(
        Set<Position> pathSet,
        Position currentPicker,
        Set<Position> pickerPositions,
        Position startPos,
        Position endPos
    ) {
        System.out.print("     ");
        for (int c = 0; c < COLS; c++) {
            System.out.print(" C" + c);
        }
        System.out.println();

        String border = "    +" + "-".repeat(COLS * 3 + 1) + "+";
        System.out.println(border);

        for (int r = 0; r < ROWS; r++) {
            System.out.printf("R%-2d |", r);
            for (int c = 0; c < COLS; c++) {
                Position pos = new Position(r, c);
                char cell = grid[r][c];
                boolean isItem = (cell != EMPTY && cell != RACK);

                char display;
                if (startPos != null && startPos.equals(pos)) {
                    display = 'S';
                } else if (endPos != null && endPos.equals(pos)) {
                    display = 'E';
                } else if ((currentPicker != null && currentPicker.equals(pos) && !isItem)
                    || (pickerPositions != null && pickerPositions.contains(pos) && !isItem)) {
                    display = 'P';
                } else if (!isItem && cell == EMPTY && pathSet != null && pathSet.contains(pos)) {
                    display = '*';
                } else {
                    display = cell;
                }
                System.out.print("  " + display);
            }
            System.out.println(" |");
        }
        System.out.println(border);
    }
}

