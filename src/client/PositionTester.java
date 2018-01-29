package client;

import geometry.Vertex3D;

@FunctionalInterface
public interface PositionTester {
	public boolean inside(Vertex3D v);
}
