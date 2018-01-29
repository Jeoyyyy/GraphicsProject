package client;


import java.util.ArrayList;
import java.util.List;

import client.interpreter.SimpInterpreter.Intersector;
import geometry.Vertex3D;

public class Clipper {
	public Clipper() {
		
	}
	
	public List<Vertex3D> clipPolygon(List<Vertex3D> pVertices, 
			double z, Intersector intersector, PositionTester tester) {
		if(pVertices == null) {
			return null;
		}
		int k = -1;
		int num_vert = pVertices.size();
		for(int i = 0; i < num_vert; i++) {
			if( !(tester.inside(pVertices.get(i))) &&
					tester.inside(pVertices.get((i + 1) % num_vert)) ) {
				k = i;
			}
		}
		if (k == -1) {
			if(!tester.inside(pVertices.get(0))) {
				return null;
			}
			else {
				return pVertices;
			}
		}
		List<Vertex3D> vertices = new ArrayList<>();
		vertices.add(intersector.intersect(pVertices.get(k), 
				pVertices.get((k + 1) % num_vert), z));
		for(int i = k + 1; i <= k + num_vert; i++) {
			if( tester.inside( pVertices.get(i % num_vert) ) ) {
				vertices.add( pVertices.get(i % num_vert) );
			}
			else {
				vertices.add( intersector.intersect(pVertices.get((i - 1) % num_vert), 
						pVertices.get(i % num_vert), z) );
				break;
			}
		}
		return vertices;
	}
	
}
