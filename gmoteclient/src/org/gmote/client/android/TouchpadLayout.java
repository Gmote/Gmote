package org.gmote.client.android;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.LinearLayout;

public class TouchpadLayout extends LinearLayout {
  public TouchpadLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    return false;
  }

  @Override
  public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
    return new BaseInputConnection(this, false) {
      @Override
      public boolean sendKeyEvent(KeyEvent event) {
        return super.sendKeyEvent(event);
      }
      
      @Override
      public boolean performEditorAction(int actionCode) {
        sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
        return super.performEditorAction(actionCode);
      }
    };
  }

}
