package si.virag.parkomat;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import si.virag.parkomat.models.CarsManager;

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


}
