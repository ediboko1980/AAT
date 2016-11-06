package ch.bailu.aat.views.map.overlay.gpx;

import ch.bailu.aat.dispatcher.DispatcherInterface;
import ch.bailu.aat.dispatcher.OnContentUpdatedInterface;
import ch.bailu.aat.gpx.GpxInformation;
import ch.bailu.aat.gpx.GpxList;
import ch.bailu.aat.views.map.AbsOsmView;
import ch.bailu.aat.views.map.overlay.OsmOverlay;


public abstract class GpxOverlay extends OsmOverlay implements OnContentUpdatedInterface {
    private final int color;

    private GpxList gpxList=GpxList.NULL_ROUTE;

/*
    public GpxOverlay(AbsOsmView o, DispatcherInterface d, int iid, int c) {
        this(o, c);

        d.addTarget(this, iid);
    }
*/

    public GpxOverlay(AbsOsmView osmPreview, int c) {
        super(osmPreview);


        color = c;
    }


    @Override
    public void onContentUpdated(GpxInformation info) {
        setTrack(info.getGpxList());
    }

    private void setTrack(GpxList gpx) {
        if (gpx == null) gpxList = GpxList.NULL_ROUTE;
        else gpxList = gpx;
    }


    public GpxList getGpxList() {
        return gpxList;
    }


    public int getColor() {
        return color;
    }
}
