package canban.component;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;

public class ColorFactory {
    public static Collection<Color> getColors() {
        Collection<Color> colorCollection = new ArrayList<>();
        colorCollection.add(Color.BLUE);
        colorCollection.add(Color.ORANGE);
        colorCollection.add(Color.WHITE);
        colorCollection.add(Color.BLACK);
        colorCollection.add(Color.CYAN);
        colorCollection.add(Color.DARK_GRAY);
        colorCollection.add(Color.GRAY);
        colorCollection.add(Color.LIGHT_GRAY);
        colorCollection.add(Color.PINK);
        colorCollection.add(Color.YELLOW);
        colorCollection.add(Color.GREEN);
        colorCollection.add(Color.RED);
        colorCollection.add(Color.MAGENTA);
        return colorCollection;
    }
}
