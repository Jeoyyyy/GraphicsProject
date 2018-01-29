package windowing.drawable;

import windowing.graphics.Color;

public class DepthCueingDrawable extends DrawableDecorator {
	private double[][] zBuffer;
	private Color defaultColor;
	private double near;
	private double far;
	
	public DepthCueingDrawable(Drawable delegate, double near, double far, Color defaultColor ) {
		super(delegate);
		this.near = near;
		this.far = far;
		zBuffer = new double[delegate.getWidth()][delegate.getHeight()];
		for(int i = 0; i < delegate.getWidth(); i++) {
			for(int j = 0; j < delegate.getHeight(); j++) {
				zBuffer[i][j] = -Double.MAX_VALUE;
			}
		}
		this.defaultColor = defaultColor;
	}	
	
	@Override
	public void setPixel(int x, int y, double z, int argbColor) {
		double alpha;
		if( z > near) {
			alpha = 1;
		}
		else if( z < far) {
			alpha = 0;
		}
		else {
			alpha = (z - far) / (near - far);
		}
		Color color = Color.fromARGB(argbColor).blendInto(alpha, defaultColor);	
		if(x >= delegate.getWidth() || x < 0)
			return;
		if(y >= delegate.getHeight() || y < 0)
			return;
		if(z > zBuffer[x][y]) {
			delegate.setPixel(x,  y,  z, color.asARGB());
			zBuffer[x][y] =  z;
		}
	}
	@Override
	public void clear() {
		super.clear();
		for(int i = 0; i < delegate.getWidth(); i++) {
			for(int j = 0; j < delegate.getHeight(); j++) {
				zBuffer[i][j] = -Double.MAX_VALUE;
			}
		}
	}
}
