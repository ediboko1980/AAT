package ch.bailu.aat.views.graph;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;

import ch.bailu.aat.R;
import ch.bailu.aat.description.AverageSpeedDescription;
import ch.bailu.aat.description.AverageSpeedDescriptionAP;
import ch.bailu.aat.dispatcher.DispatcherInterface;
import ch.bailu.aat.gpx.GpxDistanceWindow;
import ch.bailu.aat.gpx.GpxList;
import ch.bailu.aat.gpx.GpxListWalker;
import ch.bailu.aat.gpx.GpxPointNode;
import ch.bailu.aat.gpx.GpxSegmentNode;
import ch.bailu.aat.gpx.attributes.AutoPause;
import ch.bailu.aat.gpx.attributes.MaxSpeed;
import ch.bailu.aat.preferences.SolidAutopause;
import ch.bailu.aat.preferences.general.SolidPostprocessedAutopause;
import ch.bailu.aat.preferences.general.SolidUnit;
import ch.bailu.aat.preferences.presets.SolidPreset;
import ch.bailu.aat.util.ui.AppDensity;
import ch.bailu.aat.util.ui.AppTheme;
import ch.bailu.aat.util.ui.UiTheme;


public class DistanceSpeedGraphView extends AbsGraphView implements SharedPreferences.OnSharedPreferenceChangeListener {


    public DistanceSpeedGraphView(Context context, DispatcherInterface di, UiTheme theme, int... iid) {
        super(context, di, theme, iid);
        setLabelText(context);
    }


    private void setLabelText(Context context) {
        ylabel.setText(Color.WHITE, R.string.speed, sunit.getSpeedUnit());
        ylabel.setText(AppTheme.HL_BLUE, new AverageSpeedDescriptionAP(context).getLabel());
        ylabel.setText(AppTheme.HL_GREEN, new AverageSpeedDescription(context).getLabel());
    }


    @Override
    public void plot(Canvas canvas, GpxList list, int index, SolidUnit sunit, boolean markerMode) {
        int km_factor = (int) (list.getDelta().getDistance()/1000) + 1;

        GraphPlotter[] plotter = new GraphPlotter[3];

        for (int i=0; i<plotter.length; i++) {
            plotter[i] = new GraphPlotter(canvas,getWidth(), getHeight(), 1000 * km_factor,
                    new AppDensity(getContext()), theme);
        }


        for(GraphPlotter p: plotter) {
            p.inlcudeInYScale(list.getDelta().getAttributes().getAsFloat((MaxSpeed.INDEX_MAX_SPEED)));
            p.inlcudeInYScale(0f);
        }




        float meter_pixel = list.getDelta().getDistance()/getWidth();

        new GraphPainter(plotter, meter_pixel).walkTrack(list);

        plotter[0].drawXScale(5, sunit.getDistanceFactor(), isXLabelVisible());
        plotter[0].drawYScale(5, sunit.getSpeedFactor(), false);

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {}


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }


    private class GraphPainter extends GpxListWalker {

        private final GraphPlotter[] plotter;

        private float totalDistance=0;
        private long totalTime=0;

        private float distanceOfSample=0;
        private long timeOfSample=0;


        private final float minDistance;


        private GpxDistanceWindow window;

        final private AutoPause autoPause;


        public GraphPainter(GraphPlotter[] p, float md) {


            int preset = new SolidPreset(getContext()).getIndex();
            final SolidAutopause spause = new SolidPostprocessedAutopause(getContext(), preset);

            autoPause = new AutoPause.Time(
                    spause.getTriggerSpeed(),
                    spause.getTriggerLevelMillis());

            plotter=p;
            minDistance=md*SAMPLE_WIDTH_PIXEL;


        }


        @Override
        public boolean doMarker(GpxSegmentNode marker) {
            return true;
        }


        @Override
        public void doPoint(GpxPointNode point) {
            window.forward(point);
            autoPause.update(point);
            increment(point.getDistance(), point.getTimeDelta());
            plotIfDistance();
        }

        public void increment(float distance, long time) {
            distanceOfSample += distance;
            timeOfSample += time;
        }


        public void plotIfDistance() {
            if (distanceOfSample >= minDistance) {
                totalTime+=timeOfSample;
                totalDistance += distanceOfSample;

                plotTotalAverage();
                plotAverage();

                timeOfSample=0;
                distanceOfSample=0;
            }
        }


        private void plotAverage() {
            if (window.getTimeDelta() > 0) {
                float avg=window.getSpeed();
                plotter[0].plotData(totalDistance, avg, AppTheme.HL_ORANGE);
            }
        }


        private void plotTotalAverage() {
            long timeDelta = totalTime - autoPause.getPauseTime();

            if (timeDelta > 0) {
                float avg = totalDistance / totalTime * 1000;
                plotter[1].plotData(totalDistance, avg, AppTheme.HL_GREEN);

                float avgAp=totalDistance/timeDelta * 1000;
                plotter[2].plotData(totalDistance, avgAp, AppTheme.HL_BLUE);

            }
        }


        @Override
        public boolean doSegment(GpxSegmentNode segment) {
            return true;
        }


        @Override
        public boolean doList(GpxList track) {
            window = new GpxDistanceWindow(track);

            ylabel.setText(AppTheme.HL_ORANGE,
                    window.getLimitAsString(getContext()));
            return true;
        }
    }
}
