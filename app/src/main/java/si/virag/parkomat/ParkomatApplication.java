package si.virag.parkomat;

import android.app.Activity;
import android.app.Application;

import com.raizlabs.android.dbflow.config.FlowLog;
import com.raizlabs.android.dbflow.config.FlowManager;

public class ParkomatApplication extends Application {

    public static ParkomatComponent get(Activity ctx) {
        return ((ParkomatApplication)ctx.getApplication()).getComponent();
    }

    private ParkomatComponent component;

    @Override
    public void onCreate() {
        super.onCreate();
        FlowLog.setMinimumLoggingLevel(FlowLog.Level.V);
        FlowManager.init(this);

        component = DaggerParkomatComponent.builder()
                    .parkomatApplicationModule(new ParkomatApplicationModule(this))
                    .build();
    }

    public ParkomatComponent getComponent() {
        return component;
    }
}
