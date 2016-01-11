package si.virag.parkomat.modules;

import org.junit.Test;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.Month;

import static junit.framework.Assert.assertEquals;

public class SmsHandlerTest {

    @Test
    public void testSchedulingParser() throws Exception {
        final String schedulingSms = "Placilo parkirnine za MSDZ325 uspesno.\n" +
                "Cona: C6\n" +
                "Veljavnost: 13:32 (11.01.2016)\n" +
                "Cena: 0,70 EUR\n" +
                "Stanje na racunu: 0,50 EUR";


        SmsHandler handler = new SmsHandler(null);
        LocalDateTime ldt = handler.getDateTimeFromParkingSMS(schedulingSms);
        assertEquals(LocalDateTime.of(2016, Month.JANUARY, 11, 13, 32), ldt);
    }
}