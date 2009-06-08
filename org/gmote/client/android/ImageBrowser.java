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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import org.gmote.common.packet.AbstractPacket;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ViewSwitcher;
import android.widget.Gallery.LayoutParams;

public class ImageBrowser extends Activity implements BaseActivity,
    AdapterView.OnItemSelectedListener, ViewSwitcher.ViewFactory {

  ActivityUtil mUtil = null;
  ProgressDialog mDialog = null;
  Gallery mGallery = null;
  List<String> mImages = null;
  //Map<Integer, Drawable> imageCache = new HashMap<Integer, Drawable>();
  
  @Override
  public void onCreate(Bundle icicle) {
    System.out.println("ImageBrowser");
    super.onCreate(icicle);
    mUtil = new ActivityUtil();
    mUtil.onCreate(icicle, this);

    Intent intent = getIntent();
    mImages = intent
        .getStringArrayListExtra(getString(R.string.gmote_stream_playlist));
    setContentView(R.layout.image_browser);
    
    mSwitcher = (ImageSwitcher) findViewById(R.id.switcher);
    mSwitcher.setFactory(this);
    mSwitcher.setInAnimation(AnimationUtils.loadAnimation(this,
        android.R.anim.fade_in));
    mSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this,
        android.R.anim.fade_out));

    mGallery = (Gallery) findViewById(R.id.gallery);
    mGallery.setAdapter(new ImageAdapter(this));
    mGallery.setOnItemSelectedListener(this);
    mGallery.setSelection(intent.getIntExtra(getString(R.string.file_type), 0), false);
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

  @Override
  public void onStart() {
    super.onStart();
    mUtil.onStart(this);
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
  }

  @Override
  public void onPause() {
    super.onPause();
    mUtil.onPause();
  }

  public void handleReceivedPacket(AbstractPacket reply) {

  }

  public void onItemSelected(AdapterView parent, View v, int position, long id) {
    mSwitcher.setImageDrawable(((ImageView)v).getDrawable());
  }

  public Drawable getDrawable(int position) {
    return new BitmapDrawable(getBitmap(position));
  }
  
  public Bitmap getBitmap(int position) {
    Bitmap bitmap = null;
    try {
      URL aURL = new URL(mImages.get(position) + "?sessionId=" + getSessionId());
      URLConnection conn = aURL.openConnection();
      conn.connect();
      InputStream is = conn.getInputStream();
      BufferedInputStream bis = new BufferedInputStream(is);
      Bitmap bm = BitmapFactory.decodeStream(bis);
      bis.close();
      is.close();
      bitmap = bm;
    } catch (IOException e) {
      bitmap = BitmapFactory.decodeResource(getResources(),
          R.drawable.image_viewer);
      Log.e("DEBUGTAG", "Remote Image Exception", e);
    }
    return bitmap;
  }

  private String getSessionId() {
    Remote remote = Remote.getInstance();
    return remote.getSessionId();
  }

  public void onNothingSelected(AdapterView parent) {
  }

  public View makeView() {
    ImageView i = new ImageView(this);
    i.setBackgroundColor(0xFF000000);
    i.setScaleType(ImageView.ScaleType.FIT_CENTER);
    i.setLayoutParams(new ImageSwitcher.LayoutParams(LayoutParams.FILL_PARENT,
        LayoutParams.FILL_PARENT));
    i.setFocusable(false);
    return i;
  }

  private ImageSwitcher mSwitcher;

  public class ImageAdapter extends BaseAdapter {
    public ImageAdapter(Context c) {
      mContext = c;
    }

    public int getCount() {
      return mImages.size();
    }

    public Object getItem(int position) {
      return position;
    }

    public long getItemId(int position) {
      return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
      ImageView i = new ImageView(mContext);
      i.setImageDrawable(getDrawable(position));
      i.setAdjustViewBounds(true);
      i.setLayoutParams(new Gallery.LayoutParams(LayoutParams.WRAP_CONTENT,
          LayoutParams.WRAP_CONTENT));
      return i;
    }

    private Context mContext;
  }

}
