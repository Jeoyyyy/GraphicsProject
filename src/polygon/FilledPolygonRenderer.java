package polygon;
import geometry.Point3DH;
import geometry.Vertex3D;
import lighting.Lighting;
import polygon.PolygonRenderer;
import shading.PixelShader;
import windowing.drawable.Drawable;
import windowing.graphics.Color;

public class FilledPolygonRenderer implements PolygonRenderer {
	private FilledPolygonRenderer() {}

	public static PolygonRenderer make() {
		return new FilledPolygonRenderer( );
	}
	@Override
	public void drawPolygon(Polygon polygon, Drawable drawable,
			PixelShader pixelShader, Lighting lighting) {
		
		Chain left_chain = polygon.leftChain();
		Chain right_chain = polygon.rightChain();
		int l = 0;
		int r = 0;
		if ( l+1 == left_chain.length() && r+1 == right_chain.length()) {
			return;
		}
		Vertex3D vLNext = left_chain.get(l+1);
		Vertex3D vRNext = right_chain.get(r+1);
		Vertex3D vLCur = left_chain.get(l);
		Vertex3D vRCur = right_chain.get(r);
		double slopeL;
		double slopeR;

		double xl = vLCur.getX();
		double xr = vRCur.getX();
		
		Point3DH normalL = vLCur.getNormal();
		Point3DH normalR = vRCur.getNormal();
		Point3DH csPointL = vLCur.getCsPoint();
		Point3DH csPointR = vRCur.getCsPoint();
		
		Point3DH slopeNormalL = null;
		Point3DH slopeNormalR = null;
		Point3DH slopeCsPointL;
		Point3DH slopeCsPointR;
		
		Color colorL = vLCur.getColor();
		Color colorR = vRCur.getColor();
		Color slopeColorL;
		Color slopeColorR;
		
		int y = vLCur.getIntY();
		
		while (l+1 < left_chain.length() && r+1 < right_chain.length()) {
			double deltaYL = vLNext.getY() - vLCur.getY();
			double deltaYR = vRNext.getY() - vRCur.getY();
			if(deltaYL == 0) {
				l++;
				if(l+1 >= left_chain.length())
					break;
				vLCur = left_chain.get(l);
				vLNext = left_chain.get(l+1);
				xl = vLCur.getX();
				
				normalL = vLCur.getNormal();
				csPointL = vLCur.getCsPoint();
				colorL = vLCur.getColor();
			}
			if(deltaYR == 0) {
				r++;
				if(r+1 >= right_chain.length())
					break;
				vRCur = right_chain.get(r);
				vRNext = right_chain.get(r+1);
				xr = vRCur.getX();
				
				normalR = vRCur.getNormal();
				csPointR = vRCur.getCsPoint();
				colorR = vRCur.getColor();
			}
			deltaYL = vLNext.getY() - vLCur.getY();
			deltaYR = vRNext.getY() - vRCur.getY();
			slopeL =  (vLNext.getX() - vLCur.getX()) / deltaYL ;
			slopeR =  (vRNext.getX() - vRCur.getX()) / deltaYR ;
			
			if(vLNext.getNormal() != null && vLCur.getNormal() != null) {
				slopeNormalL = vLNext.getNormal().subtract(vLCur.getNormal()).scale(1/deltaYL);
			}
			if(vRNext.getNormal() != null && vRCur.getNormal() != null) {
				slopeNormalR = vRNext.getNormal().subtract(vRCur.getNormal()).scale(1/deltaYR);
			}
			
			slopeCsPointL = vLNext.getCsPoint().subtract(vLCur.getCsPoint()).scale(1/deltaYL);
			slopeCsPointR = vRNext.getCsPoint().subtract(vRCur.getCsPoint()).scale(1/deltaYR);
			
			slopeColorL = vLNext.getColor().subtract(vLCur.getColor()).scale(1/deltaYL);
			slopeColorR = vRNext.getColor().subtract(vRCur.getColor()).scale(1/deltaYR);
			
			while(y != vLNext.getIntY() && y != vRNext.getIntY()) {
				double deltaX = xr - xl;			
				
				Point3DH slopeNormal = null;
				if(normalR != null)
					slopeNormal = normalR.subtract(normalL).scale(1/deltaX);
				Point3DH slopeCsPoint = csPointR.subtract(csPointL).scale(1/deltaX);
				Color slopeColor = colorR.subtract(colorL).scale(1/deltaX);
				
				Point3DH normal = normalL;
				Point3DH csPoint = csPointL;
				Color color = colorL;
				
				for(int x = (int) Math.round(xl); x < Math.round(xr); x++) {
					if(normal != null) {
						normal = normal.normalize();
					}
					Vertex3D vertex = new Vertex3D(new Point3DH(x, y, csPoint.getZ()), 
							color, normal, csPoint);
					drawable.setPixel(x, y, csPoint.getZ(), 
							pixelShader.shade(polygon, vertex, lighting).asARGB());

					if(normal != null)
						normal = normal.add(slopeNormal);
					csPoint = csPoint.add(slopeCsPoint);
					color = color.add(slopeColor);
				}
				y--;
				xl -= slopeL;
				xr -= slopeR;
				
				if(normalL != null)
					normalL = normalL.subtract(slopeNormalL);
				if(normalR != null)
					normalR = normalR.subtract(slopeNormalR);
				csPointL = csPointL.subtract(slopeCsPointL);
				csPointR = csPointR.subtract(slopeCsPointR);
				colorL = colorL.subtract(slopeColorL);
				colorR = colorR.subtract(slopeColorR);
			}
			
			if(y == vLNext.getIntY() ) {
				l++;
				if(l+1 >= left_chain.length())
					break;
				vLCur = left_chain.get(l);
				vLNext = left_chain.get(l+1);
				xl = vLCur.getX();
				
				normalL = vLCur.getNormal();
				csPointL = vLCur.getCsPoint();
				colorL = vLCur.getColor();
			}
			if(y == vRNext.getIntY() ) {
				r++;
				if(r+1 >= right_chain.length())
					break;
				vRCur = right_chain.get(r);
				vRNext = right_chain.get(r+1);
				xr = vRCur.getX();

				normalR = vRCur.getNormal();
				csPointR = vRCur.getCsPoint();
				colorR = vRCur.getColor();
			}
		}
	}
	private int lerp(int a, int b, double alpha) {
		return (int)Math.round( a * (1 - alpha) + b*alpha );
	}
}
