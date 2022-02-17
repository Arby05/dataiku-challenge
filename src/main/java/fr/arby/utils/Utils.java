package fr.arby.utils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Utils {

    public static String getAbsolutePathFromFalconPath(String falconPath, String routeDbPath) {
        Path dbFilePath = Paths.get(routeDbPath);
        if (dbFilePath.isAbsolute()) {
            // chemin absolu ? Rien à faire
            return routeDbPath;
        } else { // Sinon on reconstruit le chemin à partir du falconPath
            return Paths.get(falconPath).getParent() + File.separator + routeDbPath;
        }
    }

}
