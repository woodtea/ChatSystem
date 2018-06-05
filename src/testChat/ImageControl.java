package testChat;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.imageio.ImageIO;

import sun.misc.BASE64Decoder;

/*
 * 图片与字符串的转换代码来源于
 * https://blog.csdn.net/hjimce/article/details/78155666
 */
public class ImageControl {
    public static String imgToBase64String(final RenderedImage img) {  
        final ByteArrayOutputStream os = new ByteArrayOutputStream();  
        try {  
            ImageIO.write(img, "jpg", Base64.getEncoder().wrap(os));  
            return os.toString(StandardCharsets.ISO_8859_1.name());  
        } catch (final IOException ioe) {  
            throw new UncheckedIOException(ioe);  
        }  
    }  
    
    public static BufferedImage base64StringToImg(final String base64String) {  
        try {  
            BASE64Decoder decoder = new BASE64Decoder();  
            byte[] bytes = decoder.decodeBuffer(base64String);  
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);  
            return ImageIO.read(bais);  
        } catch (final IOException ioe) {  
            throw new UncheckedIOException(ioe);  
        }  
    } 
}
