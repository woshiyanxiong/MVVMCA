package com.mvvm.logcat;


import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.view.ContextThemeWrapper;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.mvvm.logcat.activity.LogcatDetailActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FloatingLogcatService extends Service implements View.OnClickListener {


    private WindowManager wm;

    public static void launch(Context context) {
        context.startService(new Intent(context, FloatingLogcatService.class));
    }

    private View mRoot;
    private Spinner mSpinner;
    private View mLlTop;
    private ListView mList;
    private ListView mFilterList;
    private ImageView mIvClean;
    private ImageView mIvFilter;
    private ImageView mIvDrag;
    private ImageView mIvClose;

    private LogcatAdapter mAdapter = new LogcatAdapter();
    private FilterAdapter mFilterAdapter = new FilterAdapter();
    private volatile boolean mReading = false;

    List mFilterDatas = new ArrayList<String>();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mReading) {
            return super.onStartCommand(intent, flags, startId);
        }

        ContextThemeWrapper context = new ContextThemeWrapper(this, androidx.appcompat.R.style.Theme_AppCompat_NoActionBar);
        mRoot = View.inflate(context, R.layout.service_floating_logcat, null);
        mLlTop = mRoot.findViewById(R.id.llTop);
        mSpinner = mRoot.findViewById(R.id.spinner);

        mIvClean = mRoot.findViewById(R.id.iv_clean);
        mIvFilter = mRoot.findViewById(R.id.iv_filter);
        mIvDrag = mRoot.findViewById(R.id.ivDrag);
        mIvClose = mRoot.findViewById(R.id.ivClose);

        mIvClean.setOnClickListener(this);
        mIvFilter.setOnClickListener(this);
        mIvClose.setOnClickListener(this);

        mList = mRoot.findViewById(R.id.list);
        mFilterList = mRoot.findViewById(R.id.listFilter);

        initViews();
        startReadLogcat();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (wm != null) {
            wm.removeView(mRoot);
        }

        stopReadLogcat();
        super.onDestroy();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initViews() {
        final WindowManager.LayoutParams params;
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (wm == null) {
            return;
        } else {
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            int height = size.y;

            params = new WindowManager.LayoutParams(
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,

                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,

                PixelFormat.TRANSLUCENT);
            params.alpha = 1.0f;
            params.dimAmount = 0f;
//            params.gravity = Gravity.CENTER;
            params.gravity = Gravity.TOP | Gravity.LEFT;
            params.windowAnimations = android.R.style.Animation_Dialog;
            params.setTitle("Logcat Viewer");

            if (height > width) {
                params.width = (int) (width * .8);
                params.height = (int) (height * .5);
            } else {
                params.width = (int) (width * .7);
                params.height = (int) (height * .8);
            }

            wm.addView(mRoot, params);
        }

        mList.setBackgroundResource(R.color.logcat_floating_bg);

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
            R.array.logcat_spinner, R.layout.item_float_logcat_dropdown);
        spinnerAdapter.setDropDownViewResource(R.layout.item_float_logcat_dropdown);
        mSpinner.setAdapter(spinnerAdapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String filter = getResources().getStringArray(R.array.logcat_spinner)[position];
                mAdapter.getFilter().filter(filter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        mList.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
        mList.setStackFromBottom(true);
        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                LogcatDetailActivity.launch(getApplicationContext(), mAdapter.getItem(position));
            }
        });

        mFilterList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mFilterList.setVisibility(View.GONE);
                mList.setVisibility(View.VISIBLE);

                String tag = (String) mFilterDatas.get(position);
                if ("不过滤".equals(tag)) {
                    tag = null;
                }
                mAdapter.setFilterTag(tag);
            }
        });

        mIvDrag.setOnTouchListener(new View.OnTouchListener() {

            boolean mIntercepted = false;
            int mLastX;
            int mLastY;
            int mFirstX;
            int mFirstY;
            int mTouchSlop = ViewConfiguration.get(getApplicationContext()).getScaledTouchSlop();

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int totalDeltaX = mLastX - mFirstX;
                int totalDeltaY = mLastY - mFirstY;

                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        mLastX = (int) event.getRawX();
                        mLastY = (int) event.getRawY();
                        mFirstX = mLastX;
                        mFirstY = mLastY;
                        break;
                    case MotionEvent.ACTION_UP:
                        if (!mIntercepted) {
                            v.performClick();
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int deltaX = (int) event.getRawX() - mLastX;
                        int deltaY = (int) event.getRawY() - mLastY;
                        mLastX = (int) event.getRawX();
                        mLastY = (int) event.getRawY();

                        if (Math.abs(totalDeltaX) >= mTouchSlop || Math.abs(totalDeltaY) >= mTouchSlop) {
                            if (event.getPointerCount() == 1) {
                                params.width += deltaX;
                                params.height += deltaY;
                                mIntercepted = true;
                                wm.updateViewLayout(mRoot, params);
                            } else {
                                mIntercepted = false;
                            }
                        } else {
                            mIntercepted = false;
                        }
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        mLlTop.setOnTouchListener(new View.OnTouchListener() {

            boolean mIntercepted = false;
            int mLastX;
            int mLastY;
            int mFirstX;
            int mFirstY;
            int mTouchSlop = ViewConfiguration.get(getApplicationContext()).getScaledTouchSlop();

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int totalDeltaX = mLastX - mFirstX;
                int totalDeltaY = mLastY - mFirstY;

                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        mLastX = (int) event.getRawX();
                        mLastY = (int) event.getRawY();
                        mFirstX = mLastX;
                        mFirstY = mLastY;
                        break;
                    case MotionEvent.ACTION_UP:
                        if (!mIntercepted) {
                            v.performClick();
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int deltaX = (int) event.getRawX() - mLastX;
                        int deltaY = (int) event.getRawY() - mLastY;
                        mLastX = (int) event.getRawX();
                        mLastY = (int) event.getRawY();

                        if (Math.abs(totalDeltaX) >= mTouchSlop || Math.abs(totalDeltaY) >= mTouchSlop) {
                            if (event.getPointerCount() == 1) {
                                params.x += deltaX;
                                params.y += deltaY;
                                mIntercepted = true;
                                wm.updateViewLayout(mRoot, params);
                            } else {
                                mIntercepted = false;
                            }
                        } else {
                            mIntercepted = false;
                        }
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }

    private void startReadLogcat() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                mReading = true;
                BufferedReader reader = null;
                try {
                    Process process = new ProcessBuilder("logcat", "-v", "threadtime").start();
                    reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    while (mReading && (line = reader.readLine()) != null) {
                        if (LogItem.IGNORED_LOG.contains(line)) {
                            continue;
                        }
                        try {
                            final LogItem item = new LogItem(line);
                            mList.post(new Runnable() {
                                @Override
                                public void run() {
                                    mAdapter.append(item);
                                }
                            });
                        } catch (ParseException | NumberFormatException | IllegalStateException e) {
                            e.printStackTrace();
                        }
                    }
                    stopReadLogcat();
                } catch (IOException e) {
                    e.printStackTrace();
                    stopReadLogcat();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    private void stopReadLogcat() {
        mReading = false;
    }


    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.iv_clean) {
            mAdapter.clear();

        } else if (v.getId() == R.id.iv_filter) {
            showFilter();
        } else if (v.getId() == R.id.ivClose) {
            stopSelf();
        }
    }

    private void showFilter() {
        if (mFilterList.getVisibility() == View.VISIBLE) {
            mList.setVisibility(View.VISIBLE);
            mFilterList.setVisibility(View.GONE);
        } else {
            mList.setVisibility(View.GONE);
            mFilterList.setVisibility(View.VISIBLE);

            mFilterList.setAdapter(mFilterAdapter);

            mFilterDatas = Arrays.asList(getResources().getStringArray(R.array.filter));
            mFilterAdapter.setData(mFilterDatas);
        }
    }

    public static void show(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + context.getPackageName()));
            if (context.getPackageManager().queryIntentActivities(intent, 0).isEmpty()) {
                Toast.makeText(context, context.getString(R.string.not_support_on_this_device), Toast.LENGTH_SHORT).show();
            } else {
                context.startActivity(intent);
            }
        } else {
            FloatingLogcatService.launch(context);
        }
    }
}
