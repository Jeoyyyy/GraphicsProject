package geometry;

import windowing.graphics.Color;

public class Vertex3D implements Vertex {
	protected Point3DH point;
	protected Point3DH csPoint;
	protected Color color;
	protected double csz;
	protected Point3DH normal;

	public Vertex3D(Point3DH point, Color color) {
		super();
		this.point = point;
		this.color = color;
		this.csPoint = point;
		this.normal = null;
	}
	public Vertex3D(Point3DH point, Color color, Point3DH normal, Point3DH csPoint) {
		super();
		this.point = point;
		this.color = color;
		this.normal = normal;
		this.csPoint = csPoint;
	}
	public Vertex3D(double x, double y, double z, Color color) {
		this(new Point3DH(x, y, z), color);
	}
	public Vertex3D(double x, double y, double z, Color color, Point3DH normal, Point3DH csPoint) {
		this(new Point3DH(x, y, z), color, normal, csPoint);
	}
	public Vertex3D() {
	}
	public Vertex3D(Transformation coor, Color color) {
		this(new Point3DH(coor.transpose().getMatrix()[0]), color);
	}
	public Vertex3D(Transformation coor, Color color, Transformation normalVector, Point3DH csPoint) {
		this(new Point3DH(coor.transpose().getMatrix()[0]), color,
				new Point3DH(normalVector.getMatrix()[0]), csPoint);
	}
	public void setCsPoint(Point3DH csPoint) {
		this.csPoint = csPoint;
	}
	public Point3DH getCsPoint() {
		return this.csPoint;
	}
	public void setNormal(Point3DH normal) {
		this.normal = normal;
	}
	public Point3DH getNormal() {
		return this.normal;
	}
	public double getX() {
		return point.getX();
	}
	public double getY() {
		return point.getY();
	}
	public double getZ() {
		return point.getZ();
	}
	public double getW() {
		return point.getW();
	}
	public void setCameraSpaceZ(double z) {
		csz = z;
	}
	public double getCameraSpaceZ() {
		return csz;
	}
	public Point getPoint() {
		return point;
	}
	public Point3DH getPoint3D() {
		return point;
	}
	public double getCsx() {
		return csPoint.getX();
	}
	public double getCsy() {
		return csPoint.getY();
	}
	public double getCsz() {
		return csPoint.getZ();
	}
	public int getIntX() {
		return (int) Math.round(getX());
	}
	public int getIntY() {
		return (int) Math.round(getY());
	}
	public int getIntZ() {
		return (int) Math.round(getZ());
	}
	
	public Color getColor() {
		return color;
	}
	
	public Vertex3D rounded() {
		return new Vertex3D(point.round(), color);
	}
	public Vertex3D add(Vertex other) {
		Vertex3D other3D = (Vertex3D)other;
		Point3DH addNormal = null;
		if(this.normal != null && other3D.getNormal() != null) {
			addNormal = normal.add(other3D.normal);
		}
		return new Vertex3D(point.add(other3D.getPoint()),
				            color.add(other3D.getColor()),
				            addNormal,
				            csPoint.add(other3D.getCsPoint()));
	}
	public Vertex3D subtract(Vertex other) {
		Vertex3D other3D = (Vertex3D)other;
		Point3DH subNormal = null;
		if(this.normal != null && other3D.getNormal() != null) {
			subNormal = normal.subtract(other3D.normal);
		}
		return new Vertex3D(point.subtract(other3D.getPoint()),
				            color.subtract(other3D.getColor()), 
				            subNormal,
				            csPoint.subtract(other3D.getCsPoint()));
	}
	public Vertex3D scale(double scalar) {
		Point3DH scaleNormal = null;
		if(normal!=null) {
			scaleNormal = normal.scale(scalar);
		}
		return new Vertex3D(point.scale(scalar),
				            color.scale(scalar), 
				            scaleNormal,
				            csPoint.scale(scalar));
	}
	public Vertex3D replacePoint(Point3DH newPoint) {
		return new Vertex3D(newPoint, color, normal, csPoint);
	}
	public Vertex3D replaceColor(Color newColor) {
		return new Vertex3D(point, newColor, normal, csPoint);
	}
	public Vertex3D euclidean() {
		Point3DH euclidean = getPoint3D().euclidean();
		return replacePoint(euclidean);
	}
	
	public String toString() {
		return "(" + getX() + ", " + getY() + ", " + getZ() + ", " + getColor().toIntString() + 
				" csPoint: " + csPoint.toString() + ")";
	}
	public String toIntString() {
		return "(" + getIntX() + ", " + getIntY() + getIntZ() + ", " + ", " + getColor().toIntString() + ")";
	}
	public Transformation getCoordinates() {
		double[][] coor = {{getX()}, {getY()}, {getZ()}, {getW()}};
		return new Transformation(coor);
	}
	public Vertex3D normalize() {
		Point3DH n = null;
		if(normal!=null) {
			n = normal.normalize();
		}
		return new Vertex3D(point, color, n, csPoint);
	}

}
