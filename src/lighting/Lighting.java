package lighting;

import java.util.ArrayList;
import java.util.List;

import geometry.Point3DH;
import geometry.Vertex3D;
import windowing.graphics.Color;

public class Lighting {
	private List<Source> sources;
	private Color ambient;
	private Point3DH cameraPosition;
	
	private class Source{
		private Color intensity;
		private double attA;
		private double attB;
		private Point3DH position;

		
		public Source(Color intensity, double attA, double attB, Point3DH position) {
			this.intensity = intensity;
			this.attA = attA;
			this.attB = attB;
			this.position = position;
		}
		public Color getIntensity() {
			return this.intensity;
		}
		public double getAttA() {
			return this.attA;
		}
		public double getAttB() {
			return this.attB;
		}
		public Point3DH getPosition() {
			return this.position;
		}
	}
	public Lighting() {
		sources = new ArrayList<>();
		ambient = Color.BLACK;
	}
	
	public void addSource(Color intensity, double attA, double attB, Point3DH position) {
		sources.add(new Source(intensity, attA, attB, position));
	}
	
	public void setAmbient(Color ambient) {
		this.ambient = ambient;
	}
	public Color illuminate(Vertex3D v, double ks, double spExp) {
		Color I = v.getColor().multiply(ambient);
		for(Source source : sources) {
			Color tempI = source.getIntensity().scale(attenuation(source, v));
			Point3DH L = source.getPosition().subtract(v.getCsPoint()).normalize();
			double NL = L.dotProduct(v.getNormal());

			if(NL <= 0) {				
				continue;
			}
			Color diffuse = v.getColor().scale(NL);
			Point3DH V = cameraPosition.subtract(v.getCsPoint()).normalize();
			Point3DH R = v.getNormal().scale(2 * (v.getNormal().dotProduct(L))).subtract(L).normalize();
			double specular = ks * Math.pow(V.dotProduct(R), spExp);
			if(NL > 0) {
				I = I.add(tempI.multiply(diffuse));
			}
			if(specular > 0) {
				I = I.add(tempI.scale(specular));
			}
		}

		return I;
	}

	private double attenuation(Source source, Vertex3D v) {
		double dis = distance(source.getPosition(), v.getCsPoint());
		return 1 / (source.getAttA() + source.getAttB() * dis);
	}

	private double distance(Point3DH v0, Point3DH v1) {
		return Math.sqrt(Math.pow(v0.getX() - v1.getX(), 2) + 
				Math.pow(v0.getY() - v1.getY(), 2) + 
				Math.pow(v0.getZ() - v1.getZ(), 2));
	}

	public Point3DH getCameraPosition() {
		return cameraPosition;
	}

	public void setCameraPosition(Point3DH cameraPosition) {
		this.cameraPosition = cameraPosition;
	}
	
}
