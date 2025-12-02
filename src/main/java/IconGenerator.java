import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Génère un fichier PNG d'icône pour l'application.
 * Usage: java -cp target/xslt-transformer-app-1.0.0-shaded.jar IconGenerator target/app-icon.png
 */
public class IconGenerator {
    public static void main(String[] args) throws Exception {
        String out = "target/app-icon.png";
        if (args != null && args.length > 0) out = args[0];

        int size = 1024;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // background gradient
        GradientPaint gp = new GradientPaint(0, 0, new Color(0x1E88E5), 0, size, new Color(0x1976D2));
        g.setPaint(gp);
        g.fill(new RoundRectangle2D.Double(0, 0, size, size, size*0.15, size*0.15));

        // draw rounded inset
        int margin = size/8;
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.12f));
        g.setColor(Color.white);
        g.fill(new RoundRectangle2D.Double(margin, margin, size-2*margin, size-2*margin, size*0.12, size*0.12));
        g.setComposite(AlphaComposite.SrcOver);

        // draw text <X>
        g.setColor(Color.white);
        int fontSize = size/3;
        Font font = new Font("SansSerif", Font.BOLD, fontSize);
        g.setFont(font);
        String text = "<X>";
        FontMetrics fm = g.getFontMetrics();
        int tw = fm.stringWidth(text);
        int th = fm.getAscent();
        g.drawString(text, (size-tw)/2, (size+th)/2 - fm.getDescent());

        g.dispose();

        File outFile = new File(out);
        outFile.getParentFile().mkdirs();
        ImageIO.write(img, "png", outFile);
        System.out.println("Wrote icon: " + outFile.getAbsolutePath());
    }
}
