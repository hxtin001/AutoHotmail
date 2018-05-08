import javax.imageio.ImageIO
import java.awt.image.BufferedImage

class Test {

    private void cropImage(String filePath, int x, int y, int w, int h){
        try {
            BufferedImage originalImgage = ImageIO.read(new File(filePath));

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

    static void main(String[] args) {
        String path = ""
        int index = -1
        String newIndex
        new File("target/reports/HotmailSpec/").eachFileMatch(~/.*.png/) { file ->
            def m = file.getAbsolutePath() =~ /(_{1}\d{1}_{1})/
            newIndex = m.find()?m.group():"0"
            newIndex = newIndex.replaceAll("_", "")
            if (newIndex.toInteger() > index) {
                path = file.getAbsolutePath()
            }
        }
        print path
    }

}
