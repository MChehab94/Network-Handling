package mchehab.com.networkhandling;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity{

    private ProgressDialog progressDialog;
    private AlertDialog alertDialogNoInternet;

    private BroadcastReceiver broadcastReceiverConnectionChanged = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(!NetworkUtil.isNetworkAvailable(MainActivity.this)){
                displayNoInternetAlertDialog();
            }else{
                if(alertDialogNoInternet != null && alertDialogNoInternet.isShowing()){
                    alertDialogNoInternet.dismiss();
                }
                if(!NetworkUtil.isWifiNetwork(MainActivity.this)){
                    initMobileDataAlertDialog();
                }
            }
        }
    };

    @Override
    protected void onPause() {
    //        remove broadcast receiver when activity stops
        unregisterReceiver(broadcastReceiverConnectionChanged);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    //        register broadcast receiver after starting activity
        registerBroadcastReceiver();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressDialog = new ProgressDialog(this);
    }

    private void registerBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(broadcastReceiverConnectionChanged, intentFilter);
    }

    private void initMobileDataAlertDialog() {
        alertDialogNoInternet = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.alertMobileDataTitle))
                .setNegativeButton(getString(R.string.alertMobileDataNegativeButton), (dialog, which) -> {
                    dialog.dismiss();
                    //perform any tasks if necessary
                }).setPositiveButton(getString(R.string.alertMobileDataPositiveButton), (dialog, which) -> {
                    NetworkUtil.enableWifi(MainActivity.this);
                    showProgressDialog();
                })
                .setCancelable(false)
                .create();
        alertDialogNoInternet.show();
    }

    protected void checkNetwork() {
        if (NetworkUtil.isNetworkAvailable(this)) {
            if (NetworkUtil.isWifiNetwork(this)) {
                Log.d("TAG","device is using wifi network");
            } else {
//                notify the user that s/he is using mobile data (optional)
                displayMobileDataAlertDialog();
            }
        } else {
//            device has no internet connection, inform user
            displayNoInternetAlertDialog();
        }
    }

    protected void displayMobileDataAlertDialog() {
        initMobileDataAlertDialog();
    }

    protected void displayNoInternetAlertDialog() {
        alertDialogNoInternet = new AlertDialog.Builder(this).setTitle(getString(R.string
                .alertNoInternetTitle)).setMessage(getString(R.string.alertNoInternetMessage))
                .setNegativeButton(getString(R.string.alertNoInternetNegativeButton), (dialog, which) -> dialog.dismiss())
                .setPositiveButton(getString(R.string.alertNoInternetPositiveButton), (dialog, which) -> {
                            checkNetwork();
                            dialog.dismiss();
                        })
                .setNeutralButton(getString(R.string.alertNoInternetNeutralButton), (dialog, which) -> {
                            NetworkUtil.enableWifi(MainActivity.this);
                            showProgressDialog();
                        })
                .setCancelable(false).create();
        alertDialogNoInternet.show();
    }

    private void initProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Connecting");
        progressDialog.setCancelable(false);
    }

    private void showProgressDialog() {
        initProgressDialog();
        progressDialog.show();
        new Handler().postDelayed(() -> {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
                if(!NetworkUtil.isNetworkAvailable(this)){
                    displayFailedWifiConnectionAlertDialog();
                }
            }
        }, 3000);
    }

    private void displayFailedWifiConnectionAlertDialog() {
        alertDialogNoInternet = new AlertDialog.Builder(this)
                .setCancelable(false).setTitle(getString(R.string.alertFailedWifiTitle))
                .setNegativeButton(getString(R.string.alertFailedWifiNegativeButton), (dialog, which) -> {
                            dialog.dismiss();
                            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                        })
                .setPositiveButton(getString(R.string.alertFailedWifiPositiveButton), (dialog, which) -> showProgressDialog())
                .create();
        alertDialogNoInternet.show();
    }
}