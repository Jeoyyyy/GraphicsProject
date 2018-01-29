package client;

import line.DDALineRenderer;
import line.LineRenderer;
import polygon.FilledPolygonRenderer;
import polygon.PolygonRenderer;
import polygon.WireframePolygonRenderer;

public class RendererTrio {
	private LineRenderer lineRenderer;
	private PolygonRenderer filledPolygonRenderer;
	private PolygonRenderer wireFramePolygonRenderer;
	
	public RendererTrio() {
		lineRenderer = DDALineRenderer.make();
		filledPolygonRenderer = FilledPolygonRenderer.make();
		wireFramePolygonRenderer = new WireframePolygonRenderer(lineRenderer);
	}
	
	public LineRenderer getLineRenderer() {
		return lineRenderer;
	}
	public PolygonRenderer getFilledRenderer() {
		return filledPolygonRenderer;
	}
	public PolygonRenderer getWireframeRenderer() {
		return wireFramePolygonRenderer;
	}
}
