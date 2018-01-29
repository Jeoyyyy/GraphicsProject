package windowing.drawable;

public class ZBufferingDrawable extends DrawableDecorator{
	private static double[][] zBuffer;
	
	public ZBufferingDrawable(Drawable delegate) {
		super(delegate);
		zBuffer = new double[delegate.getWidth()][delegate.getHeight()];
		for(int i = 0; i < delegate.getWidth(); i++) {
			for(int j = 0; j < delegate.getHeight(); j++) {
				zBuffer[i][j] = Double.MIN_VALUE;
			}
		}
	}
	@Override
	public void setPixel(int x, int y, double z, int argbColor) {
		if(z > zBuffer[x][y]) {
			delegate.setPixel(x,  y,  z, argbColor);
			zBuffer[x][y] =  z;
		}
	}
	@Override
	public void clear() {
		super.clear();
		for(int i = 0; i < delegate.getWidth(); i++) {
			for(int j = 0; j < delegate.getHeight(); j++) {
				zBuffer[i][j] = Double.MIN_VALUE;
			}
		}
	}	
}
