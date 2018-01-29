package line;

import geometry.Vertex3D;
import windowing.drawable.Drawable;
import windowing.graphics.Color;

public class DDALineRenderer implements LineRenderer {
	private DDALineRenderer() {}
	
	@Override
	public void drawLine(Vertex3D p1, Vertex3D p2, Drawable drawable) {
		double deltaX = p2.getIntX() - p1.getIntX();
		double deltaY = p2.getIntY() - p1.getIntY();
		double deltaZ = p2.getZ() - p1.getZ();

		double m = deltaY / deltaX;
		double mz = deltaZ / deltaX;
		
		int x = p1.getIntX();
		double z = p1.getZ();
		int r = p1.getColor().getIntR();
		int g = p1.getColor().getIntG();
		int b = p1.getColor().getIntB();
		
		double y = p1.getIntY();
		
		while(x <= p2.getIntX()) {
			drawable.setPixel(x, (int)Math.round(y), z, Color.makeARGB(r, g, b));
			x++;
			y += m;
			z += mz;
			r = lerp(p1.getColor().getIntR(), p2.getColor().getIntR(), 
					(x - p1.getIntX()) / deltaX );
			g = lerp(p1.getColor().getIntG(), p2.getColor().getIntG(), 
					(x - p1.getIntX()) / deltaX );
			b = lerp(p1.getColor().getIntB(), p2.getColor().getIntB(), 
					(x - p1.getIntX()) / deltaX );
		}
	}
	
	public static LineRenderer make() {
		return new AnyOctantLineRenderer(new DDALineRenderer());
	}
	private int lerp(int a, int b, double alpha) {
		return (int)Math.round( a * (1 - alpha) + b*alpha );
	}

}
