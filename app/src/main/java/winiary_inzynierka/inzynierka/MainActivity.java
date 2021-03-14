package winiary_inzynierka.inzynierka;

import android.app.TimePickerDialog;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.SeekBar.OnSeekBarChangeListener;

import jp.co.melco.mxcomponent.MELMxCommunication;
import jp.co.melco.mxcomponent.MELMxOpenSettings;
import jp.co.melco.mxcomponent.MELMxErrDefine;

public class MainActivity extends AppCompatActivity {

    private MELMxCommunication polaczenie = new MELMxCommunication();
    private MELMxOpenSettings ustawieniaPolaczenia = new MELMxOpenSettings();
    private Boolean czy_polaczony = Boolean.FALSE;
    private ToggleButton btn_connect;
    private ScrollView scrollView;
    private ToggleButton btn_swiatlo_on_off;
    private CheckBox checkBox_wl_1;
    private Button btn_wl_1;
    private CheckBox checkBox_wl_2;
    private Button btn_wl_2;
    private CheckBox checkBox_wyl_1;
    private Button btn_wyl_1;
    private CheckBox checkBox_wyl_2;
    private Button btn_wyl_2;
    private CheckBox checkBox_czujnik_osw;
    private SeekBar seekBar_czuj_osw;
    private ProgressBar progressBar_osw;
    private CheckBox checkBox_wl_ster_temp;
    private TextView textView_aktualna_temp;
    private TextView textView_zad_temp_wartosc;
    private SeekBar seekBar_zad_temp;
    private CheckBox checkBox_wl_wentylator;

    private boolean czy_polaczenie_ok(int kod_bledu) {
        if(true)
            return true;
        if (kod_bledu == MELMxErrDefine.MX_SUCCESS) {
            return true;
        } else {
            Toast.makeText(MainActivity.this, "Wystąpił błąd połączenia",Toast.LENGTH_LONG).show();
            zamknij_polaczenie();
        }
        return false;
    }

    private void odswiez_dane() {
        int[] temp = new int[3];
        //pobranie danych
        if (czy_polaczenie_ok(polaczenie.readDeviceRandom(new String[]{"Y2", "D40", "D30"}, temp))){
            if (temp[0] != 0) {// sprawdzenie, czy światło jest zapalone czy zgaszone
                //jesli swiatlo zapalone
                btn_swiatlo_on_off.setChecked(true);// ustaw przycisk "Połącz"
            } else {
                //jeśli swiatlo zgaszone
                btn_swiatlo_on_off.setChecked(false);//ustaw przycisk "Wyłącz"
            }
            progressBar_osw.setProgress(temp[1]);// wypełnienie paska pomiaru naświetlenia
            //zaktualizowanie pola tekstowego z pomiarem temperatury
            textView_aktualna_temp.setText(String.valueOf((float) temp[2] / 2) + "*C");
        }
    }

    /*
    Kolejność pobierania:
    1. swiatlo czy włączone
    2. chB_wl_1
    3. wl_1 godz
    4. wl_1 min
    5. 6. 7. chb_wyl_1
    8. 9. 10. chb_wl_2
    11. 12. 13. chb_wyl_2
    14. chB_czuj_osw
    15. czuj_osw_wart_zad
    16. czuj_osw_aktualna
    17. chB_wl_ster_temp
    18. aktualna_temp
    19. zad_temp
    20. chB_wl_went_w_oknie
     */
    private void pobierz_przy_polaczeniu() {
        int[] temp = new int[20];
        if (czy_polaczenie_ok(polaczenie.readDeviceRandom(new String[]{"Y2", "L32", "D220", "D221", "L64", "D240", "D241", "L48", "D230", "D231", "L80", "D250", "D251", "L0", "D200", "D40", "L96", "D30", "D260", "Y1"}, temp))) {
            btn_swiatlo_on_off.setChecked(temp[0] != 0);
            checkBox_wl_1.setChecked(temp[1] != 0);
            btn_wl_1.setText(String.format("%02d", temp[2]) + ":" + String.format("%02d", temp[3]));
            checkBox_wyl_1.setChecked(temp[4] != 0);
            btn_wyl_1.setText(String.format("%02d", temp[5]) + ":" + String.format("%02d", temp[6]));
            checkBox_wl_2.setChecked(temp[7] != 0);
            btn_wl_2.setText(String.format("%02d", temp[8]) + ":" + String.format("%02d", temp[9]));
            checkBox_wyl_2.setChecked(temp[10] != 0);
            btn_wyl_2.setText(String.format("%02d", temp[11]) + ":" + String.format("%02d", temp[12]));
            checkBox_czujnik_osw.setChecked(temp[13] != 0);
            seekBar_czuj_osw.setProgress(temp[14]);
            progressBar_osw.setProgress(temp[15]);
            checkBox_wl_ster_temp.setChecked(temp[16] != 0);
            textView_aktualna_temp.setText(String.valueOf((float) temp[17] / 2) + "*C");
            textView_zad_temp_wartosc.setText(String.valueOf((float) temp[18] / 2) + "*C");
            seekBar_zad_temp.setProgress(temp[18]);
            checkBox_wl_wentylator.setChecked(temp[19] != 0);
        }
    }

    private void otworz_polaczenie() {
        //ustawienie parametrów połączenia
        ustawieniaPolaczenia.unitType = 0x2001;
        ustawieniaPolaczenia.cpuType = 0x0210;
        ustawieniaPolaczenia.hostAddress = "192.168.22.150";
        ustawieniaPolaczenia.destinationPortNumber = 5014;
        ustawieniaPolaczenia.cpuTimeOut = 400;
        //próba otwarcia połączenia
        if (czy_polaczenie_ok(polaczenie.open(ustawieniaPolaczenia, ""))) {
            //jesli nawiązeno połączenie ze sterownikiem
            czy_polaczony = true;
            scrollView.setVisibility(View.VISIBLE);
            btn_connect.setChecked(true);
            pobierz_przy_polaczeniu();
        }
    }

    private void zamknij_polaczenie() {
        polaczenie.close();
        czy_polaczony = false;
        btn_connect.setChecked(false);
        scrollView.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout);

        scrollView = (ScrollView) findViewById(R.id.scrollView);
        scrollView.setVisibility(View.INVISIBLE);

        //~~~~~~~~~~~~~~~~~~PRZYCISK POŁĄCZENA~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
        btn_connect = (ToggleButton) findViewById(R.id.btn_main_conn_disconn);
        btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((ToggleButton) view).isChecked()) {
                    //jesli niepodlaczony
                    otworz_polaczenie();
                } else {
                    // jesli podlaczony
                    zamknij_polaczenie();
                }
            }
        });

        //~~~~~~~~~~~~~~~~~~PPRZYCISK WŁ/WYŁ ŚWAITLO~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
        btn_swiatlo_on_off = (ToggleButton) findViewById(R.id.btn_swiatlo_on_off);
        btn_swiatlo_on_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int[] temp = new int[1];
                temp[0] = 1;
                if (((ToggleButton) view).isChecked()) {
                    //jesli swiatlo zgaszone
                    if (czy_polaczenie_ok(polaczenie.writeDeviceBlock(/*"M0"*/"M0", 1, temp))) {//zapalenie swiatla
                        ustawiono_h_m_lub_wl_wyl();
                        ((ToggleButton) view).setChecked(true);
                    } else {
                        ((ToggleButton) view).setChecked(false);
                    }
                } else {
                    //jesli swiatlo zapalone
                    if (czy_polaczenie_ok(polaczenie.writeDeviceBlock("M16", 1, temp))) {//zgaszenie swiatla
                        ustawiono_h_m_lub_wl_wyl();
                        ((ToggleButton) view).setChecked(false);
                    } else {
                        ((ToggleButton) view).setChecked(true);
                    }
                }

            }
        });

        //~~~~~~~~~~~~~~~~~~F_T_WL_1~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
        checkBox_wl_1 = (CheckBox) findViewById(R.id.cb_ft_wl_1);
        checkBox_wl_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (((CheckBox) view).isChecked()) {
                    //jesli nie włączone->włącz
                    if (czy_polaczenie_ok(polaczenie.writeDeviceRandom(new String[]{"L16", "L32"}, new int[]{1, 1}))) {
                        ustawiono_h_m_lub_wl_wyl();
                        ((CheckBox) view).setChecked(true);
                    } else {
                        ((CheckBox) view).setChecked(false);
                    }
                } else {
                    //jesli nie włączone->wyłącz
                    if (czy_polaczenie_ok(polaczenie.writeDeviceRandom(new String[]{"L16", "L32"}, new int[]{1, 0}))) {
                        ((CheckBox) view).setChecked(false);
                    } else {
                        ((CheckBox) view).setChecked(true);
                    }
                }
            }
        });

        btn_wl_1 = (Button) findViewById(R.id.btn_ft_wl_1);
        btn_wl_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog nowy_dialog = new TimePickerDialog(view.getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int godzina, int minuta) {
                        if (czy_polaczenie_ok(polaczenie.writeDeviceRandom(new String[]{"D220", "D221"}, new int[]{godzina, minuta}))) {
                            btn_wl_1.setText(String.format("%02d", godzina) + ":" + String.format("%02d", minuta));
                        }
                    }
                }, 0, 0, true);
                nowy_dialog.show();
            }
        });

        //~~~~~~~~~~~~~~~~~~F_T_WYL_1~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
        checkBox_wyl_1 = (CheckBox) findViewById(R.id.cb_ft_wyl_1);

        checkBox_wyl_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (((CheckBox) view).isChecked()) {
                    //jesli nie włączone->włącz
                    if (czy_polaczenie_ok(polaczenie.writeDeviceRandom(new String[]{"L16", "L64"}, new int[]{1, 1}))) {
                        ustawiono_h_m_lub_wl_wyl();
                        ((CheckBox) view).setChecked(true);
                    } else {
                        ((CheckBox) view).setChecked(false);
                    }
                } else {
                    //jesli nie włączone->wyłącz
                    if (czy_polaczenie_ok(polaczenie.writeDeviceRandom(new String[]{"L16", "L64"}, new int[]{1, 0}))) {
                        ((CheckBox) view).setChecked(false);
                    } else {
                        ((CheckBox) view).setChecked(true);
                    }
                }
            }
        });

        btn_wyl_1 = (Button) findViewById(R.id.btn_ft_wyl_1);
        btn_wyl_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog nowy_dialog = new TimePickerDialog(view.getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int godzina, int minuta) {
                        if (czy_polaczenie_ok(polaczenie.writeDeviceRandom(new String[]{"D240", "D241"}, new int[]{godzina, minuta}))) {
                            btn_wyl_1.setText(String.format("%02d", godzina) + ":" + String.format("%02d", minuta));
                        }
                    }
                }, 0, 0, true);
                nowy_dialog.show();
            }
        });

        //~~~~~~~~~~~~~~~~~~F_T_WL_2~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
        checkBox_wl_2 = (CheckBox) findViewById(R.id.cb_ft_wl_2);
        checkBox_wl_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (((CheckBox) view).isChecked()) {
                    //jesli nie włączone->włącz
                    if (czy_polaczenie_ok(polaczenie.writeDeviceRandom(new String[]{"L16", "L48"}, new int[]{1, 1}))) {
                        ustawiono_h_m_lub_wl_wyl();
                        ((CheckBox) view).setChecked(true);
                    } else {
                        ((CheckBox) view).setChecked(false);
                    }
                } else {
                    //jesli nie włączone->wyłącz
                    if (czy_polaczenie_ok(polaczenie.writeDeviceRandom(new String[]{"L16", "L48"}, new int[]{1, 0}))) {
                        ((CheckBox) view).setChecked(false);
                    } else {
                        ((CheckBox) view).setChecked(true);
                    }
                }
            }
        });

        btn_wl_2 = (Button) findViewById(R.id.btn_ft_wl_2);
        btn_wl_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog nowy_dialog = new TimePickerDialog(view.getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int godzina, int minuta) {
                        if (czy_polaczenie_ok(polaczenie.writeDeviceRandom(new String[]{"D230", "D231"}, new int[]{godzina, minuta}))) {
                            btn_wl_2.setText(String.format("%02d", godzina) + ":" + String.format("%02d", minuta));
                        }
                    }
                }, 0, 0, true);
                nowy_dialog.show();
            }
        });

        //~~~~~~~~~~~~~~~~~~F_T_WYL_2~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
        checkBox_wyl_2 = (CheckBox) findViewById(R.id.cb_ft_wyl_2);

        checkBox_wyl_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (((CheckBox) view).isChecked()) {
                    //jesli nie włączone->włącz
                    if (czy_polaczenie_ok(polaczenie.writeDeviceRandom(new String[]{"L16", "L80"}, new int[]{1, 1}))) {
                        ustawiono_h_m_lub_wl_wyl();
                        ((CheckBox) view).setChecked(true);
                    } else {
                        ((CheckBox) view).setChecked(false);
                    }
                } else {
                    //jesli nie włączone->wyłącz
                    if (czy_polaczenie_ok(polaczenie.writeDeviceRandom(new String[]{"L16", "L80"}, new int[]{1, 0}))) {
                        ((CheckBox) view).setChecked(false);
                    } else {
                        ((CheckBox) view).setChecked(true);
                    }
                }
            }
        });

        btn_wyl_2 = (Button) findViewById(R.id.btn_ft_wyl_2);
        btn_wyl_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog nowy_dialog = new TimePickerDialog(view.getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int godzina, int minuta) {
                        if (czy_polaczenie_ok(polaczenie.writeDeviceRandom(new String[]{"D250", "D251"}, new int[]{godzina, minuta}))) {
                            btn_wyl_2.setText(String.format("%02d", godzina) + ":" + String.format("%02d", minuta));
                        }
                    }
                }, 0, 0, true);
                nowy_dialog.show();
            }
        });

        //~~~~~~~~~~~~~~~~~~~SEEKBAR (USTAWIANIE PROGU OŚWIETLENIA)~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
        seekBar_czuj_osw = (SeekBar) findViewById(R.id.seekBar_set_prog_osw);
        seekBar_czuj_osw.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            int postep = 0;
            int wartosc_poczatkowa = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                postep = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                wartosc_poczatkowa = seekBar.getProgress();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (wartosc_poczatkowa != postep) {
                    if (czy_polaczenie_ok(polaczenie.writeDeviceBlock("D200", 1, new int[]{postep}))) {
                        seekBar.setProgress(postep);
                        Toast.makeText(MainActivity.this, "Próg oświetlenia ustawiono na " + postep + "%",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        seekBar.setProgress(wartosc_poczatkowa);
                    }
                }
            }
        });

        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~PROGRESSBAR~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
        progressBar_osw = (ProgressBar) findViewById(R.id.progressBar_get_nasw);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~CZUJNIK OSW CHECKBOX~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
        checkBox_czujnik_osw = (CheckBox) findViewById(R.id.cb_czujnik_osw);

        checkBox_czujnik_osw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((CheckBox) view).isChecked()) {
                    //jesli nie włączone->włącz
                    if (czy_polaczenie_ok(polaczenie.writeDeviceBlock("L0", 1, new int[]{1}))) {
                        ustawiono_czujnik_osw();
                        ((CheckBox) view).setChecked(true);
                    } else {
                        ((CheckBox) view).setChecked(false);
                    }
                } else {
                    //jesli nie włączone->wyłącz
                    if (czy_polaczenie_ok(polaczenie.writeDeviceBlock("L0", 1, new int[]{0}))) {
                        ((CheckBox) view).setChecked(false);
                    } else {
                        ((CheckBox) view).setChecked(true);
                    }
                }
            }
        });

        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~WL_STER_TEMP_CHECKBOX~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
        checkBox_wl_ster_temp = (CheckBox) findViewById(R.id.cb_ster_temp_ON);
        checkBox_wl_ster_temp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((CheckBox) view).isChecked()) {
                    //jesli nie włączone->włącz
                    if (czy_polaczenie_ok(polaczenie.writeDeviceBlock("L96", 1, new int[]{1}))) {
                        ((CheckBox) view).setChecked(true);
                    } else {
                        ((CheckBox) view).setChecked(false);
                    }
                } else {
                    //jesli nie włączone->wyłącz
                    if (czy_polaczenie_ok(polaczenie.writeDeviceBlock("L96", 1, new int[]{0}))) {
                        ((CheckBox) view).setChecked(false);
                    } else {
                        ((CheckBox) view).setChecked(true);
                    }
                }
            }
        });

        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~WL_WENTYLATOR_CHECKBOX~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
        checkBox_wl_wentylator = (CheckBox) findViewById(R.id.cb_wl_wentylator);
        checkBox_wl_wentylator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((CheckBox) view).isChecked()) {
                    //jesli nie włączone->włącz
                    if (czy_polaczenie_ok(polaczenie.writeDeviceBlock("L112", 1, new int[]{1}))) {
                        ((CheckBox) view).setChecked(true);
                    } else {
                        ((CheckBox) view).setChecked(false);
                    }
                } else {
                    //jesli nie włączone->wyłącz
                    if (czy_polaczenie_ok(polaczenie.writeDeviceBlock("L112", 1, new int[]{0}))) {
                        ((CheckBox) view).setChecked(false);
                    } else {
                        ((CheckBox) view).setChecked(true);
                    }
                }
            }
        });

        //~~~~~~~~~~~~~~~~~~~~~~~~~~AKTUALNA_TEMP_TW~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
        textView_aktualna_temp = (TextView) findViewById(R.id.tv_aktualna_temp_wartosc);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~SEEKBAR i TEXTVIEW_ZAD_TEMP~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
        textView_zad_temp_wartosc = (TextView) findViewById(R.id.tv_zad_temp_wartosc);
        seekBar_zad_temp = (SeekBar) findViewById(R.id.seekBar_zad_temp);
        seekBar_zad_temp.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            int postep = 0;
            int wartosc_poczatkowa = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                postep = i;
                textView_zad_temp_wartosc.setText(String.valueOf((float) postep / 2) + "*C");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBarr) {
                wartosc_poczatkowa = seekBarr.getProgress();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBarr) {
                if (wartosc_poczatkowa != postep) {
                    if (czy_polaczenie_ok(polaczenie.writeDeviceBlock("D210", 1, new int[]{postep}))) {
                        seekBarr.setProgress(postep);
                    } else {
                        textView_zad_temp_wartosc.setText(String.valueOf((float) wartosc_poczatkowa / 2) + "*C");
                        seekBarr.setProgress(wartosc_poczatkowa);
                    }
                }
            }
        });

        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ODŚWIEŻANIE WARTOŚCI CO 1 S~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
        final Handler ha = new Handler();
        ha.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (czy_polaczony)
                    odswiez_dane();
                ha.postDelayed(this, 1000);
            }
        }, 1000);
    }

    private void ustawiono_czujnik_osw() {
        checkBox_wl_1.setChecked(false);
        checkBox_wyl_1.setChecked(false);
        checkBox_wl_2.setChecked(false);
        checkBox_wyl_2.setChecked(false);
    }

    private void ustawiono_h_m_lub_wl_wyl() {
        checkBox_czujnik_osw.setChecked(false);
    }
}
