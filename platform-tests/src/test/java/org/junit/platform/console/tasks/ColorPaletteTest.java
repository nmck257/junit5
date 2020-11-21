package org.junit.platform.console.tasks;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

class ColorPaletteTest {
    @Nested
    class LoadFromProperties {
        @Test
        void singleOverride() {
            String properties = """
                SUCCESSFUL = 35;1
                """;
            ColorPalette colorPalette = new ColorPalette(new StringReader(properties));

            String actual = colorPalette.paint(Style.SUCCESSFUL, "text");

            Assertions.assertEquals("\u001B[35;1mtext\u001B[0m", actual);
        }

        @Test
        void keysAreCaseInsensitive() {
            String properties = """
                suCcESSfuL = 35;1
                """;
            ColorPalette colorPalette = new ColorPalette(new StringReader(properties));

            String actual = colorPalette.paint(Style.SUCCESSFUL, "text");

            Assertions.assertEquals("\u001B[35;1mtext\u001B[0m", actual);
        }

        @Test
        void junkKeysAreIgnored() {
            String properties = """
                SUCCESSFUL = 35;1
                JUNK = 1;31;40
                """;
            ColorPalette colorPalette = new ColorPalette(new StringReader(properties));

            String actual = colorPalette.paint(Style.SUCCESSFUL, "text");

            Assertions.assertEquals("\u001B[35;1mtext\u001B[0m", actual);
        }

        @Test
        void multipleOverrides() {
            String properties = """
                SUCCESSFUL = 35;1
                FAILED = 33;4
                """;
            ColorPalette colorPalette = new ColorPalette(new StringReader(properties));

            String successful = colorPalette.paint(Style.SUCCESSFUL, "text");
            String failed = colorPalette.paint(Style.FAILED, "text");

            Assertions.assertEquals("\u001B[35;1mtext\u001B[0m", successful);
            Assertions.assertEquals("\u001B[33;4mtext\u001B[0m", failed);
        }

        @Test
        void unspecifiedStylesAreDefault() {
            String properties = """
                SUCCESSFUL = 35;1
                """;
            ColorPalette colorPalette = new ColorPalette(new StringReader(properties));

            String actual = colorPalette.paint(Style.FAILED, "text");

            Assertions.assertEquals("\u001B[31mtext\u001B[0m", actual);
        }

        @Test
        void cannotOverrideNone() {
            String properties = """
                NONE = 35;1
                """;

            Assertions.assertThrows(IllegalArgumentException.class, () -> new ColorPalette(new StringReader(properties)));
        }
    }
}
