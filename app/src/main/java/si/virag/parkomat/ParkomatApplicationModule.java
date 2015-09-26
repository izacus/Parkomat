package si.virag.parkomat;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import si.virag.parkomat.models.CarsManager;
import si.virag.parkomat.models.TimeManager;
import si.virag.parkomat.models.ZoneManager;

@Module
public class ParkomatApplicationModule {

    private Context applicationContext;

    public ParkomatApplicationModule(Context applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Provides
    @Singleton
    public CarsManager provideCarsManager() {
        return new CarsManager(applicationContext);
    }

    @Provides
    @Singleton
    public ZoneManager provideZoneManager() { return new ZoneManager(applicationContext); }

    @Provides
    @Singleton
    public TimeManager provideTimeManager() { return new TimeManager(applicationContext); }

}
