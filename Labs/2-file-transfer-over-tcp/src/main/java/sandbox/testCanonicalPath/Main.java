package sandbox.testCanonicalPath;

import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        File uploadsDir = new File("uploads");
        File file = new File(uploadsDir, "../../def/core.txt");

        System.out.println(uploadsDir.getCanonicalPath());
        System.out.println(file.getParentFile());
        System.out.println(file.getParentFile().getCanonicalPath());
        System.out.println(file.getCanonicalPath());
    }
}
