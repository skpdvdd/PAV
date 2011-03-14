import codeanticode.glgraphics.GLConstants;
import codeanticode.glgraphics.GLGraphicsOffScreen;
import codeanticode.glgraphics.GLTexture;
import codeanticode.glgraphics.GLTextureFilter;
import pav.lib.PAVException;
import pav.lib.ShaderManager;
import processing.core.PApplet;

public class BubbleTest extends PApplet
{
	private static final long serialVersionUID = 344216160960127538L;

	GLGraphicsOffScreen _buffer;
	GLTextureFilter _blur, _blend2;
	GLTexture _history, _out;
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		PApplet.main(new String[] { "BubbleTest" });
	}
	
	@Override
	public void setup()
	{
		size(800, 600, GLConstants.GLGRAPHICS);
		frameRate(45);
		
		_buffer = new GLGraphicsOffScreen(this, width, height);
		_buffer.beginDraw();
		_buffer.colorMode(HSB);
		_buffer.endDraw();
		
		_history = new GLTexture(this, width, height);
		_out = new GLTexture(this, width, height);
				
		try {
			ShaderManager.initialize(this);
			
			_blur = ShaderManager.getTextureFilter("Blur");
			_blend2 = ShaderManager.getTextureFilter("Blend2");
		}
		catch(PAVException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void draw()
	{
		GLTexture buffer = _buffer.getTexture();
		
		buffer.filter(_blur, buffer);
		
		drawBubbles();
		
//		_history.filter(_blur, _history);
//		_blend2.apply(new GLTexture[] { _history, _buffer.getTexture() }, _out);
//		
//		_history.copy(_out);
//		
		image(buffer, 0, 0);
		
//		_out.clear(0, 0);
	}
	
	void drawBubbles()
	{
		float hue = random(0, 255);
		float r = random(50, 100);
				
		_buffer.beginDraw();
		_buffer.fill(hue, 255, 255, 100);
		_buffer.stroke(hue, 255, 255, 200);
		_buffer.ellipse(random(0, width), random(0, height), r, r);
		_buffer.endDraw();
	}
}
