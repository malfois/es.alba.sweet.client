package es.alba.sweet.client.scan.graph;

import org.eclipse.nebula.visualization.xygraph.figures.Trace.PointStyle;
import org.eclipse.nebula.visualization.xygraph.figures.Trace.TraceType;

import es.alba.sweet.base.xygraph.CColor;

public class Renderer {

	private int			width	= 1;

	private CColor		color	= new CColor();

	private TraceType	line	= TraceType.SOLID_LINE;

	private PointStyle	symbol	= PointStyle.NONE;

	public Renderer() {
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public CColor getColor() {
		return color;
	}

	public void setColor(CColor color) {
		this.color = color;
	}

	public TraceType getLine() {
		return line;
	}

	public void setLine(TraceType line) {
		this.line = line;
	}

	public PointStyle getSymbol() {
		return symbol;
	}

	public void setSymbol(PointStyle symbol) {
		this.symbol = symbol;
	}

}
