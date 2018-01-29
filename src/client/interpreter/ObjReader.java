package client.interpreter;

import java.util.ArrayList;
import java.util.List;

import geometry.Point3DH;
import geometry.Vertex3D;
import windowing.graphics.Color;

class ObjReader {
	private static final char COMMENT_CHAR = '#';
	private static final int NOT_SPECIFIED = -1;

	private class ObjVertex {
		// TODO: fill this class in.  Store indices for a vertex, a texture, and a normal.  Have getters for them.
		private	int vertexIndex;
		private int textureIndex;
		private int normalIndex;
		public ObjVertex(int v, int t, int n) {
			vertexIndex = v;
			textureIndex = t;
			normalIndex = n;
		}
		public int getVertexIndex() {
			return vertexIndex;
		}
		public int getTextureIndex() {
			return textureIndex;
		}
		public int getNormalIndex() {
			return normalIndex;
		}
	}
	private class ObjFace extends ArrayList<ObjVertex> {
		private static final long serialVersionUID = -4130668677651098160L;
	}	
	private LineBasedReader reader;
	
	private List<Vertex3D> objVertices;
	private List<Vertex3D> transformedVertices;
	private List<Point3DH> objNormals;
	private List<ObjFace> objFaces;

	private Color defaultColor;
	
	ObjReader(String filename, Color defaultColor) {
		reader = new LineBasedReader(filename);
		this.defaultColor = defaultColor;
		objVertices = new ArrayList<>();
		transformedVertices = new ArrayList<>();
		objNormals = new ArrayList<>();
		objFaces = new ArrayList<>();
		
	}

	public void render(SimpInterpreter si) {
		// TODO: Implement.  All of the vertices, normals, and faces have been defined.
		// First, transform all of the vertices.		
		// Then, go through each face, break into triangles if necessary, and send each triangle to the renderer.
		// You may need to add arguments to this function, and/or change the visibility of functions in SimpInterpreter.
		for(ObjFace face : objFaces) {
			for(int i = 2; i < face.size(); i++) {
				Vertex3D v1 = objVertices.get(face.get(0).getVertexIndex());
				if(face.get(0).getNormalIndex() != NOT_SPECIFIED)
					v1.setNormal(objNormals.get(face.get(0).getNormalIndex()));
				Vertex3D v2 = objVertices.get(face.get(i - 1).getVertexIndex());
				if(face.get(i - 1).getNormalIndex() != NOT_SPECIFIED)
					v2.setNormal(objNormals.get(face.get(i - 1).getNormalIndex()));
				Vertex3D v3 = objVertices.get(face.get(i).getVertexIndex());
				if(face.get(i).getNormalIndex() != NOT_SPECIFIED)
					v3.setNormal(objNormals.get(face.get(i).getNormalIndex()));
				si.polygon(v1, v2, v3);
			}
		}
	}
	
//	private Polygon polygonForFace(ObjFace face) {
//		// TODO: This function might be used in render() above.  Implement it if you find it handy.
//	}

	public void read() {
		while(reader.hasNext() ) {
			String line = reader.next().trim();
			interpretObjLine(line);
			
		}
	}
	private void interpretObjLine(String line) {
		if(!line.isEmpty() && line.charAt(0) != COMMENT_CHAR) {
			String[] tokens = line.split("[ \t,()]+");
			if(tokens.length != 0) {
				interpretObjCommand(tokens);
			}
		}
	}

	private void interpretObjCommand(String[] tokens) {
		switch(tokens[0]) {
		case "v" :
		case "V" :
			interpretObjVertex(tokens);
			break;
		case "vn":
		case "VN":
			interpretObjNormal(tokens);
			break;
		case "f":
		case "F":
			interpretObjFace(tokens);
			break;
		default:	// do nothing
			break;
		}
	}
	private void interpretObjFace(String[] tokens) {
		ObjFace face = new ObjFace();
		
		for(int i = 1; i<tokens.length; i++) {
			String token = tokens[i];
			String[] subtokens = token.split("/");

			int vertexIndex  = objIndex(subtokens, 0, objVertices.size());
			int textureIndex = objIndex(subtokens, 1, 0);
			int normalIndex  = objIndex(subtokens, 2, objNormals.size());

			face.add(new ObjVertex(vertexIndex, textureIndex, normalIndex));
		}
		objFaces.add(face);
	}

	private int objIndex(String[] subtokens, int tokenIndex, int baseForNegativeIndices) {
		// TODO: write this.  subtokens[tokenIndex], if it exists, holds a string for an index.
		// use Integer.parseInt() to get the integer value of the index.
		// Be sure to handle both positive and negative indices.
		if(tokenIndex >= subtokens.length ) {
			return NOT_SPECIFIED;
		}
		if(subtokens[tokenIndex].length() == 0) {
			return NOT_SPECIFIED;
		}
		
		int index = Integer.parseInt(subtokens[tokenIndex]);
		if (index < 0) {
			return baseForNegativeIndices + index - 1;
		}
		else {
			return index - 1;
		}
	}

	private void interpretObjNormal(String[] tokens) {
		int numArgs = tokens.length - 1;
		if(numArgs != 3) {
			throw new BadObjFileException("vertex normal with wrong number of arguments : " + numArgs + ": " + tokens);				
		}
		Point3DH normal = SimpInterpreter.interpretPoint(tokens, 1);
		objNormals.add(normal);
	}
	private void interpretObjVertex(String[] tokens) {
		int numArgs = tokens.length - 1;
		Point3DH point = objVertexPoint(tokens, numArgs);
		Color color = objVertexColor(tokens, numArgs);
		Vertex3D vertex = new Vertex3D(point, color);
		objVertices.add(vertex);
	}

	private Color objVertexColor(String[] tokens, int numArgs) {
		if(numArgs == 6) {
			return SimpInterpreter.interpretColor(tokens, 4);
		}
		if(numArgs == 7) {
			return SimpInterpreter.interpretColor(tokens, 5);
		}
		return defaultColor;
	}

	private Point3DH objVertexPoint(String[] tokens, int numArgs) {
		if(numArgs == 3 || numArgs == 6) {
			return SimpInterpreter.interpretPoint(tokens, 1);
		}
		else if(numArgs == 4 || numArgs == 7) {
			return SimpInterpreter.interpretPointWithW(tokens, 1);
		}
		throw new BadObjFileException("vertex with wrong number of arguments : " + numArgs + ": " + tokens);
	}
}