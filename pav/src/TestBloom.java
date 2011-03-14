import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import processing.core.PApplet;
import processing.core.PFont;
import codeanticode.glgraphics.GLConstants;
import codeanticode.glgraphics.GLTexture;
import codeanticode.glgraphics.GLTextureFilter;

public class TestBloom extends PApplet
{
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		PApplet.main(new String[] {"TestBloom"});
	}
	

	GLTexture srcTex, bloomMask, destTex;
	GLTexture tex0, tex2, tex4, tex8, tex16;

	GLTextureFilter extractBloom, blur, blend4, toneMap;

	PFont font;

	boolean showAllTextures;
	boolean showSrcTex, showTex16, showBloomMask, showDestTex;

	public void setup()
	{
	    size(640, 480, GLConstants.GLGRAPHICS);
	    noStroke();
	    
	    File s = new File("data/shaders");
	    
	    if(s.canRead()) {
	    	String[] shaders = s.list(new FilenameFilter()
			{
				@Override
				public boolean accept(File dir, String name)
				{
					return new File(dir, name).isFile() && name.toLowerCase().endsWith(".xml");
				}
			});
	    	
	    	System.out.println(Arrays.toString(shaders));
	    }
	    else {
	    	System.out.println("cant read");
	    }
	    
	    // Loading required filters.
	    extractBloom = new GLTextureFilter(this, "ExtractBloom.xml");
	    blur = new GLTextureFilter(this, "Blur.xml");
	    blend4 = new GLTextureFilter(this, "Blend4.xml");  
	    toneMap = new GLTextureFilter(this, "ToneMap.xml");
	       
	    srcTex = new GLTexture(this, "lights.jpg");
	    int w = srcTex.width;
	    int h = srcTex.height;
	    destTex = new GLTexture(this, w, h);

	    // Initializing bloom mask and blur textures.
	    bloomMask = new GLTexture(this, w, h, GLTexture.FLOAT);
	    tex0 = new GLTexture(this, w, h, GLTexture.FLOAT);
	    tex2 = new GLTexture(this, w / 2, h / 2, GLTexture.FLOAT);
	    tex4 = new GLTexture(this, w / 4, h / 4, GLTexture.FLOAT);
	    tex8 = new GLTexture(this, w / 8, h / 8, GLTexture.FLOAT);
	    tex16 = new GLTexture(this, w / 16, h / 16, GLTexture.FLOAT);
	    
	    font = loadFont("EstrangeloEdessa-24.vlw");
	    textFont(font, 24);     
	    
	    showAllTextures = true;
	    showSrcTex = false;
	    showTex16 = false;
	    showBloomMask = false;
	    showDestTex = false;
	}

	public void draw()
	{
	    background(0);
	    
	    float fx = ((float) mouseX) / width;
	    float fy = ((float) mouseY) / height;

	    // Extracting the bright regions from input texture.
	    extractBloom.setParameterValue("bright_threshold", fx);
	    extractBloom.apply(srcTex, tex0);
	  
	    // Downsampling with blur.
	    tex0.filter(blur, tex2);
	    tex2.filter(blur, tex4);    
	    tex4.filter(blur, tex8);    
	    tex8.filter(blur, tex16);     
	    
	    // Blending downsampled textures.
	    blend4.apply(new GLTexture[]{tex2, tex4, tex8, tex16}, new GLTexture[]{bloomMask});
	    
	    // Final tone mapping into destination texture.
	    toneMap.setParameterValue("exposure", fy);
	    toneMap.setParameterValue("bright", fx);
	    toneMap.apply(new GLTexture[]{srcTex, bloomMask}, new GLTexture[]{destTex});

	    if (showAllTextures)
	    {
	        image(srcTex, 0, 0, 320, 240);
	        image(tex16, 320, 0, 320, 240);
	        image(bloomMask, 0, 240, 320, 240);
	        image(destTex, 320, 240, 320, 240);      
	        
	        fill(220, 20, 20);
	        text("source texture", 10, 230);
	        text("downsampled texture", 330, 230);
	        text("bloom mask", 10, 470);        
	        text("final texture", 330, 470);        
	    }
	    else
	    {
	        if (showSrcTex) image(srcTex, 0, 0, width, height);
	        else if (showTex16) image(tex16, 0, 0, width, height);
	        else if (showBloomMask) image(bloomMask, 0, 0, width, height);        
	        else if (showDestTex) image(destTex, 0, 0, width, height);
	    }
	}

	public void mousePressed()
	{
	    if (showAllTextures)
	    {
	        showAllTextures = false;
	        showSrcTex = (0 <= mouseX) && (mouseX < 320) && (0 <= mouseY) && (mouseY < 240);
	        showTex16 = (320 <= mouseX) && (mouseX <= 640) && (0 <= mouseY) && (mouseY < 240);    
	        showBloomMask = (0 <= mouseX) && (mouseX < 320) && (240 <= mouseY) && (mouseY <= 480);
	        showDestTex = (320 <= mouseX) && (mouseX <= 640) && (240 <= mouseY) && (mouseY <= 480);   
	    }
	    else
	    {
	        showAllTextures = true; 
	    }
	}
}
