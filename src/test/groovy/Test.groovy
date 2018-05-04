import javax.imageio.ImageIO
import java.awt.image.BufferedImage

class Test {

    private BufferedImage cropImage(File filePath, int x, int y, int w, int h){
        try {
            BufferedImage originalImgage = ImageIO.read(filePath)
            BufferedImage subImgage = originalImgage.getSubimage(x, y, w, h)
            File outputFile = new File("target/reports/captcha.jpg")
            ImageIO.write(subImgage, "jpg", outputFile)
        } catch (IOException e) {
            e.printStackTrace()
            return null
        }
    }

    static void main(String[] agrs) {

        try {
            BufferedImage originalImgage = ImageIO.read(new File("target/reports/HotmailSpec/001-001-Hotmail register_0_-ABC.png"));

            System.out.println("Original Image Dimension: "+originalImgage.getWidth()+"x"+originalImgage.getHeight());

            BufferedImage SubImgage = originalImgage.getSubimage(465, 772, 490, 148);
            System.out.println("Cropped Image Dimension: "+SubImgage.getWidth()+"x"+SubImgage.getHeight());

            File outputfile = new File("target/reports/captcha3.png");
            ImageIO.write(SubImgage, "png", outputfile);

            System.out.println("Image cropped successfully: "+outputfile.getPath());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
