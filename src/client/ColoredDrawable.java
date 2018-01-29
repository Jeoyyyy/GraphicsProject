package client;

import windowing.drawable.*;

public class ColoredDrawable extends DrawableDecorator{
	private int color;
	public ColoredDrawable (Drawable delegate, int color) {
		super(delegate);
		this.color = color;
	}
	
	@Override
	public void clear() {
		fill(color, Double.MAX_VALUE);
	}
}
