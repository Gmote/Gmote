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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gmote.common.FileInfo;
import org.gmote.common.FileInfo.FileSource;
import org.gmote.common.FileInfo.FileType;
import org.gmote.common.Protocol.Command;
import org.gmote.common.packet.AbstractPacket;
import org.gmote.common.packet.ListReplyPacket;
import org.gmote.common.packet.ListReqPacket;
import org.gmote.common.packet.SimplePacket;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

/**
 * The Browse activity. Displays a list of files in the current directory and
 * handles onclick events to launch files.
 *
 */
public class Browse extends ListActivity implements BaseActivity, RadioGroup.OnCheckedChangeListener {
  static final String DEBUG_TAG = "Gmote";

  AbstractPacket reply;
  FileInfo fileInfo = null;
  ActivityUtil mUtil = null;
  private static boolean inGmoteStreamMode = false;
  private static boolean lastReqWasBaseList = false;
  private static String filePath = "";
  
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    mUtil = new ActivityUtil();
    mUtil.onCreate(icicle, this);

    Intent intent = getIntent();
    fileInfo = (FileInfo)intent.getSerializableExtra(getString(R.string.current_path));
    Log.d(DEBUG_TAG, "Gmote Stream Mode = " + inGmoteStreamMode);

    if (fileInfo == null) {
      mUtil.send(new SimplePacket(Command.BASE_LIST_REQ));
      filePath = "";
      lastReqWasBaseList = true;
      
    } else {
      mUtil.send(new ListReqPacket(fileInfo));
      filePath = fileInfo.getAbsolutePath();
      lastReqWasBaseList = false;  
    }
    
    loadGmoteStreamState();
    
    mUtil.showProgressDialog("Fetching list of files");
    Log.d(DEBUG_TAG, "Browse onCreate");
  }
  
  private void loadGmoteStreamState() {
    SharedPreferences prefs = getSharedPreferences(GmoteClient.PREFS, MODE_WORLD_READABLE);
    inGmoteStreamMode = prefs.getBoolean(GmoteClient.KEY_IN_STREAM_MODE, false);
  }
  
  private void saveGmoteStreamState() {
    SharedPreferences.Editor editor = getSharedPreferences(GmoteClient.PREFS,
        MODE_WORLD_WRITEABLE).edit();
    editor.putBoolean(GmoteClient.KEY_IN_STREAM_MODE, inGmoteStreamMode);
    editor.commit();  
  }
  
  private void initCorrectRadioButton() {
    int radioButtonId;
    if (inGmoteStreamMode) {
      radioButtonId = R.id.radio_play_on_phone; 
    } else {
      radioButtonId = R.id.radio_play_on_computer;
    }
    RadioButton radioButton = (RadioButton)findViewById(radioButtonId);
    radioButton.setChecked(true);
  }

  private void writeTitle() {
    String title = "";    
    if (filePath.length() == 0) {
      title += "Browse";
    } else {
      title += filePath;
    }
    
    
    TextView txtTitle = (TextView)findViewById(R.id.file_list_title);
    txtTitle.setText(title);
  }

  @Override
  public void onStart() {
    super.onStart();
    mUtil.onStart(this);
  }

  private void addButtonListeners() {
    RadioGroup playOn = (RadioGroup)findViewById(R.id.radio_group_play_on);
    playOn.setOnCheckedChangeListener(this);
    
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

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    mUtil.onCreateOptionsMenu(menu);
    menu.removeItem(R.id.menui_browse);
    menu.removeItem(R.id.menui_settings);    
    
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.browse_settings, menu);

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    super.onOptionsItemSelected(item);
    if (item.getItemId() == R.id.menui_browse_settings) {
      showDialog(ActivityUtil.DIALOG_BROWSE_VIEW_SETTINGS);
      return true;
    } else {
      return mUtil.onOptionsItemSelected(item);
    }
  }

  @Override
  protected Dialog onCreateDialog(int id) {
    if (id == ActivityUtil.DIALOG_BROWSE_VIEW_SETTINGS) {
      final String menuOptions[] = { "Show playable files only", "Show all files" };
      return new AlertDialog.Builder(Browse.this).setTitle("View Settings").setItems(menuOptions,
          new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
              Command command;
              if (which == 0) {
                command = Command.SHOW_PLAYABLE_FILES_ONLY_REQ;
              } else {
                command = Command.SHOW_ALL_FILES_REQ;
              }
              mUtil.send(new SimplePacket(command));

              Intent intent = new Intent(Browse.this, Browse.class);
              intent.putExtra(getString(R.string.current_path), fileInfo);
              startActivity(intent);
            }
          }).create();

    } else {
      return mUtil.onCreateDialog(id);
    }
  }

  @Override
  protected void onListItemClick(ListView l, View v, int position, long id) {
    FileInfo file = ((ListReplyPacket) reply).getFiles()[position];
    if (file.isDirectory()) {
      Intent intent = new Intent(this, Browse.class);
      intent.putExtra(getString(R.string.current_path), file);
      startActivityForResult(intent, 0);
    } else if (file.isControllable()) {
      
      //saves the last browsed directory
      SharedPreferences.Editor editor = getSharedPreferences(GmoteClient.PREFS, MODE_WORLD_WRITEABLE).edit();
      editor.putString(GmoteClient.KEY_LAST_BROWSE_DIR, this.getIntent().getStringExtra(getString(R.string.current_path)));
      editor.commit();
    	
      // start the remote control
      Intent intent = new Intent(this, ButtonControl.class);
      intent.putExtra(getString(R.string.file_type), file);
      inGmoteStreamMode = gmoteStreamIsChecked();
      
      if (inGmoteStreamMode) {
        intent.putExtra(getString(R.string.gmote_stream_playlist), reply);
      }
      
      intent.putExtra(getString(R.string.gmote_stream_mode), inGmoteStreamMode);
      setResult(Activity.RESULT_OK, intent);
      finish();
    } else {
      Toast.makeText(Browse.this,
          "I don't know that to do with this file.",
          Toast.LENGTH_SHORT).show();
    }
  }
  protected void onActivityResult(int requestCode, int resultCode,
          Intent data) {
	  //TODO: test passing the intent
	  if (resultCode == Activity.RESULT_OK) {
          if (resultCode == RESULT_OK) {
        	  setResult(Activity.RESULT_OK, data);
              finish();
          }
      }
  }

  private boolean gmoteStreamIsChecked() {
    RadioButton radioButton = (RadioButton) findViewById(R.id.radio_play_on_phone);
    return radioButton.isChecked();
  }

  public void handleReceivedPacket(AbstractPacket tempReply) {
    if (tempReply.getCommand() == Command.PLAY_DVD) {
      Log.d(DEBUG_TAG, "Browse# play dvd ");
      Intent intent = new Intent(Browse.this, ButtonControl.class);
      startActivity(intent);
      finish();
    } else if (tempReply.getCommand() == Command.LIST_REPLY) {
      Log.d(DEBUG_TAG, "Browse# got list ");
      reply = tempReply;
      displayFiles();

      setContentView(R.layout.file_list);
      
      Log.d(DEBUG_TAG, "Browse# setup list ");
      getListView().setTextFilterEnabled(true);
      getListView().requestFocus();
      
      addButtonListeners();
      initCorrectRadioButton();
      writeTitle();
    } else if (tempReply.getCommand() == Command.MEDIA_INFO){
      // Simply ignore this packet.
      return;
    } else {
      Log.e(DEBUG_TAG,"Unexpected packet in browse: " + tempReply.getCommand());
      return;
    }
    reply = tempReply;
    
  }

  class FileAdapterView extends LinearLayout {
    public FileAdapterView(Context context, String fileName, ImageView imageIcon, LinearLayout.LayoutParams imageParams) {
      super(context);
      imageParams = new LinearLayout.LayoutParams(48,
          LayoutParams.WRAP_CONTENT);

      this.setOrientation(HORIZONTAL);
      this.setHorizontalGravity(Gravity.FILL_HORIZONTAL);
      this.setGravity(Gravity.CENTER_VERTICAL);

      addView(imageIcon, imageParams);

      // Add the file name
      LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
          LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
      nameParams.setMargins(5, 1, 1, 1);

      TextView nameControl = new TextView(context);
      nameControl.setText(fileName);
      nameControl.setTextSize(14f);
      nameControl.setTextColor(Color.WHITE);
      addView(nameControl, nameParams);
    }
  }

  class FileAdapter extends BaseAdapter implements Filterable {
    private List<FileInfo> originalFiles;
    private List<FileInfo> filteredFiles;
    private LayoutInflater mInflater;
    private Map<FileType, Bitmap> imageCache = null;
    private Bitmap folderImage;
    private Bitmap unknownFileImage;

    public FileAdapter(Context context, List<FileInfo> files) {
      this.originalFiles = files;
      this.filteredFiles = files;
      
      mInflater = LayoutInflater.from(context);
      if (imageCache == null) {

        imageCache = new HashMap<FileType, Bitmap>();
        imageCache.put(FileType.MUSIC, BitmapFactory.decodeResource(context.getResources(), R.drawable.audio));
        imageCache.put(FileType.VIDEO, BitmapFactory.decodeResource(context.getResources(), R.drawable.video));
        imageCache.put(FileType.DVD_DRIVE, BitmapFactory.decodeResource(context.getResources(), R.drawable.dvd));
        imageCache.put(FileType.PLAYLIST, BitmapFactory.decodeResource(context.getResources(), R.drawable.audio));
        imageCache.put(FileType.POWER_POINT, BitmapFactory.decodeResource(context.getResources(), R.drawable.power_point));
        imageCache.put(FileType.IMAGE, BitmapFactory.decodeResource(context.getResources(), R.drawable.image_viewer));

        folderImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.folder);
        unknownFileImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.file);
      }
    }

    public int getCount() {
      return filteredFiles.size();
    }

    public Object getItem(int position) {
      return filteredFiles.get(position);
    }

    public long getItemId(int position) {
      return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
      FileInfo file = filteredFiles.get(position);

      Bitmap selectedImage = null;

      if (file.getFileType() != null) {
        if (imageCache.containsKey(file.getFileType())) {
          selectedImage = imageCache.get(file.getFileType());
        } else {
          selectedImage = unknownFileImage;
        }
      } else if (file.isDirectory()) {
        selectedImage = folderImage;
      } else {
        selectedImage = unknownFileImage;
      }

      // A ViewHolder keeps references to children views to avoid unneccessary
      // calls
      // to findViewById() on each row.
      ViewHolder holder;

      // When convertView is not null, we can reuse it directly, there is no
      // need
      // to reinflate it. We only inflate a new View when the convertView
      // supplied
      // by ListView is null.
      if (convertView == null) {
        convertView = mInflater.inflate(R.layout.file_list_item, null);

        // Creates a ViewHolder and store references to the two children views
        // we want to bind data to.
        holder = new ViewHolder();
        holder.text = (TextView) convertView.findViewById(R.id.text);
        holder.icon = (ImageView) convertView.findViewById(R.id.icon);

        convertView.setTag(holder);
      } else {
        // Get the ViewHolder back to get fast access to the TextView
        // and the ImageView.
        holder = (ViewHolder) convertView.getTag();
      }

      // Bind the data efficiently with the holder.
      holder.text.setText(file.getFileName());
      holder.icon.setImageBitmap(selectedImage);

      return convertView;
    }

    @Override
    public Filter getFilter() {
      return new Filter() {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
          Log.d(DEBUG_TAG, "Perform Filter: " + constraint);
          FilterResults filterResults = new FilterResults();
          List<FileInfo> results = new ArrayList<FileInfo>();
          String constraintLower = constraint.toString().toLowerCase();
          for (FileInfo fileInfo : originalFiles) {
            if (fileInfo.getFileName().toLowerCase().startsWith(constraintLower)) {
              results.add(fileInfo);
            }
          }
          filterResults.values = results;
          filterResults.count = results.size();
          return filterResults;
        }

        // FilterResults is not using generics.
        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {

          filteredFiles = (List<FileInfo>) results.values;
          if (results.count > 0) {
            notifyDataSetChanged();
          } else {
            notifyDataSetInvalidated();
          }
        }
      };
    }
  }

  static class ViewHolder {
    TextView text;
    ImageView icon;
  }

  public void onCheckedChanged(RadioGroup group, int checkedId) {
    if (checkedId == R.id.radio_play_on_computer) {
      inGmoteStreamMode = false;
    } else {
      inGmoteStreamMode = true;
    }
    displayFiles();
    
    TextView tv = (TextView)findViewById(R.id.browse_top_textview);
    if (tv != null) {
      if (inGmoteStreamMode && lastReqWasBaseList) {
        tv.setPadding(3, 0, 1, 2);
        tv.setText("PlayOnPhone(Beta) allows you to stream media files from your computer to your phone. Please keep in mind that this is an experimental feature and will have limitations. There are several media types that are not currently supported by the Android platform and will therefore not play.\nKnown filetypes that work: mp3, mp4, jpeg\nKnown filetypes that do not work: avi (divx)");
        tv.setVisibility(View.VISIBLE);
      } else {
        tv.setPadding(0,0,0,0);
        tv.setText("");
        tv.setVisibility(View.GONE);
      }
    }
    saveGmoteStreamState();
  }

  private void displayFiles() {
    
    if (reply != null) {
      List<FileInfo> filesToDisplay;
      if (inGmoteStreamMode) {
        FileInfo[] allFiles = ((ListReplyPacket) reply).getFiles();

        // We can only stream songs that are on the file system.
        filesToDisplay = new ArrayList<FileInfo>();
        for (FileInfo file : allFiles) {
          if (file.getFileSource() == FileSource.FILE_SYSTEM) {
            filesToDisplay.add(file);
          }
        }
      } else {
        filesToDisplay = Arrays.asList(((ListReplyPacket) reply).getFiles());
      }
      FileAdapter fileAdapter = new FileAdapter(Browse.this, filesToDisplay);
      setListAdapter(fileAdapter);
      
    }
  }
}
