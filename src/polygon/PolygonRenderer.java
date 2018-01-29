package polygon;

import lighting.Lighting;
import shading.PixelShader;
import windowing.drawable.Drawable;

public interface PolygonRenderer {
	// assumes polygon is ccw.
	public void drawPolygon(Polygon polygon, Drawable drawable, 
			PixelShader pixelShader, Lighting lighting);
	default public void drawPolygon(Polygon polygon, Drawable panel, Lighting lighting) {
		drawPolygon(polygon, panel, (p, v, l)->v.getColor(), lighting);
	};
}
