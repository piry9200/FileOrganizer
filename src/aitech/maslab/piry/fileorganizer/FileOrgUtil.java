package aitech.maslab.piry.fileorganizer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileOrgUtil {
    private FileOrgUtil(){}

    public static boolean isExistDir(String dirPath){
        try {
            final Path path = Paths.get(dirPath);
            if(Files.isDirectory(path)) {
                return true;
            }else{
                return false;
            }
        }catch (NullPointerException e) {
            return false;
        }
    }
}
