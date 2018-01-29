package line;

import geometry.Vertex3D;
import windowing.drawable.Drawable;

public class AntialiasingLineRenderer implements LineRenderer{
	private AntialiasingLineRenderer() {}
	private static final double R = 0.5;
	@Override
	public void drawLine(Vertex3D p1, Vertex3D p2, Drawable drawable) {
		double deltaX = p2.getIntX() - p1.getIntX();
		double deltaY = p2.getIntY() - p1.getIntY();
		
		double m = deltaY / deltaX;
		double b = p2.getIntY() - m * p2.getIntX();
		int argbColor = p1.getColor().asARGB();
		int x = p1.getIntX();
		double y = p1.getIntY();
		for(; x <= p2.getIntX(); x++) {
			int lower_bound = (int) (y - 2 * Math.sqrt(2) * R);
			int upper_bound = (int) (y + 2 * Math.sqrt(2) * R) + 1;
			for(int yi = lower_bound; yi <= upper_bound; yi++) {
				double distance = Math.abs(m*x - yi + b) / Math.sqrt(m*m + 1);
				if(distance < 2*R) {
					double coverage = caculateCoverage(distance);
					drawable.setPixelWithCoverage(x, yi, 0.0, argbColor, coverage);
				}
			}
			y+=m;
		}
	}

/*	private double caculateCoverage(double d) {
		double Theta = Math.acos( d/R );
		double pie_area = Theta * R * R;
		double triangle_area = Math.sqrt(R*R - (d)*(d)) * (d);
		double coverage = (pie_area - triangle_area) / (Math.PI * R * R);
		return coverage;
	}
*/
	private double caculateCoverage(double d) {
		double Theta = Math.acos((d - R)/R);
		double pie_area = Theta * R * R;
		double triangle_area = Math.sqrt(R*R - (d - R)*(d - R)) * (d - R);
		double coverage = (pie_area - triangle_area) / (Math.PI * R * R);
		return coverage;
	}
	
	
	public static LineRenderer make() {
		return new AnyOctantLineRenderer(new AntialiasingLineRenderer());
	}

}
