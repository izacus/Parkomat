package si.virag.parkomat;

import javax.inject.Singleton;

import dagger.Component;
import si.virag.parkomat.activities.CarManagerActivity;

@Singleton
@Component(modules = {ParkomatApplicationModule.class})
public interface ParkomatComponent {
    void inject(CarManagerActivity activity);
}
