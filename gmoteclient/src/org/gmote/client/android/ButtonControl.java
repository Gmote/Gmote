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

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.gmote.common.FileInfo;
import org.gmote.common.MimeTypeResolver;
import org.gmote.common.FileInfo.FileType;
import org.gmote.common.Protocol.Command;
import org.gmote.common.media.MediaMetaInfo;
import org.gmote.common.packet.AbstractPacket;
import org.gmote.common.packet.ListReplyPacket;
import org.gmote.common.packet.MediaInfoPacket;
import org.gmote.common.packet.MediaInfoReqPacket;
import org.gmote.common.packet.RunFileReqPacket;
import org.gmote.common.packet.SimplePacket;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Controller logic for the remote control
 *
 * @author Mimi
 *
 */
public class ButtonControl extends Activity implements BaseActivity {
  private static final String DEBUG_TAG = "Gmote";
  private static Bitmap mBitmap = null;
  private static GmoteMediaPlayer mediaPlayer = null;
  private static boolean inMediaPlayerMode = false;
  private static FileInfo fileInfo;
  private static ActivityUtil mUtil = null;
  
  private static int lastSeenDuration = 0;
  private static int lastSeenPercentage = 0;
  private static MediaMetaInfo mediaMetaInfo = null;
  
  private LocalMediaPlayerListener localMediaPlayerListener = new LocalMediaPlayerListener();
  private View mContentView = null;
  private View mMediaInfoView = null;
  private TextView mMediaTitleView = null;
  private TextView mMediaArtistView = null;
  private ImageView mMediaImageView = null;
  
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    Log.i(DEBUG_TAG, "ButtonControl: onCreate()");
    mUtil = new ActivityUtil();
    mUtil.onCreate(icicle, this);

    Intent intent = getIntent();
    fileInfo = (FileInfo) intent
        .getSerializableExtra(getString(R.string.file_type));
    
    if (fileInfo != null) {
      boolean inSyncMode = intent.getBooleanExtra(getString(R.string.gmote_stream_mode), false);

      if (inSyncMode) {
        ListReplyPacket reply = (ListReplyPacket) intent.getSerializableExtra(getString(R.string.gmote_stream_playlist));
        FileInfo[] playList = reply.getFiles();
        startGmoteSyncMode(fileInfo, playList);
      } else {
        if (inMediaPlayerMode) {
          if (mediaPlayer != null) {
            mediaPlayer.handleCommand(Command.STOP);
          }
        }
        inMediaPlayerMode = false;
        mUtil.send(new RunFileReqPacket(fileInfo));
      }
    }

    /*
     * uncomment this section to support different views based on file type
     * FileType type = null; if (path != null) type =
     * FileType.valueOf(typeName);
     *
     * get view based on file type if (type==FileType.VIDEO ||
     * type==FileType.MUSIC) { } else { attachGenericView(); }
     */
  }

  private void startGmoteSyncMode(FileInfo fileInfo, FileInfo[] playList) {

    Remote remoteInstance = Remote.getInstance();
    String serverUrl = remoteInstance.getServerIp() + ":" + remoteInstance.getServerPort() + "/";
    if (!serverUrl.startsWith("http://")) {
      serverUrl = "http://" + serverUrl;
    }

    if (fileInfo.getFileType() == FileType.MUSIC) {
      startGmoteAudioPlayer(fileInfo, playList, serverUrl);
    } else if (fileInfo.getFileType() == FileType.IMAGE) {
      System.out.println("### images: " + playList.length);
      ArrayList<String> images = new ArrayList<String>();
      int startingImageIndex = 0;
      for (FileInfo file : playList) {
        if (file.equals(fileInfo)) {
          startingImageIndex = images.size();
        }
        if (file.getFileType() == FileType.IMAGE) {
          images.add(createUrlFromFilename(file, serverUrl, true));
        }
      }
      Intent intent = new Intent(this, ImageBrowser.class);
      intent.putStringArrayListExtra(getString(R.string.gmote_stream_playlist), images);
      intent.putExtra(getString(R.string.file_type), startingImageIndex);
      startActivity(intent);
      finish();
    } else {
      startExternalActivity(fileInfo, serverUrl);
    }

  }

  private String createUrlFromFilename(FileInfo fileInfo, String serverUrl, boolean encodeForGmoteHttpServer) {
    String fileName = "files/" + fileInfo.getAbsolutePath();
    String encodedFileName;
    
    try {
      // TODO(mstogaitis): Fix this so that we only use one encoding.
      if (!encodeForGmoteHttpServer) {
        encodedFileName = Uri.encode(fileName);
      } else {
        encodedFileName = URLEncoder.encode(fileName, "UTF-8");
      }
    } catch (UnsupportedEncodingException e) {
      Log.e(DEBUG_TAG, e.getMessage(), e);
      encodedFileName = "UnsupportedEncodingException";
    }
    return serverUrl + encodedFileName;
  }

  private void startGmoteAudioPlayer(FileInfo fileInfo, FileInfo[] playList, String serverUrl) {
    inMediaPlayerMode = true;
    if (mediaPlayer == null) {
      mediaPlayer = new GmoteMediaPlayer(localMediaPlayerListener);
    } else {
      mediaPlayer.setMediaPlayerListener(localMediaPlayerListener);
    }

    List<String> songs = new ArrayList<String>();
    int startingSongIndex = 0;
    for (FileInfo file : playList) {
      if (file.equals(fileInfo)) {
        startingSongIndex = songs.size();
      }
      if (file.getFileType() == FileType.MUSIC) {
        songs.add(createUrlFromFilename(file, serverUrl, true));
      }
    }

    mediaPlayer.playSongs(songs, startingSongIndex);
  }

  private void startExternalActivity(FileInfo fileInfo, String serverUrl) {
    Intent intent = new Intent(Intent.ACTION_VIEW);
    String contentType = MimeTypeResolver.findMimeType(fileInfo.getAbsolutePath());
    
    boolean unknownContentType = contentType.equals(MimeTypeResolver.UNKNOWN_MIME_TYPE); 
    if (unknownContentType) {
      if (fileInfo.getFileType() == FileType.MUSIC) {
        contentType = "audio/unknown";
        unknownContentType = false;
      } else if (fileInfo.getFileType() == FileType.VIDEO) {
        contentType = "video/unknown";
        unknownContentType = false;
      }
    }

    String sessionId = Remote.getInstance().getSessionId();
    if (sessionId == null) {
      Log.i(DEBUG_TAG, "Null session id when trying to start an external activity");
      ActivityUtil.showMessageBox(this, "Error", "Encountered a null session id. Please re-connect to the server by clicking 'menu', 'Gmote Stream' and try again");
      return;
    }
    String url = createUrlFromFilename(fileInfo, serverUrl, !unknownContentType) + "?sessionId=" + sessionId;
    
    if (contentType.toLowerCase().startsWith("audio/") || contentType.toLowerCase().startsWith("video/")) {
      intent.setDataAndType(Uri.parse(url.toString()), contentType);
    } else { 
      intent.setData(Uri.parse(url.toString()));
    }
    Log.i(DEBUG_TAG, "Uri is: " + url.toString());
    try {
      startActivity(intent);
    } catch (ActivityNotFoundException e) {
      Log.e(DEBUG_TAG, e.getMessage(), e);
      Toast.makeText(ButtonControl.this,"Gmote is unable to find an android application to play this file type: " + e.getMessage(), Toast.LENGTH_LONG).show();
    }
  }

  @Override
  public void onStart() {
    super.onStart();
    Log.i(DEBUG_TAG, "ButtonControl: onStart()");
    mUtil.onStart(this);
    attachMediaView();
    
  }

  @Override
  public void onResume() {
    super.onResume();
    Log.i(DEBUG_TAG, "ButtonControl: onResume()");
    mUtil.onResume();
    
    if (inMediaPlayerMode && mediaPlayer != null) {  
      mediaPlayer.setMediaPlayerListener(localMediaPlayerListener);
      if (localMediaPlayerListener != null) {
        localMediaPlayerListener.updateData();
      }
    }
  }

  @Override
  public void onPause() {
    super.onPause();
    Log.i(DEBUG_TAG, "ButtonControl: onPause()");
    mUtil.onPause();
  }

  @Override
  public void onStop() {
    super.onStop();
    Log.i(DEBUG_TAG, "ButtonControl: onStop()");
    mUtil.onStop();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    mUtil.onCreateOptionsMenu(menu);
    menu.removeItem(R.id.menui_remote_control);
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
    System.out.println("ButtonControl got packet");
    if (reply.getCommand() == Command.MEDIA_INFO) {
      updateMediaInfo(((MediaInfoPacket) reply).getMedia());
    }
  }

  private void attachMediaView() {
    int layoutId = R.layout.media_view;
    int viewId = R.id.media_view;

    setContentView(layoutId);
    mContentView = findViewById(viewId);
    ArrayList<View> views = mContentView.getTouchables();

    for (View v : views) {
      if (v == null || v.getId() == R.id.browse)
        continue;
      v.setOnClickListener(mListener);
      if (v.getId() == R.id.fast_forward || v.getId() == R.id.rewind) {
        v.setLongClickable(true);
        v.setOnLongClickListener(mLongListener);
      }
    }

    View browse = findViewById(R.id.browse);
    browse.setOnClickListener(mBrowseListener);

    LayoutInflater factory = LayoutInflater.from(this);
    mMediaInfoView = factory.inflate(R.layout.media_info, null);
    ((ViewGroup) mContentView).addView(mMediaInfoView);
    initMediaInfo();

  }

  void initMediaInfo() {
    mMediaTitleView = (TextView) (mMediaInfoView
        .findViewById(R.id.media_info_title));
    mMediaArtistView = (TextView) (mMediaInfoView
        .findViewById(R.id.media_info_artist));
    mMediaImageView = (ImageView) (mMediaInfoView
        .findViewById(R.id.media_info_image));
  }

  synchronized void updateMediaInfo(MediaMetaInfo mediaMeta) {
    
    if (mediaMeta == null || mediaMeta.getTitle() == null && mediaMeta.getArtist() == null && mediaMeta.getImage() == null) {
      mMediaInfoView.setVisibility(ActivityUtil.VIEW_GONE);
      mContentView.setBackgroundDrawable(null);
      mMediaImageView.setImageBitmap(null);
      mMediaTitleView.setText("");
      mMediaArtistView.setText("");
      
    } else {
      mMediaInfoView.setVisibility(ActivityUtil.VIEW_VISIBLE);
      
      if (mediaMeta.getTitle() != null) {
        mMediaTitleView.setText(mediaMeta.getTitle());
      }
      if (mediaMeta.getArtist() != null) {
        mMediaArtistView.setText(mediaMeta.getArtist());
      }
      
      try {
        byte[] image = mediaMeta.getImage();
        
        if (image != null) {
          int length = image.length;
          if (length > 10) {
            if (mBitmap != null) {
              mBitmap.recycle();
            }
            Log.e(ActivityUtil.DEBUG_TAG, "ButtonControl# changing image");
            mBitmap = BitmapFactory.decodeByteArray(image, 0, length);
            mMediaImageView.setImageBitmap(mBitmap);
            if (mediaMeta.isShowImageOnBackground()) {
              mContentView.setBackgroundDrawable(new BitmapDrawable(mBitmap));
            }
          } else {
            Log.e(ActivityUtil.DEBUG_TAG, "ButtonControl# same album");
          }
        } else {
          Log.w(ActivityUtil.DEBUG_TAG, "ButtonControl# null image");

          if (mBitmap != null && !mediaMeta.isImageSameAsPrevious()) {
            mMediaImageView.setImageResource(R.drawable.audio);
            mContentView.setBackgroundDrawable(null);
            mBitmap.recycle();
            mBitmap = null;
          }
        }
      } catch (Exception e) {
        Log.e(DEBUG_TAG, e.getMessage(), e);
      }
    }
    mediaMeta = null;
  }

  private OnClickListener mBrowseListener = new OnClickListener() {
    public void onClick(View v) {
      Log.d(ActivityUtil.DEBUG_TAG, "ButtonControl# clicked Browse");
      Intent intent = new Intent(ButtonControl.this, Browse.class);
      startActivity(intent);
    }

  };

  View.OnClickListener mListener = new OnClickListener() {
    public void onClick(View v) {
      String command = (String) v.getTag();
      Log.d(ActivityUtil.DEBUG_TAG, "ButtonControl# clicked" + command);
      if (inMediaPlayerMode) {
        mediaPlayer.handleCommand(Command.valueOf(command));
      } else {
        mUtil.send(new SimplePacket(Command.valueOf(command)));
      }
    }
  };
  View.OnLongClickListener mLongListener = new OnLongClickListener() {
    public boolean onLongClick(View v) {
      String commandName = (String) v.getTag();
      Log.d(ActivityUtil.DEBUG_TAG, "ButtonControl# long-clicked" + commandName);
      if (inMediaPlayerMode) {
        mediaPlayer.handleCommand(Command.valueOf(commandName + "_LONG"));
      } else {
        mUtil.send(new SimplePacket(Command.valueOf(commandName + "_LONG")));
      }

      return true;
    }
  };

  private class LocalMediaPlayerListener extends Handler {
    
    @Override
    public synchronized void handleMessage(Message msg) {
      if (msg.what == GmoteMediaPlayer.BUFFERING_UPDATE) {
        int percent = (Integer)msg.obj;
        lastSeenPercentage = percent;
        displayBufferAndTime(lastSeenPercentage, lastSeenDuration);
      } else if (msg.what == GmoteMediaPlayer.MEDIA_INFO_UPDATE) {
        MediaMetaInfo meta = (MediaMetaInfo)msg.obj;
        String songNameAndPath = meta.getTitle();
        if (songNameAndPath == null) {
          return;
        }
        mUtil.send(new MediaInfoReqPacket(songNameAndPath, mBitmap == null));
        
        meta.setTitle(new File(songNameAndPath).getName());
        mediaMetaInfo = meta;
        displayTitle(mediaMetaInfo.getTitle());
      } else if (msg.what == GmoteMediaPlayer.MEDIA_DURATION_UPDATE) {
        int duration = (Integer)msg.obj;
        lastSeenDuration = duration;
        displayBufferAndTime(lastSeenPercentage, lastSeenDuration);
      } else if (msg.what == GmoteMediaPlayer.MEDIA_PLAYER_ERROR) {
        mUtil.cancelDialog();
        String message = (String)msg.obj;
        mediaMetaInfo = new MediaMetaInfo("",message, null,null,false);
        Toast.makeText(ButtonControl.this,"An error occurred during playback. This can happen when your phone experiences connection issues. " + message, Toast.LENGTH_LONG).show();
        updateMediaInfo(mediaMetaInfo);
        lastSeenDuration = 0;
        lastSeenPercentage = 0;
      } else if (msg.what == GmoteMediaPlayer.SESSION_ERROR) {
        String message = (String)msg.obj;
        ActivityUtil.showMessageBox(ButtonControl.this, "PlayOnPhone(beta) Error" , message);
        lastSeenDuration = 0;
        lastSeenPercentage = 0;
      } else if (msg.what == GmoteMediaPlayer.PREPARING_MEDIA) {
        lastSeenDuration = 0;
        lastSeenPercentage = 0;
        displayTitle("");
        mMediaArtistView.setText("loading...");
        displayMediaArt();
        mMediaInfoView.setVisibility(ActivityUtil.VIEW_VISIBLE);
      }
    }


    private void displayMediaArt() {
      if (mBitmap == null) {
        mMediaImageView.setImageResource(R.drawable.audio);
      } else {
        mMediaImageView.setImageBitmap(mBitmap);
        mContentView.setBackgroundDrawable(new BitmapDrawable(mBitmap));
      }
    }
    
    
    private void displayTitle(String title) {
     mMediaTitleView.setText(title);
     mMediaInfoView.setVisibility(ActivityUtil.VIEW_VISIBLE);
    }


    private void displayBufferAndTime(int percent, int duration) {
      mMediaArtistView.setText((duration == 0 ? "" : GmoteMediaPlayer.formatTime(duration) + " - ")
          + "Buffering: " + percent + "%");
      mMediaInfoView.setVisibility(ActivityUtil.VIEW_VISIBLE);
    }
    
    /**
     * Displays the latest data. Typically used when onResume() is called.
     */
    public synchronized void updateData() {
      
      if (mediaMetaInfo != null && mediaMetaInfo.getAlbum() == null && mediaMetaInfo.getArtist() == null && mediaMetaInfo.getImage() == null) {
        displayTitle(mediaMetaInfo.getTitle());
      } else {
        //updateMediaInfo(metaInfo);
      }
      
      displayMediaArt();
      
      if (lastSeenDuration != 0 || lastSeenPercentage != 0) {
        displayBufferAndTime(lastSeenPercentage, lastSeenDuration);
      }
    }
  }
}
