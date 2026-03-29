package it.asso.core.common;

import java.awt.image.BufferedImage;
import org.imgscalr.Scalr;


public class ResizeImage {
	
	public static BufferedImage resize(BufferedImage originalImage, int targetWidth) throws Exception {
	    return Scalr.resize(originalImage, targetWidth);
	}

}
