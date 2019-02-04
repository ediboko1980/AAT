package ch.bailu.aat.description;

import android.content.Context;

import ch.bailu.aat.R;
import ch.bailu.aat.gpx.GpxListAttributes;
import ch.bailu.aat.gpx.GpxInformation;

public class TimeApDescription extends TimeDescription {

    public TimeApDescription(Context context) {
        super(context);
    }

    @Override
    public void onContentUpdated(int iid, GpxInformation info) {
        final long autoPause = info.getAttributes().getAsLong(GpxListAttributes.INDEX_AUTO_PAUSE);
        setCache(info.getTimeDelta() - autoPause);
    }


    @Override
    public String getLabel() {
        return getContext().getString(R.string.time_ap);
    }

}
