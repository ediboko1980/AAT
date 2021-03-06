package ch.bailu.aat.services.tracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ch.bailu.aat.gpx.GpxInformation;
import ch.bailu.aat.util.AppBroadcaster;
import ch.bailu.aat.util.WithStatusText;
import ch.bailu.aat.services.ServiceContext;
import ch.bailu.aat.services.VirtualService;

public final class TrackerService extends VirtualService implements WithStatusText {

    private final TrackerInternals internal;


    public TrackerService(ServiceContext sc) {
        super(sc);
        internal = new TrackerInternals(getSContext());

        AppBroadcaster.register(getContext(), onLocation, AppBroadcaster.LOCATION_CHANGED);
    }


    public State getState() {
        return internal.state;
    }



    public GpxInformation getLoggerInformation() {
        return internal.logger;
    }


    private final BroadcastReceiver onLocation = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            internal.state.updateTrack();
        }
    };



    public int getPresetIndex() {
        return internal.presetIndex;
    }



    @Override
    public void appendStatusText(StringBuilder builder) {
        builder .append("Log to: ")
                .append(internal.logger.getFile().getPathName());
    }

    public void close() {
        internal.close();
        getContext().unregisterReceiver(onLocation);
    }

}
