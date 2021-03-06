package com.tagtraum.perf.gcviewer.renderer;

import com.tagtraum.perf.gcviewer.ChartRenderer;
import com.tagtraum.perf.gcviewer.ModelChart;
import com.tagtraum.perf.gcviewer.ModelChartImpl;
import com.tagtraum.perf.gcviewer.model.GCModel;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * Superclass for components rendering model data as polygon, polyline
 * or both.
 *
 * Date: Jun 2, 2005
 * Time: 2:53:36 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public abstract class PolygonChartRenderer extends ChartRenderer {
    private boolean drawPolygon;
    private Paint fillPaint;

    public PolygonChartRenderer(ModelChartImpl modelChart) {
        super(modelChart);
        setOpaque(false);
    }

    public void setDrawPolygon(boolean drawPolygon) {
        this.drawPolygon = drawPolygon;
    }

    public void setFillPaint(Paint fillPaint) {
        this.fillPaint = fillPaint;
    }

    public void paintComponent(Graphics2D g2d) {
        if ((!drawPolygon) && (!isDrawLine())) return;
        Polygon polygon = computePolygon(getModelChart(), getModelChart().getModel());
        if (drawPolygon) {
            // don't antialias the polygon, if we are going to antialias the bounding lines
            Object oldAAHint = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
            if (isDrawLine()) {
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            }
            g2d.setPaint(createPaint(polygon));
            g2d.fillPolygon(polygon);
            if (isDrawLine()) {
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAAHint);
            }
        }
        if (isDrawLine()) {
            g2d.setPaint(getLinePaint());
            g2d.drawPolyline(polygon.xpoints, polygon.ypoints, polygon.npoints-1);
        }
    }

    public abstract Polygon computePolygon(ModelChart modelChart, GCModel model);

    protected Paint createPaint(Polygon polygon) {
        if (fillPaint instanceof GradientPaint) {
            GradientPaint gradientPaint = (GradientPaint)fillPaint;
            Point2D point1 = new Point(0, getLowestY(polygon));
            Point2D point2 = new Point(0, getHeight());
            return new GradientPaint(point1, gradientPaint.getColor1(), point2, gradientPaint.getColor2(), false);
        }
        else {
            return fillPaint;
        }
    }

    protected ScaledPolygon createTimeScaledPolygon() {
        return new ScaledPolygon(getModelChart().getScaleFactor(), getHeight()/((double)getModelChart().getMaxPause()), getHeight());
    }

    protected ScaledPolygon createMemoryScaledPolygon() {
        return new ScaledPolygon(getModelChart().getScaleFactor(), getHeight()/((double)getModelChart().getFootprint()), getHeight());
    }

    private static int getLowestY(Polygon polygon) {
        int[] y = polygon.ypoints;
        int min = Integer.MAX_VALUE;
        for (int i=0, max=polygon.npoints; i<max; i++) {
            min = Math.min(min, y[i]);
        }
        return min;
    }

    /**
     * Polygon that scales points upon addition.
     */
    public static class ScaledPolygon extends Polygon {
        private double xScaleFactor;
        private double yScaleFactor;
        private int yOffset;

        public ScaledPolygon(double xScaleFactor, double yScaleFactor, int yOffset) {
            this.xScaleFactor = xScaleFactor;
            this.yScaleFactor = yScaleFactor;
            this.yOffset = yOffset;
        }

        public void addPoint(double x, double y) {
            int scaledY = yOffset - (int)(yScaleFactor * y);
            int scaledX = (int)(xScaleFactor * x);
            final int n = npoints;
            // optimize the polygon as we add points.
            if (n>2 && ypoints[n-2] == scaledY && ypoints[n-1] == ypoints[n-2]) {
                xpoints[n-1] = scaledX;
            }
            else {
                addPoint(scaledX, scaledY);
            }
        }
    }
}
