package consular.weatherwatch.lib;

public class MoonResult {
    private final double decimalPhase;
    private final int minecraftIndex;

    public MoonResult(double decimalPhase, int minecraftIndex) {
        this.decimalPhase = decimalPhase;
        this.minecraftIndex = minecraftIndex;
    }

    public double getDecimalPhase() { return decimalPhase; }
    public int getMinecraftIndex() { return minecraftIndex; }
}

