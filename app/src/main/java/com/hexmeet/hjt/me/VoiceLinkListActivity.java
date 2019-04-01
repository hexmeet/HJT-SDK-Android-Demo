package com.hexmeet.hjt.me;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.hexmeet.hjt.BaseActivity;
import com.hexmeet.hjt.R;
import com.hexmeet.hjt.utils.ScreenUtil;

import java.util.ArrayList;
import java.util.List;

public class VoiceLinkListActivity extends BaseActivity {
	private final static String TAG = VoiceLinkListActivity.class.getSimpleName();
	private TextView voiceLinkScanTextView;
	private ListView voiceLinkListView;

	private BluetoothAdapter mBluetoothAdapter = null;
	private static final int REQUEST_LOCATION = 0;
	private BluetoothLeScanner scanner = null;
	private boolean permissions_granted=false;
	private boolean mScanning = false;
	private ListAdapter mLeDeviceListAdapter;
	private BluetoothGatt mBluetoothGatt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		 ScreenUtil.initStatusBar(this);
		setContentView(R.layout.voicelink_scan_list_layout);

		voiceLinkListView = (ListView) findViewById(R.id.voicelink_list);
		voiceLinkScanTextView = (TextView) findViewById(R.id.voicelink_scan_textview);
      	findViewById(R.id.back_icon).setOnClickListener(new OnClickListener() {
         	@Override
        	 public void onClick(View v)
         	{
            	finish();
         	}
      	});

		// Initializes Bluetooth adapter.
		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();

		mLeDeviceListAdapter = new ListAdapter();
		voiceLinkListView.setAdapter(mLeDeviceListAdapter);
		voiceLinkListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                voiceLinkScanTextView.setText(getString(R.string.voicelink_time_start));
                if (mScanning) {
					setScanState(false);
					scanner.stopScan(mLeScanCallback);
				}
				BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
				CurrentTimeService.startServer(VoiceLinkListActivity.this);
				mBluetoothGatt = device.connectGatt(VoiceLinkListActivity.this, true, mGattCallback, BluetoothDevice.TRANSPORT_LE);
			}
		});

        startScanVoiceLink();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
        	finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

    @Override
    protected void onPause() {
        if(scanner != null && mScanning) {
            scanner.stopScan(mLeScanCallback);
        }
        super.onPause();
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                voiceLinkScanTextView.setText(getString(R.string.voicelink_time_complete));
            } else {
                voiceLinkScanTextView.setText(getString(R.string.voicelink_time_fail));
            }
        }
    };

	public void startScanVoiceLink() {
		// check bluetooth is available on on
		if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(	BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivity(enableBtIntent);
			return;
		}
		Log.d(TAG, "Bluetooth is switched on");
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
					!= PackageManager.PERMISSION_GRANTED) {
				permissions_granted = false;
				requestLocationPermission();
			} else {
				Log.i(TAG, "Location permission has already been granted. Starting scanning.");
				permissions_granted = true;
			}
		} else {
			// the ACCESS_COARSE_LOCATION permission did not exist before M so....
			permissions_granted = true;
		}
		if (permissions_granted) {
			if (!mScanning) {
				scanLeDevices();
			} else {
				setScanState(false);
				scanner.stopScan(mLeScanCallback);
			}
		}
	}

	private void setScanState(boolean value) {
		mScanning = value;
	}

	private void requestLocationPermission() {
		Log.i(TAG, "Location permission has NOT yet been granted. Requesting permission.");
		if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)){
			Log.i(TAG, "Displaying location permission rationale to provide additional context.");
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Permission Required");
			builder.setMessage("Please grant Location access so this application can perform Bluetooth scanning");
			builder.setPositiveButton(android.R.string.ok, null);
			builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
				public void onDismiss(DialogInterface dialog) {
					Log.d(TAG, "Requesting permissions after explanation");
					ActivityCompat.requestPermissions(VoiceLinkListActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
				}
			});
			builder.show();
		} else {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == REQUEST_LOCATION) {
			Log.i(TAG, "Received response for location permission request.");
			// Check if the only required permission has been granted
			if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				// Location permission has been granted
				Log.i(TAG, "Location permission has now been granted. Scanning.....");
				permissions_granted = true;
				scanLeDevices();
			}else{
				Log.i(TAG, "Location permission was NOT granted.");
			}
		} else {
			super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}
	private void scanLeDevices() {
        voiceLinkScanTextView.setText(getString(R.string.voicelink_scanning));
		scanner = mBluetoothAdapter.getBluetoothLeScanner();
		List<ScanFilter> filters;
		filters = new ArrayList<ScanFilter>();
		ScanFilterFactory filter_factory = ScanFilterFactory.getInstance();
		filters.add(filter_factory.getScanFilter());
		ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
		if (permissions_granted) {
			setScanState(true);
			scanner.startScan(filters, settings, mLeScanCallback);
		} else {
			Log.d(TAG,"Application lacks permission to start Bluetooth scanning");
		}
	}

	private ScanCallback mLeScanCallback = new ScanCallback() {

		public void onScanResult(int callbackType, final ScanResult result) {

			runOnUiThread(new Runnable() {
				@Override
				public void run() {
                    if(result.getDevice().getName()!=null && result.getDevice().getName().contains("VoiceLink")) {
                        mLeDeviceListAdapter.addDevice(result.getDevice());
                        mLeDeviceListAdapter.notifyDataSetChanged();
                    }
				}
			});
		}
	};
	// adaptor
	private class ListAdapter extends BaseAdapter {
		private ArrayList<BluetoothDevice> mLeDevices;

		public ListAdapter() {
			super();
			mLeDevices = new ArrayList<BluetoothDevice>();

		}

		public void addDevice(BluetoothDevice device) {
			if (!mLeDevices.contains(device)) {
				mLeDevices.add(device);
			}
		}

		public BluetoothDevice getDevice(int position) {
			return mLeDevices.get(position);
		}

		public void clear() {
			mLeDevices.clear();
		}

		@Override
		public int getCount() {
			return mLeDevices.size();
		}

		@Override
		public Object getItem(int i) {
			return mLeDevices.get(i);
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@Override
		public View getView(int i, View view, ViewGroup viewGroup) {
			ViewHolder viewHolder;
			// General ListView optimization code.
			if (view == null) {
				view = VoiceLinkListActivity.this.getLayoutInflater().inflate(
						R.layout.voicelink_item, null);
				viewHolder = new ViewHolder();
				viewHolder.text = (TextView) view.findViewById(R.id.textView);
				view.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) view.getTag();
			}
			BluetoothDevice device = mLeDevices.get(i);
			final String deviceName = device.getName();
			if (deviceName != null && deviceName.length() > 0)
				viewHolder.text.setText(deviceName);
			else
				viewHolder.text.setText("unknown device");

			return view;
		}

        private final class ViewHolder {
            public TextView text;
        }
	}
}
