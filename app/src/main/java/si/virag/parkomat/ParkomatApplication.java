package si.virag.parkomat;

import android.app.Activity;
import android.app.Application;
import android.content.res.Configuration;

import com.crashlytics.android.Crashlytics;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.raizlabs.android.dbflow.config.FlowLog;
import com.raizlabs.android.dbflow.config.FlowManager;

import java.util.Locale;

import javax.inject.Inject;

import io.fabric.sdk.android.Fabric;
import si.virag.parkomat.modules.CarsManager;

public class ParkomatApplication extends Application {

    public static ParkomatComponent get(Activity ctx) {
        return ((ParkomatApplication)ctx.getApplication()).getComponent();
    }

    private ParkomatComponent component;

    @Inject
    CarsManager carsManager;

    @Override
    public void onCreate() {
        AndroidThreeTen.init(this);
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        Locale locale = new Locale("sl");
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

        FlowLog.setMinimumLoggingLevel(FlowLog.Level.V);
        FlowManager.init(this);

        component = DaggerParkomatComponent.builder()
                    .parkomatApplicationModule(new ParkomatApplicationModule(this))
                    .build();
        component.inject(this);
        carsManager.pruneCarsAsync();
    }

    public ParkomatComponent getComponent() {
        return component;
    }
}
