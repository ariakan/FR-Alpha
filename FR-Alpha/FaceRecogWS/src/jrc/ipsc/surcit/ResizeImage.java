package jrc.ipsc.surcit;


import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/*
 * @author mkyong
 *
 */
public class ResizeImage {

	private static final int IMG_WIDTH = 125;
	private static final int IMG_HEIGHT = 150;

	public static void doResizeImage(String path, String destination)
	{

		try{

			BufferedImage originalImage = ImageIO.read(new File(path));
			int type = originalImage.getType() == 0? BufferedImage.TYPE_INT_ARGB : originalImage.getType();

			BufferedImage resizeImageJpg = resizeImage(originalImage, type);
			resizeImageJpg = ImageUtils.toScaledGray(resizeImageJpg, 1.0);
			ImageIO.write(resizeImageJpg, "jpg", new File(destination)); 

		}catch(IOException e){
			System.out.println(e.getMessage());
		}

	}

	private static BufferedImage resizeImage(BufferedImage originalImage, int type){
		BufferedImage resizedImage = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, type);
		Graphics2D g = resizedImage.createGraphics();
		g.drawImage(originalImage, 0, 0, IMG_WIDTH, IMG_HEIGHT, null);
		g.dispose();

		return resizedImage;
	}


}