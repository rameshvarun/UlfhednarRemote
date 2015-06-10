package net.varunramesh.ulfhednarremote;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;

import java.util.Random;


public class RemoteActivity extends Activity {
    public static String TAG = "RemoteActivity";
    private RemoteState state = new RemoteState();
    private NetworkManager netManager;

    public void pushUpdate() {
        synchronized (netManager) { netManager.notify(); }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote);

        findViewById(R.id.remote_frame).setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        SharedPreferences prefs = getSharedPreferences("UlfhednarRemotePreferences", Context.MODE_PRIVATE);
        if(prefs.contains("REMOTE_ID")) {
            state.setId(prefs.getInt("REMOTE_ID", 0));
        } else {
            state.setId((new Random()).nextInt());
            SharedPreferences.Editor e = prefs.edit();
            e.putInt("REMOTE_ID", state.getId());
            e.commit();
        }
        Log.d(TAG, "Remote id: " + state.getId());

        netManager = new NetworkManager(state, (ServerInfo)getIntent().getSerializableExtra("server"));
        new Thread(netManager).start();

        final View attackView = findViewById(R.id.attack);
        final Rect attackRect = new Rect();
        final int[] attackLocation = new int[2];

        final View specialView = findViewById(R.id.special);
        final Rect specialRect = new Rect();
        final int[] specialLocation = new int[2];

        final View moveView = findViewById(R.id.move);
        final Rect moveRect = new Rect();
        final int[] moveLocation = new int[2];

        findViewById(R.id.remote_frame).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                boolean changed = false;

                boolean attack = false;
                boolean special = false;

                attackView.getDrawingRect(attackRect);
                attackView.getLocationOnScreen(attackLocation);
                attackRect.offset(attackLocation[0], attackLocation[1]);

                specialView.getDrawingRect(specialRect);
                specialView.getLocationOnScreen(specialLocation);
                specialRect.offset(specialLocation[0], specialLocation[1]);

                moveView.getDrawingRect(moveRect);
                moveView.getLocationOnScreen(moveLocation);
                moveRect.offset(moveLocation[0], moveLocation[1]);

                float circleRadius = Math.min(moveRect.width(), moveRect.height()) * 0.5f * 0.5f;

                float x = 0;
                float y = 0;

                for(int i = 0; i < event.getPointerCount(); ++i) {
                    int action = event.getActionMasked();
                    if(event.getActionIndex() == i)
                        if(action == MotionEvent.ACTION_UP ||
                                action == MotionEvent.ACTION_POINTER_UP ||
                                action == MotionEvent.ACTION_CANCEL )
                            continue;

                    PointF point = new PointF(event.getX(i), event.getY(i));
                    if (attackRect.contains((int)point.x, (int)point.y)) attack = true;
                    if (specialRect.contains((int)point.x, (int)point.y)) special = true;
                    if (moveRect.contains((int)point.x, (int)point.y)) {
                        x = (point.x - (float)moveRect.centerX()) / circleRadius;
                        y = (point.y - (float)moveRect.centerY()) / circleRadius;

                        float norm = (float)Math.sqrt(x*x + y*y);
                        if(norm > 1) {
                            x /= norm;
                            y /= norm;
                        }
                    }
                }

                if(state.getAttack() != attack) {
                    state.setAttack(attack);
                    changed = true;
                }

                if(state.getSpecial() != special) {
                    state.setSpecial(special);
                    changed = true;
                }

                if(state.getX() != x) {
                    state.setX(x);
                    changed = true;
                }

                if(state.getY() != y) {
                    state.setY(y);
                    changed = true;
                }

                if(changed) pushUpdate();
                return true;
            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            // TODO: If Settings has multiple levels, Up should navigate up
            // that hierarchy.
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
