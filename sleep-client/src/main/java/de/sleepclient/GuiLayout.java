package de.sleepclient;

/** One coordinate system for drawing and mouse input, independent of GUI scale. */
public record GuiLayout(float x, float y, float scale, int panelWidth, int panelHeight) {
    public static GuiLayout fit(int screenWidth, int screenHeight, int panelWidth, int panelHeight, float preferredScale) {
        float fit = Math.min(Math.max(1, screenWidth - 20) / (float) panelWidth,
                Math.max(1, screenHeight - 20) / (float) panelHeight);
        float scale = Math.min(Math.max(.1f, preferredScale), fit);
        return new GuiLayout((screenWidth - panelWidth * scale) / 2f,
                (screenHeight - panelHeight * scale) / 2f, scale, panelWidth, panelHeight);
    }

    public double localX(double screenX) { return (screenX - x) / scale; }
    public double localY(double screenY) { return (screenY - y) / scale; }
    public float screenX(double localX) { return x + (float) localX * scale; }
    public float screenY(double localY) { return y + (float) localY * scale; }

    public static boolean inside(double mx, double my, double x, double y, double width, double height) {
        return mx >= x && mx < x + width && my >= y && my < y + height;
    }

    /** Only the visible, clipped grid is clickable; headers and gaps never toggle modules. */
    public static int cardAt(double mx, double my, int scroll, int count) {
        if (!inside(mx, my, 205, 69, 393, 413)) return -1;
        double gx = mx - 205;
        double gy = my - 69 + scroll;
        int column = (int) (gx / 201);
        int row = (int) (gy / 63);
        if (column > 1 || gx - column * 201 >= 192 || gy - row * 63 >= 54) return -1;
        int index = row * 2 + column;
        return index >= 0 && index < count ? index : -1;
    }

    public static int maxGridScroll(int count) {
        return Math.max(0, ((count + 1) / 2) * 63 - 9 - 413);
    }
}
