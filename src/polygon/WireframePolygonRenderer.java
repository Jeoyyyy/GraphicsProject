package polygon;

import geometry.Vertex3D;
import lighting.Lighting;
import line.LineRenderer;
import shading.PixelShader;
import windowing.drawable.Drawable;

public class WireframePolygonRenderer implements PolygonRenderer {
	private LineRenderer lineRenderer;
	
	public WireframePolygonRenderer(LineRenderer lineRenderer) {
		this.lineRenderer = lineRenderer;
	}

	@Override
	public void drawPolygon(Polygon polygon, Drawable drawable, 
			PixelShader pixelShader, Lighting lighting) {
		Vertex3D v0;
		Vertex3D v1;
		for (int i = 0; i + 1 < polygon.length(); i++) {
			v0 = polygon.get(i);
			v1 = polygon.get(i + 1);
			lineRenderer.drawLine(v0, v1, drawable);
		}
		v0 = polygon.get(polygon.length() - 1);
		v1 = polygon.get(0);
		lineRenderer.drawLine(v0, v1, drawable);
		
	}
	
}
