package ch.bailu.aat.services.cache;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import ch.bailu.aat.preferences.system.SolidCacheSize;
import ch.bailu.aat.services.ServiceContext;
import ch.bailu.aat.services.VirtualService;
import ch.bailu.aat.util.AppBroadcaster;
import ch.bailu.aat.util.MemSize;
import ch.bailu.aat.util.WithStatusText;


public final class CacheService extends VirtualService implements SharedPreferences.OnSharedPreferenceChangeListener, WithStatusText {



    public final ObjectTable table=new ObjectTable();
    public final ObjectBroadcaster broadcaster;

    public final ServiceContext scontext;

    private final SolidCacheSize slimit;


    public CacheService(ServiceContext sc) {
        super(sc);

        scontext = sc;
        broadcaster = new ObjectBroadcaster(getSContext());

        slimit = new SolidCacheSize(sc.getContext());
        slimit.register(this);

        table.limit(this, slimit.getValueAsLong());

        AppBroadcaster.register(getContext(), onFileProcessed, AppBroadcaster.FILE_CHANGED_INCACHE);
    }

    public void onLowMemory() {
        table.limit(this, MemSize.MB);
        slimit.setIndex(1);
    }

    public Obj getObject(String id, Obj.Factory factory) {

        return table.getHandle(id, factory, this);
    }

    public Obj getObject(String id) {
        return table.getHandle(id, getSContext());
    }

    @Override
    public void appendStatusText(StringBuilder builder) {
        table.appendStatusText(builder);
    }


    public void close() {
        getContext().unregisterReceiver(onFileProcessed);

        slimit.unregister(this);
        table.logLocked();
        broadcaster.close();
        table.close(this);
    }


    public void addToBroadcaster(ObjBroadcastReceiver b) {
        broadcaster.put(b);
    }

    private final BroadcastReceiver onFileProcessed = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            table.onObjectChanged(intent, CacheService.this);
        }
    };

    public void log() {
        table.log();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (slimit.hasKey(s)) {
            table.limit(this, slimit.getValueAsLong());
        }
    }
}

