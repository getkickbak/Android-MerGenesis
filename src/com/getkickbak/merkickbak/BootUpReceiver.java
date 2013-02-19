package com.getkickbak.merkickbak;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootUpReceiver extends BroadcastReceiver
{

	@Override
	public void onReceive(Context context, Intent intent)
	{
		String action = intent.getAction();
		if (action.equals(Intent.ACTION_USER_PRESENT))
		{
			Intent myIntent = new Intent(context, MerKICKBAK.class);
			if (MerKICKBAK.singleTask != true)
			{
				myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			}
			//
			// Bring to Foreground
			//
			else
			{
				myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			}
			context.startActivity(myIntent);
		}
	}
}
