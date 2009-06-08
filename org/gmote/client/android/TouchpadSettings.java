/**
 * Copyright 2009 Marc Stogaitis and Mimi Sun
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gmote.client.android;

import org.gmote.common.packet.AbstractPacket;

import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class TouchpadSettings extends Activity {
  public static final String MOUSE_SENSITIVITY_SETTINGS_KEY = "mouse_sensitivity";
  public static final String MOUSE_ACCELERATION_SETTINGS_KEY = "mouse_acceleration";
  
  View mContentView = null;
  ActivityUtil mUtil = null;
  
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    mUtil = new ActivityUtil();
    mUtil.onCreate(icicle, this);
    
  }

  @Override
  public void onStart() {
    super.onStart();
    mUtil.onStart(this);
    attachMediaView();
    
  }
  
  @Override
  public void onStop() {
    super.onStop();
    mUtil.onStop();
  }
  
  @Override
  public void onResume() {
    super.onResume();
    mUtil.onResume();
    
    SharedPreferences prefs = getSharedPreferences(GmoteClient.PREFS, MODE_WORLD_READABLE);
    int mouseSensitivityPref = prefs.getInt(TouchpadSettings.MOUSE_SENSITIVITY_SETTINGS_KEY, 50);
    setSeekBar(R.id.mouse_sensitivity_seek, mouseSensitivityPref);
    
    int mouseAccelerationPref = prefs.getInt(TouchpadSettings.MOUSE_ACCELERATION_SETTINGS_KEY, 50);
    setSeekBar(R.id.mouse_acceleration_seek, mouseAccelerationPref);
    
  }
  
  private void setSeekBar(int seekBarId, int seekBarValue) {
    ((SeekBar)findViewById(seekBarId)).setProgress(seekBarValue);
  }

  @Override
  public void onPause() {
    super.onPause();
    mUtil.onPause();
  }
 
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    mUtil.onCreateOptionsMenu(menu);
    return true;
  }
  
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    super.onOptionsItemSelected(item);
    return mUtil.onOptionsItemSelected(item);
  }
  
  @Override
  protected Dialog onCreateDialog(int id) {
    return mUtil.onCreateDialog(id);
  }

  public void handleReceivedPacket(AbstractPacket reply) {
    
  }
  
  private void attachMediaView() {
    int layoutId = R.layout.touchpad_settings;
    int viewId = R.id.touchpad_settings_view;

    setContentView(layoutId);
    mContentView = findViewById(viewId);
    
    ((SeekBar)findViewById(R.id.mouse_acceleration_seek)).setOnSeekBarChangeListener(new SeekBarListener());
    ((SeekBar)findViewById(R.id.mouse_sensitivity_seek)).setOnSeekBarChangeListener(new SeekBarListener());

    findViewById(R.id.touchpad_settings_ok).setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        mUtil.startActivityByClass(Touchpad.class);
      }
    });
    
    findViewById(R.id.touchpad_settings_defaults).setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
       setSeekBar(R.id.mouse_acceleration_seek, 50);
       setSeekBar(R.id.mouse_sensitivity_seek, 50);
      }
    });
    
  }
  
  private class SeekBarListener implements OnSeekBarChangeListener {

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
      if (seekBar.getId() == R.id.mouse_acceleration_seek) {
        saveMousePreferences(MOUSE_ACCELERATION_SETTINGS_KEY, progress);
      } else {
        saveMousePreferences(MOUSE_SENSITIVITY_SETTINGS_KEY, progress);
      }
    }

    public void onStartTrackingTouch(SeekBar arg0) {
      
    }

    public void onStopTrackingTouch(SeekBar arg0) {
      
    }
  }
  
  private void saveMousePreferences(String preferenceName, int sensitivity) {
    SharedPreferences.Editor editor = getSharedPreferences(GmoteClient.PREFS,
        MODE_WORLD_WRITEABLE).edit();
    editor.putInt(preferenceName, sensitivity);
    editor.commit();
  }
}
