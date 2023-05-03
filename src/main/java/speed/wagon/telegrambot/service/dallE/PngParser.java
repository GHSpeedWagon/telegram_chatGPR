package speed.wagon.telegrambot.service.dallE;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.UUID;

public class PngParser {
    public File parse(String siteWithPng) {
        String path = "C:/Users/User/Desktop/pngs/";
        String fileName = UUID.randomUUID().toString().replaceAll("-", "");
        String fileType = ".png";
        File file = new File(path + fileName + fileType);
        try {
            URL url = new URL(siteWithPng);
            InputStream is = url.openStream();
            OutputStream os = new FileOutputStream(file);
            byte[] b = new byte[2048];
            int length;
            while ((length = is.read(b)) != -1) {
                os.write(b, 0, length);
            }
            is.close();
            os.close();
            return file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
