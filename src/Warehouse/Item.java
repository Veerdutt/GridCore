package warehouse;

public class Item {
    private final String itemId;
    private final String name;
    private final char symbol;
    private final Position position;

    public Item(String itemId, String name, char symbol, Position position) {
        this.itemId = itemId;
        this.name = name;
        this.symbol = symbol;
        this.position = position;
    }

    public String getItemId() {
        return itemId;
    }

    public String getName() {
        return name;
    }

    public char getSymbol() {
        return symbol;
    }

    public Position getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return name + " [" + symbol + "] at " + position;
    }
}
