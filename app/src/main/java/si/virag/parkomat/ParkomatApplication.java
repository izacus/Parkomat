package si.virag.parkomat;

import android.app.Activity;
import android.app.Application;

import com.jakewharton.threetenabp.AndroidThreeTen;
import com.raizlabs.android.dbflow.config.FlowLog;
import com.raizlabs.android.dbflow.config.FlowManager;

import javax.inject.Inject;

import si.virag.parkomat.models.CarsManager;

public class ParkomatApplication extends Application {

    public static ParkomatComponent get(Activity ctx) {
        return ((ParkomatApplication)ctx.getApplication()).getComponent();
    }

    private ParkomatComponent component;

    @Inject
    CarsManager carsManager;

    @Override
    public void onCreate() {
        super.onCreate();
        FlowLog.setMinimumLoggingLevel(FlowLog.Level.V);
        FlowManager.init(this);
        AndroidThreeTen.init(this);

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
