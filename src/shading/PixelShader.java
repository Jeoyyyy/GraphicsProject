package shading;

import geometry.Vertex3D;
import lighting.Lighting;
import polygon.Polygon;
import windowing.graphics.Color;

public interface PixelShader {

	Color shade(Polygon polygon, Vertex3D vertex, Lighting lighting);

}
