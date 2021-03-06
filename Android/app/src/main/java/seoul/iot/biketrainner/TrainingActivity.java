package seoul.iot.biketrainner;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContentResolverCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.Set;

public class TrainingActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "TrainingActivity";
    private static final int ZONE_SIZE = 6;

    /*
     * Notifications from UsbService will be received here.
     */
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    Toast.makeText(context, "USB Ready", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Toast.makeText(context, "USB Permission not granted", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                    Intent reqConnectIntent = new Intent(TrainingActivity.this, ReqConnectActivity.class);
                    //startActivity(reqConnectIntent);
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    Intent reqConnectIntent1 = new Intent(TrainingActivity.this, ReqConnectActivity.class);
                    //startActivity(reqConnectIntent1);
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    Toast.makeText(context, "USB device not supported", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private UsbService usbService;
    private TextView heartDisplay;
    private MyHandler mHandler;
    private DrawerLayout mNavDrawerLayout;
    private Button mBtnEnd;
    private Button mBtnMenu;
    private ImageView mIvZoneCircle;
    private int[] ZONE_START = {50, 60, 70, 80, 90, 100};

    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);
        mBtnEnd = (Button) findViewById(R.id.t_end);
        mBtnMenu = (Button) findViewById(R.id.t_menu);
        mNavDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        heartDisplay = (TextView) findViewById(R.id.tv_heart);
        mIvZoneCircle = (ImageView) findViewById(R.id.t_zone_circle);

        //ZONE 시작 심박수 설정(ZONE_START[n] : n+1존 시작점)
        ZONE_START[1] = 60;
        ZONE_START[2] = 70;
        ZONE_START[3] = 80;
        ZONE_START[4] = 90;
        ZONE_START[5] = 100;

        Intent intent = getIntent();

        mHandler = new MyHandler(this);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //mNavDrawerLayout.openDrawer(Gravity.LEFT);

        mBtnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mNavDrawerLayout.openDrawer(Gravity.LEFT);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mNavDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mNavDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.training, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onResume() {
        super.onResume();
        setFilters();  // Start listening notifications from UsbService
        startService(UsbService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mUsbReceiver);
        unbindService(usbConnection);
    }

    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(this, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            startService(startService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_training) {
            Intent intent = new Intent(this, TrainingActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_history) {

        } else if (id == R.id.nav_scoreboard) {

        } else if (id == R.id.nav_family) {

        } else if (id == R.id.nav_profile) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setZone(int heart) {
        //Zone 이미지 설정(heart 값에따라 image 변경)
        if (heart < ZONE_START[1]) {
            mIvZoneCircle.setBackground(getDrawable(R.drawable.zone_1));
        }
        else if (heart < ZONE_START[2]) {
            mIvZoneCircle.setBackground(getDrawable(R.drawable.zone_2));
        }
        else if (heart < ZONE_START[3]) {
            mIvZoneCircle.setBackground(getDrawable(R.drawable.zone_3));
        }
        else if (heart < ZONE_START[4]) {
            mIvZoneCircle.setBackground(getDrawable(R.drawable.zone_4));
        }
        else if (heart < ZONE_START[5]) {
            mIvZoneCircle.setBackground(getDrawable(R.drawable.zone_5));
        }
        else {
            mIvZoneCircle.setBackground(getDrawable(R.drawable.zone_6));
        }
    }

    /*
     * This handler will be passed to UsbService. Data received from serial port is displayed through this handler
     */
    private static class MyHandler extends Handler {
        private final WeakReference<TrainingActivity> mActivity;
        private int curHeart;
        private String mstr;

        public MyHandler(TrainingActivity activity) {
            mActivity = new WeakReference<>(activity);
            curHeart = 1;
            mstr = "";
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    String data = (String) msg.obj;
                    //mActivity.get().heartDisplay.setText(data);
                    break;
                case UsbService.CTS_CHANGE:
                    Toast.makeText(mActivity.get(), "CTS_CHANGE",Toast.LENGTH_LONG).show();
                    break;
                case UsbService.DSR_CHANGE:
                    Toast.makeText(mActivity.get(), "DSR_CHANGE",Toast.LENGTH_LONG).show();
                    break;
                case UsbService.SYNC_READ:
                    String buffer = (String) msg.obj;
                    if (stringAppend(buffer)) {
                        try {
                            mActivity.get().heartDisplay.setText(mstr);
                            setZone(Integer.parseInt(mstr));
                            mstr = "";
                        } catch (Exception e) {

                        }
                    }
                    //setZone(Integer.parseInt(buffer));
                    break;
            }
        }

        private boolean stringAppend(String buffer) {
            char[] arrC = buffer.toCharArray();

            if (arrC[arrC.length - 1] >= '0' && arrC[arrC.length - 1] <= '9') {
                mstr += buffer;
                return false;
            } else {
                for (int i = 0; i < arrC.length && (arrC[arrC.length - 1] >= '0' && arrC[arrC.length - 1] <= '9'); i++) {
                    mstr += String.valueOf(arrC[i]);
                }
                return true;
            }
        }

        private void setZone(int heart) {
            //Zone 이미지 설정(heart 값에따라 image 변경)
            if (heart < mActivity.get().ZONE_START[1]) {
                mActivity.get().mIvZoneCircle.setBackground(mActivity.get().getDrawable(R.drawable.zone_1));
            }
            else if (heart < mActivity.get().ZONE_START[2]) {
                mActivity.get().mIvZoneCircle.setBackground(mActivity.get().getDrawable(R.drawable.zone_2));
            }
            else if (heart < mActivity.get().ZONE_START[3]) {
                mActivity.get().mIvZoneCircle.setBackground(mActivity.get().getDrawable(R.drawable.zone_3));
            }
            else if (heart < mActivity.get().ZONE_START[4]) {
                mActivity.get().mIvZoneCircle.setBackground(mActivity.get().getDrawable(R.drawable.zone_4));
            }
            else if (heart < mActivity.get().ZONE_START[5]) {
                mActivity.get().mIvZoneCircle.setBackground(mActivity.get().getDrawable(R.drawable.zone_5));
            }
            else {
                mActivity.get().mIvZoneCircle.setBackground(mActivity.get().getDrawable(R.drawable.zone_6));
            }
        }
    }
}
