package shading;

import geometry.Vertex3D;
import lighting.Lighting;
import polygon.Polygon;
import windowing.graphics.Color;

public class PhongPixelShader implements PixelShader{

	@Override
	public Color shade(Polygon polygon, Vertex3D vertex, Lighting lighting) {
		return lighting.illuminate(vertex, polygon.getKs(), polygon.getSpExp());
	}
	
}
