package com.liberty.ScanApp;

import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.Reader.Priority;
import com.digitalpersona.uareu.UareUException;

import com.digitalpersona.uareu.dpfpddusbhost.DPFPDDUsbHost;
import com.digitalpersona.uareu.dpfpddusbhost.DPFPDDUsbException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.os.Bundle;

import android.content.Context;
import android.app.PendingIntent; 
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

public class MainActivity extends Activity {
    private final int GENERAL_ACTIVITY_RESULT = 1;

    private static final String ACTION_USB_PERMISSION = "com.digitalpersona.uareu.dpfpddusbhost.USB_PERMISSION";
    private TextView m_selectedDevice;

    private Button m_getReader;
    private Button m_enrollrecipient;
    private Button m_authenticaterecipient;

    private Button m_enroll;
    private Button m_identify;
    private Button m_settings;

    private String m_deviceName = "";

    Reader m_reader;

    @Override
    public void onStop() {
        // reset you to initial state when activity stops
        m_selectedDevice.setText("Device: (No Reader Selected)");
        super.onStop();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //enable tracing
        System.setProperty("DPTRACE_ON", "1");

        super.onCreate(savedInstanceState);

        // UI
        setContentView(R.layout.activity_main);
        m_selectedDevice = (TextView) findViewById(R.id.selected_device);
        m_getReader = (Button) findViewById(R.id.get_reader);
        m_enrollrecipient = (Button) findViewById(R.id.enroll_recipient);
        m_authenticaterecipient = (Button) findViewById(R.id.authenticate_recipient);

        m_getReader.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                launchGetReader();
            }
        });

        m_enrollrecipient.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                launchEnrollRecipient();
            }
        });

        m_authenticaterecipient.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                launchAuthenticateRecipient();
            }
        });

        Context ctx = this; // or you can replace **'this'** with your **ActivityName.this**
        try {
            Intent i = ctx.getPackageManager().getLaunchIntentForPackage("com.twidroid.SendTweet");
            ctx.startActivity(i);
            //} catch (NameNotFoundException e) {
        } catch (Exception e) {
            // TODO Auto-generated catch block
        }
    }


    protected void launchGetReader()
    {
        Intent i = new Intent(MainActivity.this, GetReaderActivity.class);
        i.putExtra("device_name", m_deviceName);
        startActivityForResult(i, 1);
    }

    protected void launchEnrollRecipient() {
        Intent i = new Intent(MainActivity.this, EnrollmentActivity.class);
        i.putExtra("device_name", m_deviceName);
        startActivityForResult(i, 1);
    }

    protected void launchAuthenticateRecipient() {
        Intent i = new Intent(MainActivity.this, GetReaderActivity.class);
        i.putExtra("device_name", m_deviceName);
        startActivityForResult(i, 1);
    }

	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
	}

	protected void CheckDevice()
	{
		try
		{
			m_reader.Open(Priority.EXCLUSIVE);
			m_reader.Close();
		} 
		catch (UareUException e1)
		{
			displayReaderNotFound();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (data == null)
		{
			displayReaderNotFound();
			return;
		}
		
		Globals.ClearLastBitmap();
		m_deviceName = (String) data.getExtras().get("device_name");

		switch (requestCode)
		{
		case GENERAL_ACTIVITY_RESULT:

			if((m_deviceName != null) && !m_deviceName.isEmpty())
			{
				m_selectedDevice.setText("Device: " + m_deviceName);

				try {
					Context applContext = getApplicationContext();
					m_reader = Globals.getInstance().getReader(m_deviceName, applContext);

					{
						PendingIntent mPermissionIntent;
						mPermissionIntent = PendingIntent.getBroadcast(applContext, 0, new Intent(ACTION_USB_PERMISSION), 0);
						IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
						applContext.registerReceiver(mUsbReceiver, filter);

						if(DPFPDDUsbHost.DPFPDDUsbCheckAndRequestPermissions(applContext, mPermissionIntent, m_deviceName))
						{
							CheckDevice();
						}
					}
				} catch (UareUException e1)
				{
					displayReaderNotFound();
				}
				catch (DPFPDDUsbException e)
				{
					displayReaderNotFound();
				}

			} else
			{ 
				displayReaderNotFound();
			}

			break;
		}
	}

	private void displayReaderNotFound()
	{
		m_selectedDevice.setText("Device: (No Reader Selected)");
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle("Reader Not Found");
		alertDialogBuilder.setMessage("Plug in a reader and try again.").setCancelable(false).setPositiveButton("Ok",
				new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface dialog,int id) {}
		});

		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}

	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() 
	{
		public void onReceive(Context context, Intent intent) 
		{
			String action = intent.getAction();
			if (ACTION_USB_PERMISSION.equals(action))
			{
				synchronized (this)
				{
					UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
					if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false))
					{
						if(device != null)
						{
							//call method to set up device communication
							CheckDevice();
						}
					}
	    			else
	    			{
	    				// do nothing
	    			}
	    		}
	    	}
	    }
	};
}
