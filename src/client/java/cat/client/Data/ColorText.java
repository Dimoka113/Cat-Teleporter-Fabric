package cat.client.Data;

import net.minecraft.text.Text;
import net.minecraft.text.Style;
import net.minecraft.text.TextColor;

public class ColorText {
    public static Text create(String text, int rgb) {
        return Text.literal(text).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(rgb)));
    }

}
