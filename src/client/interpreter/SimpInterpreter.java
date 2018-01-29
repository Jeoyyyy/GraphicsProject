package client.interpreter;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import client.RendererTrio;
import geometry.Point3DH;
import geometry.Transformation;
import geometry.Vertex3D;
import lighting.Lighting;
import line.LineRenderer;
import client.Clipper;
import polygon.Polygon;
import polygon.PolygonRenderer;
import shading.FlatPixelShader;
import shading.GouraudPixelShader;
import shading.PhongPixelShader;
import shading.PixelShader;
import windowing.drawable.Drawable;
import windowing.graphics.Color;
import windowing.graphics.Dimensions;
import windowing.drawable.DepthCueingDrawable;

public class SimpInterpreter {
	private static final int NUM_TOKENS_FOR_POINT = 3;
	private static final int NUM_TOKENS_FOR_COMMAND = 1;
	private static final int NUM_TOKENS_FOR_COLORED_VERTEX = 6;
	private static final int NUM_TOKENS_FOR_UNCOLORED_VERTEX = 3;
	private static final char COMMENT_CHAR = '#';
	private RenderStyle renderStyle;
	private ShadingStyle shadingStyle;
	
	private Transformation CTM;
	private Transformation worldToScreen;
	private Transformation worldToCamera;
	private Transformation perspective;
	
	private static int WORLD_LOW_X = -100;
	private static int WORLD_HIGH_X = 100;
	private static int WORLD_LOW_Y = -100;
	private static int WORLD_HIGH_Y = 100;
	
	private LineBasedReader reader;
	private Stack<LineBasedReader> readerStack;
	private Stack<Transformation> worldStack;
	
	private Color defaultColor = Color.WHITE;
	private Color ambientLight = Color.BLACK;
	private Color depthColor = Color.BLACK;
	private double near = -Double.MAX_VALUE;
	private double far = -Double.MAX_VALUE;
	private double hither = 0;
	private double yon = -200;
	private double xlow;
	private double ylow;
	private double xhigh;
	private double yhigh;
	
	private double ks = 0.3;
	private double spExp = 8;
	
	private Lighting lighting;
	private Drawable drawable;
	private Drawable depthCueingDrawable;
	
	private LineRenderer lineRenderer;
	private PolygonRenderer filledRenderer;
	private PolygonRenderer wireframeRenderer;
	private Transformation cameraToScreen;
	private Clipper clipper;

	private PixelShader flatPixelShader;
	private PixelShader gouraudPixelShader;
	private PixelShader phongPixelShader;
	
	public enum RenderStyle {
		FILLED,
		WIREFRAME;
	}
	public enum ShadingStyle{
		FLAT,
		GOURAUD,
		PHONG;
	}
	public SimpInterpreter(String filename, 
			Drawable drawable,
			RendererTrio renderers) {
		this.drawable = drawable;
		this.depthCueingDrawable = new DepthCueingDrawable(drawable, near, far, depthColor);
		this.lineRenderer = renderers.getLineRenderer();
		this.filledRenderer = renderers.getFilledRenderer();
		this.wireframeRenderer = renderers.getWireframeRenderer();
		this.defaultColor = Color.WHITE;
		
		makeWorldToScreenTransform(drawable.getDimensions());
		makePerspectiveTransform();
		
		lighting = new Lighting();
		reader = new LineBasedReader(filename);
		readerStack = new Stack<>();
		worldStack = new Stack<>();
		renderStyle = RenderStyle.FILLED;
		shadingStyle = ShadingStyle.PHONG;
		CTM = Transformation.identity();
		clipper = new Clipper();
		
		flatPixelShader = new FlatPixelShader();
		gouraudPixelShader = new GouraudPixelShader();
		phongPixelShader = new PhongPixelShader();
	}

	private void makePerspectiveTransform() {
		double array[][] = { {1, 0, 0, 0}, {0, 1, 0, 0}, {0, 0, 1, 0}, {0, 0, -1, 0 } };
		perspective = new Transformation(array);
	}

	private void makeWorldToScreenTransform(Dimensions dimensions) {
		worldToScreen = Transformation.identity();
		 
		worldToScreen.set(0, 0, (double)dimensions.getWidth() / (WORLD_HIGH_X - WORLD_LOW_X) );
		worldToScreen.set(0, 3, (0 - WORLD_LOW_X) * dimensions.getWidth() / (WORLD_HIGH_X - WORLD_LOW_X));
		worldToScreen.set(1, 1, (double)dimensions.getHeight() / (WORLD_HIGH_Y - WORLD_LOW_Y));
		worldToScreen.set(1, 3, (0 - WORLD_LOW_Y) * dimensions.getHeight() / (WORLD_HIGH_Y - WORLD_LOW_Y));
	}
	
	private void makeCameraToScreenTransform(Dimensions dimensions) {
		cameraToScreen = Transformation.identity();
		double ratioX = xhigh - xlow;
		double ratioY = yhigh - ylow;
		double ratio;
		if(xhigh - xlow > yhigh - ylow) {
			ratio = xhigh - xlow;
		}
		else {
			ratio = yhigh - ylow;
		}
		cameraToScreen.set(0, 0, (double)dimensions.getWidth() / ratio );
		cameraToScreen.set(0, 3, (dimensions.getWidth() / 2) -
				(xhigh + xlow) * dimensions.getWidth() / 2 / ratio);
		cameraToScreen.set(1, 1, (double)dimensions.getHeight() / ratio);
		cameraToScreen.set(1, 3, (dimensions.getHeight() / 2) - 
				(yhigh + ylow) * dimensions.getHeight() / 2 /ratio);
	}
	
	public void interpret() {
		while(reader.hasNext() ) {
			String line = reader.next().trim();
			interpretLine(line);
			while(!reader.hasNext()) {
				if(readerStack.isEmpty()) {
					return;
				}
				else {
					reader = readerStack.pop();
				}
			}
		}
	}
	public void interpretLine(String line) {
		if(!line.isEmpty() && line.charAt(0) != COMMENT_CHAR) {
			String[] tokens = line.split("[ \t,()]+");
			if(tokens.length != 0) {
				interpretCommand(tokens);
			}
		}
	}
	private void interpretCommand(String[] tokens) {
		switch(tokens[0]) {
		case "{" :      push();   break;
		case "}" :      pop();    break;
		case "wire" :   wire();   break;
		case "filled" : filled(); break;
		
		case "file" :		interpretFile(tokens);		break;
		case "scale" :		interpretScale(tokens);		break;
		case "translate" :	interpretTranslate(tokens);	break;
		case "rotate" :		interpretRotate(tokens);	break;
		case "line" :		interpretLine(tokens);		break;
		case "polygon" :	interpretPolygon(tokens);	break;
		case "camera" :		interpretCamera(tokens);	break;
		case "surface" :	interpretSurface(tokens);	break;
		case "ambient" :	interpretAmbient(tokens);	break;
		case "depth" :		interpretDepth(tokens);		break;
		case "obj" :		interpretObj(tokens);		break;
		case "light":		interpretLight(tokens);		break;
		case "flat":		shadingStyle = ShadingStyle.FLAT;		break;
		case "gouraud":		shadingStyle = ShadingStyle.GOURAUD;	break;
		case "phong":		shadingStyle = ShadingStyle.PHONG;		break;
		
		default :
			System.err.println("bad input line: " + tokens);
			break;
		}
	}

	private void interpretLight(String[] tokens) {
		double r = cleanNumber(tokens[1]);
		double g = cleanNumber(tokens[2]);
		double b = cleanNumber(tokens[3]);
		double attA = cleanNumber(tokens[4]);
		double attB = cleanNumber(tokens[5]);
		Color color;
		color = new Color(r, g, b);
		Point3DH origin = new Point3DH(0, 0, 0);
		Point3DH sourcePosition = new Point3DH(worldToCamera.multiply(
				CTM.multiply(origin.getVector().transpose())).transpose().getMatrix()[0]);
		Point3DH cameraPosition = new Point3DH(0, 0, 0);
		lighting.addSource(color, attA, attB, sourcePosition);
		lighting.setCameraPosition(cameraPosition);
	}

	private void interpretDepth(String[] tokens) {
		near = cleanNumber(tokens[1]);
		far = cleanNumber(tokens[2]);
		depthColor = interpretColor(tokens, 3);
		depthCueingDrawable = new DepthCueingDrawable(drawable, near, far, depthColor);
	}

	private void interpretAmbient(String[] tokens) {
		ambientLight = interpretColor(tokens, 1);
		lighting.setAmbient(ambientLight);
	}

	private void interpretSurface(String[] tokens) {
		defaultColor = interpretColor(tokens, 1);
		ks = cleanNumber(tokens[4]);
		spExp = cleanNumber(tokens[5]);
	}

	private void interpretCamera(String[] tokens) {
		xlow = cleanNumber(tokens[1]);
		ylow = cleanNumber(tokens[2]);
		xhigh = cleanNumber(tokens[3]);
		yhigh = cleanNumber(tokens[4]);
		hither = cleanNumber(tokens[5]);
		yon = cleanNumber(tokens[6]);
		worldToCamera = CTM.inverse();
		
		makeCameraToScreenTransform(drawable.getDimensions());
	}

	private void interpretObj(String[] tokens) {
		String quotedFilename = tokens[1];
		int length = quotedFilename.length();
		assert quotedFilename.charAt(0) == '"' && quotedFilename.charAt(length-1) == '"'; 
		String filename = quotedFilename.substring(1, length-1);
		objFile("simp/" + filename + ".obj");
	}
	
	private void objFile(String filename) {
		ObjReader objReader = new ObjReader(filename, defaultColor);
		objReader.read();
		objReader.render(this);
	}
	
	private void push() {
		worldStack.push(CTM);
	}
	private void pop() {
		CTM = worldStack.pop();
	}
	private void wire() {
		renderStyle = RenderStyle.WIREFRAME;
	}
	private void filled() {
		renderStyle = RenderStyle.FILLED;
	}
	
	// this one is complete.
	private void interpretFile(String[] tokens) {
		String quotedFilename = tokens[1];
		int length = quotedFilename.length();
		assert quotedFilename.charAt(0) == '"' && quotedFilename.charAt(length-1) == '"'; 
		String filename = quotedFilename.substring(1, length-1);
		file("simp/" + filename + ".simp");
	}
	private void file(String filename) {
		readerStack.push(reader);
		reader = new LineBasedReader(filename);
	}	

	private void interpretScale(String[] tokens) {
		double sx = cleanNumber(tokens[1]);
		double sy = cleanNumber(tokens[2]);
		double sz = cleanNumber(tokens[3]);
		Transformation trans = new Transformation();
		trans.set(0, 0, sx);
		trans.set(1, 1, sy);
		trans.set(2, 2, sz);
		
		CTM = CTM.multiply(trans);
	}
	private void interpretTranslate(String[] tokens) {
		double tx = cleanNumber(tokens[1]);
		double ty = cleanNumber(tokens[2]);
		double tz = cleanNumber(tokens[3]);
		Transformation trans = new Transformation();
		trans.set(0, 3, tx);
		trans.set(1, 3, ty);
		trans.set(2, 3, tz);
		
		CTM = CTM.multiply(trans);
	}
	private void interpretRotate(String[] tokens) {
		String axisString = tokens[1];
		double angleInDegrees = cleanNumber(tokens[2]);
		double angle = angleInDegrees / 180 * Math.PI;
		Transformation trans;
		if(axisString.equals("X")) {
			double[][] array = {{1, 0, 0, 0}, {0, Math.cos(angle), -Math.sin(angle), 0},
					{0, Math.sin(angle), Math.cos(angle), 0}, {0, 0, 0, 1}};
			trans = new Transformation(array);
		}
		else if (axisString.equals("Y")) {
			double[][] array = { {Math.cos(angle), 0, Math.sin(angle), 0}, {0, 1, 0, 0},
					{-Math.sin(angle), 0, Math.cos(angle), 0}, {0, 0, 0, 1} };
			trans = new Transformation(array);
		}
		else {
			double[][] array = { {Math.cos(angle), -Math.sin(angle), 0, 0}, 
					{Math.sin(angle), Math.cos(angle), 0, 0}, {0, 0, 1, 0}, {0, 0, 0, 1}};
			trans = new Transformation(array);
		}
		CTM = CTM.multiply(trans);
	}
	private static double cleanNumber(String string) {
		return Double.parseDouble(string);
	}
	
	private enum VertexColors {
		COLORED(NUM_TOKENS_FOR_COLORED_VERTEX),
		UNCOLORED(NUM_TOKENS_FOR_UNCOLORED_VERTEX);
		
		private int numTokensPerVertex;
		
		private VertexColors(int numTokensPerVertex) {
			this.numTokensPerVertex = numTokensPerVertex;
		}
		public int numTokensPerVertex() {
			return numTokensPerVertex;
		}
	}
	private void interpretLine(String[] tokens) {			
		Vertex3D[] vertices = interpretVertices(tokens, 2, 1);
	
		line(vertices[0], vertices[1]);
	}	
	private void interpretPolygon(String[] tokens) {			
		Vertex3D[] vertices = interpretVertices(tokens, 3, 1);

		polygon(vertices[0], vertices[1], vertices[2]);
	}
	

	public Vertex3D[] interpretVertices(String[] tokens, int numVertices, int startingIndex) {
		VertexColors vertexColors = verticesAreColored(tokens, numVertices);	
		Vertex3D vertices[] = new Vertex3D[numVertices];
		
		for(int index = 0; index < numVertices; index++) {
			vertices[index] = interpretVertex(tokens, startingIndex + index * vertexColors.numTokensPerVertex(), vertexColors);
		}
		return vertices;
	}
	public VertexColors verticesAreColored(String[] tokens, int numVertices) {
		return hasColoredVertices(tokens, numVertices) ? VertexColors.COLORED :
														 VertexColors.UNCOLORED;
	}
	public boolean hasColoredVertices(String[] tokens, int numVertices) {
		return tokens.length == numTokensForCommandWithNVertices(numVertices);
	}
	public int numTokensForCommandWithNVertices(int numVertices) {
		return NUM_TOKENS_FOR_COMMAND + numVertices*(NUM_TOKENS_FOR_COLORED_VERTEX);
	}

	
	private Vertex3D interpretVertex(String[] tokens, int startingIndex, VertexColors colored) {
		Point3DH point = interpretPoint(tokens, startingIndex);
		
		Color color = defaultColor;
		if(colored == VertexColors.COLORED) {
			color = interpretColor(tokens, startingIndex + NUM_TOKENS_FOR_POINT);
		}
		
		return new Vertex3D(point, color);
	}
	public static Point3DH interpretPoint(String[] tokens, int startingIndex) {
		double x = cleanNumber(tokens[startingIndex]);
		double y = cleanNumber(tokens[startingIndex + 1]);
		double z = cleanNumber(tokens[startingIndex + 2]);

		return new Point3DH(x, y, z);
	}
	public static Color interpretColor(String[] tokens, int startingIndex) {
		double r = cleanNumber(tokens[startingIndex]);
		double g = cleanNumber(tokens[startingIndex + 1]);
		double b = cleanNumber(tokens[startingIndex + 2]);
		
		return new Color(r, g, b);
	}
	public static Point3DH interpretPointWithW(String[] tokens, int startingIndex) {
		double x = cleanNumber(tokens[startingIndex]);
		double y = cleanNumber(tokens[startingIndex + 1]);
		double z = cleanNumber(tokens[startingIndex + 2]);
		double w = cleanNumber(tokens[startingIndex + 3]);
		Point3DH point = new Point3DH(x, y, z, w);
		return point;
	}

	private void line(Vertex3D p1, Vertex3D p2) {
		Vertex3D screenP1 = transformToCamera(p1);
		Vertex3D screenP2 = transformToCamera(p2);

		lineRenderer.drawLine(screenP1, screenP2, depthCueingDrawable);
	}
	public void polygon(Vertex3D p1, Vertex3D p2, Vertex3D p3) {
		Vertex3D screenP1 = transformToCamera(p1);
		Vertex3D screenP2 = transformToCamera(p2);
		Vertex3D screenP3 = transformToCamera(p3);
		
		List<Vertex3D> vertices = new ArrayList<>();
		vertices.add(screenP1);
		vertices.add(screenP2);
		vertices.add(screenP3);
		Intersector intersectorZ = new Intersector() {
			@Override
			public Vertex3D intersect(Vertex3D v1, Vertex3D v2, double plane) {
				double ratio = (v1.getZ() - plane) / (v1.getZ() - v2.getZ());
				double x = v1.getX() + (v2.getX() - v1.getX()) * ratio; 
				double y = v1.getY() + (v2.getY() - v1.getY()) * ratio;
				Point3DH normal = null;
				if(v1.getNormal()!=null && v2.getNormal()!=null) {
					normal = v1.getNormal().
							add((v2.getNormal().subtract(v2.getNormal())).scale(ratio)).normalize();
				}
				Color color = v1.getColor().blendInto(1 - ratio, v2.getColor());
				Vertex3D intersection = new Vertex3D(x, y, plane,
						color, normal, new Point3DH(x, y, plane));
				return intersection;
			}
		};
		Intersector intersectorX2D = new Intersector() {
			@Override
			public Vertex3D intersect(Vertex3D v1, Vertex3D v2, double plane) {
				double ratio = (v1.getX() - plane) / (v1.getX() - v2.getX());
				double y = v1.getY() + (v2.getY() - v1.getY()) * ratio;
				double z = v1.getZ() + (v2.getZ() - v1.getZ()) * ratio;
				Point3DH newCsPoint = v1.getCsPoint().add(
						v2.getCsPoint().subtract(v1.getCsPoint()).scale(ratio));
				Point3DH normal = null;
				if(v1.getNormal()!=null && v2.getNormal()!=null) {
					normal = v1.getNormal().add(
							v2.getNormal().subtract(v2.getNormal()).scale(ratio)).normalize();
				}
				Color color = v1.getColor().blendInto(1 - ratio, v2.getColor());
				Vertex3D intersection = new Vertex3D(plane, y, z, color, normal, newCsPoint);
				return intersection;
			}
		};
		Intersector intersectorY2D = new Intersector() {
			@Override
			public Vertex3D intersect(Vertex3D v1, Vertex3D v2, double plane) {
				double ratio = (v1.getY() - plane) / (v1.getY() - v2.getY());
				double x = v1.getX() + (v2.getX() - v1.getX()) * ratio; 
				double z = v1.getZ() + (v2.getZ() - v1.getZ()) * ratio;
				Point3DH newCsPoint = v1.getCsPoint().add(
						v2.getCsPoint().subtract(v1.getCsPoint()).scale(ratio));
				Point3DH normal = null;
				if(v1.getNormal()!=null && v2.getNormal()!=null) {
					normal = v1.getNormal().
							add((v2.getNormal().subtract(v2.getNormal())).scale(ratio)).normalize();
				}
				Color color = v1.getColor().blendInto(1 - ratio, v2.getColor());
				Vertex3D intersection = new Vertex3D(x, plane, z, color, normal, newCsPoint);
				return intersection;
			}
		};
		vertices = clipper.clipPolygon(vertices, hither, intersectorZ, (v) -> (v.getZ() < hither));
		vertices = clipper.clipPolygon(vertices, yon, intersectorZ, (v) -> (v.getZ() > yon));
		if(vertices == null) {
			return;
		}
		else {
			for(int i = 0; i < vertices.size(); i++) {
				Vertex3D vertex = vertices.get(i);
				vertex = cameraToPerspective(vertex);
				vertices.set(i, vertex);
			}
			vertices = clipper.clipPolygon(vertices, xlow, intersectorX2D, v -> v.getX() > xlow);
			vertices = clipper.clipPolygon(vertices, xhigh, intersectorX2D, v -> v.getX() < xhigh);
			vertices = clipper.clipPolygon(vertices, ylow, intersectorY2D, v -> v.getY() > ylow);
			vertices = clipper.clipPolygon(vertices, yhigh, intersectorY2D, v -> v.getY() < yhigh);
			if(vertices == null) {
				return;
			}
			polygonToScreen(vertices);
		}
	}

	private void polygonToScreen(List<Vertex3D> vertices) {
		for(int i = 0; i < vertices.size(); i++) {
			Vertex3D vertex = vertices.get(i);
			vertex = perspectiveToScreen(vertex);
			vertices.set(i, vertex);
		}
		if(renderStyle == RenderStyle.FILLED) {
			for(int i = 2; i < vertices.size(); i++) {
				if (Polygon.isClockwise(vertices.get(0), vertices.get(i - 1), vertices.get(i)))
					continue;				
				if(shadingStyle == ShadingStyle.FLAT) {
					Polygon polygon = Polygon.make(ks, spExp,
							vertices.get(0), vertices.get(i - 1), vertices.get(i));
					polygon = shadeFace(polygon, lighting);
					filledRenderer.drawPolygon(polygon, depthCueingDrawable,
							flatPixelShader, lighting);
				}
				else if(shadingStyle == ShadingStyle.GOURAUD) {
					Polygon polygon = Polygon.make(ks, spExp,
							vertices.get(0), vertices.get(i - 1), vertices.get(i));
					Vertex3D v0 = shadeVertex(polygon, vertices.get(0), lighting);
					Vertex3D v1 = shadeVertex(polygon, vertices.get(i - 1), lighting);
					Vertex3D v2 = shadeVertex(polygon, vertices.get(i), lighting);
					polygon = Polygon.make(ks, spExp, v0, v1, v2);
					filledRenderer.drawPolygon(polygon, depthCueingDrawable,
							gouraudPixelShader, lighting);
				}
				else {
					Polygon polygon = Polygon.make(ks, spExp,
							vertices.get(0), vertices.get(i - 1), vertices.get(i));
					Vertex3D v0 = setNormalInPolygon(polygon, vertices.get(0));
					Vertex3D v1 = setNormalInPolygon(polygon, vertices.get(i - 1));
					Vertex3D v2 = setNormalInPolygon(polygon, vertices.get(i));
					polygon = Polygon.make(ks, spExp, v0, v1, v2);
					filledRenderer.drawPolygon(polygon, depthCueingDrawable,
							phongPixelShader, lighting);
				}
			}	
		}
		else {
			Vertex3D[] vertexArray = new Vertex3D[vertices.size()];
			Polygon polygon = Polygon.make(vertices.toArray(vertexArray));
			wireframeRenderer.drawPolygon(polygon, drawable, lighting);
		}
	}
	public Polygon shadeFace(Polygon polygon, Lighting lighting) {
		polygon.CalcCenter();
		Vertex3D center = polygon.getCenter();
		center = center.replaceColor(lighting.illuminate(center, 
				polygon.getKs(), polygon.getSpExp()));
		polygon.setCenter(center);
		return polygon;
	}
	public Vertex3D shadeVertex(Polygon polygon, Vertex3D vertex, Lighting lighting) {
		vertex = setNormalInPolygon(polygon, vertex);
		return vertex.replaceColor(lighting.illuminate(vertex, polygon.getKs(), polygon.getSpExp()));
	}
	public Vertex3D setNormalInPolygon(Polygon polygon, Vertex3D vertex) {
		if(vertex.getNormal()!=null) {
			return vertex;
		}
		else {
			polygon.CalcCenter();
			Point3DH faceNormal = polygon.getCenter().getNormal();
			vertex.setNormal(faceNormal);
			return vertex;
		}
	}
	
	@FunctionalInterface
	public interface Intersector {
		public Vertex3D intersect(Vertex3D v1, Vertex3D v2, double plane);
	}

	private Vertex3D perspectiveToScreen(Vertex3D vertex) {
		Transformation coor = vertex.getCoordinates();
		coor = cameraToScreen.multiply(coor);
		Vertex3D transformedV = new Vertex3D(new Point3DH(coor.transpose().getMatrix()[0]),
				vertex.getColor(), vertex.getNormal(), vertex.getCsPoint());
		return transformedV;
	}

	private Vertex3D cameraToPerspective(Vertex3D vertex) {
		Transformation coor = vertex.getCoordinates();
		coor = perspective.multiply(coor);
		Vertex3D transformedV = new Vertex3D(new Point3DH(coor.transpose().getMatrix()[0]),
				vertex.getColor(), vertex.getNormal(), vertex.getCsPoint());
		transformedV = transformedV.euclidean();
		return transformedV;
	}

	private Vertex3D transformToCamera(Vertex3D vertex) {
		Transformation coor = vertex.getCoordinates();
		coor = CTM.multiply(coor);
		coor = worldToCamera.multiply(coor);
		Point3DH point = new Point3DH(coor.transpose().getMatrix()[0]);
		Point3DH normal = null;
		if(vertex.getNormal() != null) {
			Transformation normalVector = vertex.getNormal().getVector();
			normalVector = normalVector.multiply(worldToCamera.inverse());
			normalVector = normalVector.multiply(CTM.inverse());
			normal = new Point3DH(normalVector.getMatrix()[0]);
			normal = normal.normalize();
		}
		Vertex3D transformedV = new Vertex3D( point, vertex.getColor(), normal, point);	
		return transformedV;
	}
}
