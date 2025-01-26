package com.aliangmaker.media;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.media.*;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListPopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.aliangmaker.media.databinding.ActivityPlayVideoBinding;
import com.aliangmaker.media.event.DownloadEvent;
import com.aliangmaker.media.event.SQLiteOpenHelper;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import master.flame.danmaku.controller.DrawHandler;
import master.flame.danmaku.danmaku.loader.ILoader;
import master.flame.danmaku.danmaku.loader.android.DanmakuLoaderFactory;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.IDisplayer;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.danmaku.parser.IDataSource;
import master.flame.danmaku.danmaku.parser.android.BiliDanmukuParser;
import master.flame.danmaku.ui.widget.DanmakuView;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class PlayVideoActivity extends AppCompatActivity implements View.OnClickListener {//
    private ActivityPlayVideoBinding binding;
    private SharedPreferences playSet;
    private final IjkMediaPlayer ijkMediaPlayer = new IjkMediaPlayer();
    private SQLiteOpenHelper sqLiteOpenHelper;
    private final Handler handler = new Handler();
    private BroadcastReceiver bluetoothStateReceiver;
    private float currentSpeed;
    private ListPopupWindow popupWindow;
    private String videoName, videoPath;
    private DanmakuContext danmakuContext;
    private AudioManager audioManager;
    private SurfaceView surfaceView;
    private TextureView textureView;
    private DanmakuView danmakuView;
    private long progress = 0, duration, change, lastClickTime = 0, currentVolume;
    Runnable lockSetInvisible = () -> runOnUiThread(() -> binding.pvImLc.setVisibility(View.INVISIBLE));
    private int secondLockTouchMode = 3, horizonMode = 0, verticalMode = 1, screenWidth, screenHeight,leftThird, rightThird, currentPos, lockMode = 0;
    ;
    private boolean canSecondLockChange = true, secondLockChanged = false, firstPlay = true, canPlay = true, choose_suf, horizon = false, tapScale = false, canRestart = true;
    private boolean playDanmaku = false, danmakuPrepared = false, danmakuPlayed = false, danmakuReset = false;
    private float[] videoInfoInSQL;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlayVideoBinding.inflate(getLayoutInflater());
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//沉浸播放
        setContentView(binding.getRoot());
        initScreenInfo();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        playSet = getSharedPreferences("play_set", MODE_PRIVATE);
        currentSpeed = playSet.getFloat("speed", 1.00f);
        choose_suf = playSet.getBoolean("view", true);
        try {
            initVideoInfo();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        binding.pvCl.initScale(binding.pvFl, videoInfoInSQL[1]);//初始化缩放View
        initIjk();
        initBluetooth();
        playDanmaku = getIntent().getStringExtra("danmaku") != null;
        if (playDanmaku) initDanmaku();
        else binding.pvImDan.setVisibility(View.INVISIBLE);
        initOthers();
        startService(new Intent(this, PostService.class));//上传数据
    }


    @Override
    protected void onPause() {
        super.onPause();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        if (!playSet.getBoolean("background", false)) {
            ijkMediaPlayer.pause();
            binding.pvImPause.setImageResource(R.drawable.ic_play);
            if (playDanmaku) danmakuView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.postDelayed(setINVISIBLE, 2000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (playDanmaku) danmakuView.release();
        int progress = (int) ijkMediaPlayer.getCurrentPosition();
        if (progress >= duration - 3000) {
            progress = 0;
        }
        SharedPreferences.Editor editor = playSet.edit();
        editor.putFloat("speed", currentSpeed);
        editor.putBoolean("horizon", horizon);
        editor.apply();
        unregisterReceiver(bluetoothStateReceiver);
        sqLiteOpenHelper.saveVideoProgress(videoPath, progress, binding.pvCl.getScale());
        handler.removeCallbacksAndMessages(null);
        ijkMediaPlayer.release();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && playSet.getBoolean("watch_back", false)) {
            return true;
        } else
            setResult(Activity.RESULT_OK, new Intent().putExtra("progress",(int) ijkMediaPlayer.getCurrentPosition()));
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onConfigurationChanged(@NonNull @NotNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE || newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            initScreenInfo();
            adjustPlayView(ijkMediaPlayer.getVideoWidth(), ijkMediaPlayer.getVideoHeight());
        }
    }

    private void adjustAudio(int way) {
        if (way == 1) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, 0);
        } else if (way == 2) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, 0);
        } else
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,way,0);
    }

    private void initBluetooth() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        // 创建 BroadcastReceiver 的匿名内部类实例
        bluetoothStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                    Toast.makeText(context, "蓝牙断开", Toast.LENGTH_SHORT).show();
                    ijkMediaPlayer.pause();
                    if (playDanmaku) danmakuView.pause();
                    binding.pvImPause.setImageResource(R.drawable.ic_play);
                } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                    Toast.makeText(context, "蓝牙已连接", Toast.LENGTH_SHORT).show();
                    ijkMediaPlayer.start();
                    if (playDanmaku) danmakuView.resume();
                    binding.pvImPause.setImageResource(R.drawable.ic_pause);
                }
            }
        };

        // 注册 BroadcastReceiver
        registerReceiver(bluetoothStateReceiver, filter);
    }



    private void initDanmaku() {
        danmakuView = binding.pvDanmakuView;
        HashMap<Integer, Integer> maxLinesPair = new HashMap<>();
        maxLinesPair.put(BaseDanmaku.TYPE_SCROLL_RL, (Integer) getDanmakuSet("danmakuLines") + 1);
        danmakuContext = DanmakuContext.create();
        danmakuContext.setDuplicateMergingEnabled(getDanmakuSet("same"))//弹幕重复
                .setFTDanmakuVisibility(getDanmakuSet("style"))//居中弹幕
                .setScrollSpeedFactor(getDanmakuSet("danmakuSpeed"))
                .setScaleTextSize(getDanmakuSet("danmakuSize"))
                .setMaximumLines(maxLinesPair) //设置最大显示行数
                .setDanmakuStyle(IDisplayer.DANMAKU_STYLE_SHADOW, 1)
                .preventOverlapping(getDanmakuSet("fold")); //设置防弹幕重叠，null为允许重叠
        String danmakuUrl = getIntent().getStringExtra("danmaku");
        if (danmakuUrl.startsWith("http") || danmakuUrl.startsWith("https")) {
            new DownloadEvent(danmakuUrl, this, true, () -> danmakuView.prepare(createParser(getExternalFilesDir("danmaku").getAbsolutePath() + "/danmaku.xml"), danmakuContext));
        } else danmakuView.prepare(createParser(danmakuUrl), danmakuContext);
        danmakuView.enableDanmakuDrawingCache(true);
        danmakuView.setCallback(new DrawHandler.Callback() {
            @Override
            public void prepared() {
                danmakuPrepared = true;
                if (!danmakuPlayed && ijkMediaPlayer.isPlaying()) {
                    danmakuView.start(progress);
                    danmakuPlayed = true;
                    if (!playSet.getBoolean("hd_dan", false)) {
                        binding.pvImDan.setImageResource(R.drawable.ic_danmaku_green);
                    } else
                        danmakuView.hide();
                }
            }

            @Override
            public void updateTimer(DanmakuTimer timer) {

            }

            @Override
            public void danmakuShown(BaseDanmaku danmaku) {

            }

            @Override
            public void drawingFinished() {
            }
        });
        binding.pvImDan.setOnClickListener(this);
    }

    private BaseDanmakuParser createParser(String stream) {
        ILoader loader = DanmakuLoaderFactory.create(DanmakuLoaderFactory.TAG_BILI);
        loader.load(stream);
        BaseDanmakuParser parser = new BiliDanmukuParser();
        IDataSource<?> dataSource = loader.getDataSource();
        parser.load(dataSource);
        return parser;
    }

    private <T> T getDanmakuSet(String SPId) {
        SharedPreferences sharedPreferences = getSharedPreferences("danmaku_set", MODE_PRIVATE);
        if (SPId.equals("style")) {
            return (T) (Boolean) sharedPreferences.getBoolean("style", true);
        } else if (SPId.equals("fold")) {
            if (sharedPreferences.getBoolean("fold", false)) {
                HashMap<Integer, Boolean> overlappingEnablePair = new HashMap<Integer, Boolean>();
                overlappingEnablePair.put(BaseDanmaku.TYPE_SCROLL_LR, true);
                overlappingEnablePair.put(BaseDanmaku.TYPE_FIX_BOTTOM, true);
                return (T) (Map) overlappingEnablePair;
            } else return (T) (Map) null;
        } else if (SPId.equals("same")) {
            return (T) (Boolean) sharedPreferences.getBoolean("merge", false);
        } else if (SPId.equals("danmakuSize")) {
            return (T) (Float) ((float) (sharedPreferences.getInt(SPId, 50) + 40) / 100.0f);
        } else if (SPId.equals("danmakuSpeed")) {
            int value = sharedPreferences.getInt(SPId, 1);
            if (value == 0) {
                return (T) (Float) 2.83f;
            } else if (value == 1) {
                return (T) (Float) 2f;
            } else if (value == 2) {
                return (T) (Float) 0.8f;
            }
        } else if (SPId.equals("danmakuLines")) {
            return (T) (Integer) sharedPreferences.getInt(SPId, 2);
        }
        throw new IllegalArgumentException("Invalid item: " + SPId);
    }

    private void initOthers() {
        tapScale = playSet.getBoolean("tap_scale", false);
        initCl();
        initPoWin(binding.pvTvSpeed);
        binding.pvTvSpeed.setOnClickListener(this);
        binding.pvBack.setOnClickListener(this);
        binding.pvTvLen1.setOnClickListener(this);
        binding.pvImPause.setOnClickListener(this);
        binding.pvTvSpeed.setOnClickListener(this);
        binding.pvTvLen0.setOnClickListener(this);
        binding.pvImLc.setOnClickListener(this);
        binding.pvTvLen0.setOnLongClickListener(view -> {
            toBackSeconds();
            return true;
        });
        binding.pvTvLen1.setOnLongClickListener(view -> {
            toNextSeconds();
            return true;
        });
        if (playSet.getBoolean("dark", false))
            binding.pvFlDark.setBackgroundColor(Color.argb(playSet.getInt("dark_pg", 125), 0, 0, 0));
    }

    @SuppressLint("SetTextI18n")
    private void initPoWin(TextView tv) {
        String[] items = {"0.50", "1.00", "1.25", "1.50", "2.00"};
        popupWindow = new ListPopupWindow(this);
        popupWindow.setAdapter(new ArrayAdapter<>(this, R.layout.basic_text_list, items));
        popupWindow.setWidth(100);
        popupWindow.setHeight(200);
        popupWindow.setAnchorView(tv);
        popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.ba_round_grey_video));
        popupWindow.setOnItemClickListener((parent, view, position, id) -> {
            String speed = items[position];
            if (speed.startsWith("1.0")) {
                tv.setText("倍速");
            } else tv.setText(speed + "X");
            currentSpeed = Float.parseFloat(speed);
            ijkMediaPlayer.setSpeed(currentSpeed);
            DrawHandler.setSpeed(currentSpeed);
            popupWindow.dismiss();
        });
    }

    private void initSbPbTimeTv() {
        binding.pvSb.setMax((int) duration);
        binding.pvPb.setMax((int) duration);
        int totalSeconds = (int) (duration / 1000);
        int totalMinutes = totalSeconds / 60;
        int totalSecondsRemainder = totalSeconds % 60;
        @SuppressLint("DefaultLocale") String totalTime = String.format("%02d:%02d", totalMinutes, totalSecondsRemainder);
        binding.pvTvLen1.setText(totalTime);
        binding.pvSb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            long pro;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    ijkMediaPlayer.seekTo(progress);
                    pro = progress;
                }
                binding.pvPb.setProgress(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                ijkMediaPlayer.pause();
                if (playDanmaku) {
                    danmakuView.pause();
                    danmakuView.clear();
                }
                handler.removeCallbacks(setINVISIBLE);
                handler.removeCallbacks(setINVISIBLEExceptLock);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                handler.postDelayed(setINVISIBLE, 2000);
                if (canPlay) {
                    ijkMediaPlayer.start();
                    if (playDanmaku)
                        danmakuView.seekTo(pro);
                } else if (playDanmaku) {
                    danmakuReset = true;
                }
            }
        });
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                int currentPosition = (int) ijkMediaPlayer.getCurrentPosition();
                int currentSeconds = currentPosition / 1000;
                int minutes = currentSeconds / 60;
                int seconds = currentSeconds % 60;
                @SuppressLint("DefaultLocale") String currentTime = String.format("%02d:%02d", minutes, seconds);
                runOnUiThread(() -> {
                    binding.pvTvLen0.setText(currentTime);
                    binding.pvSb.setProgress((int) ijkMediaPlayer.getCurrentPosition());
                });
                handler.postDelayed(this, 800);
            }
        };
        handler.postDelayed(runnable, 800);
    }

    private final Runnable setINVISIBLE = new Runnable() {
        @Override
        public void run() {
            binding.pvVg.setVisibility(View.INVISIBLE);
            binding.pvPb.setVisibility(View.VISIBLE);
            binding.pvImLc.setVisibility(View.INVISIBLE);
            binding.pvImDan.setVisibility(View.INVISIBLE);
        }
    };

    private final Runnable setINVISIBLEExceptLock = new Runnable() {
        @Override
        public void run() {
            binding.pvVg.setVisibility(View.INVISIBLE);
            binding.pvPb.setVisibility(View.VISIBLE);
            binding.pvImDan.setVisibility(View.INVISIBLE);
        }
    };
    @SuppressLint("ClickableViewAccessibility")
    private void initCl() {
        final int[] clickCount = {0};
        final boolean[] speedACan = {false, true};
        final float[] point = {0, 0};
        ConstraintLayout cl = binding.pvCl;
        cl.setSoundEffectsEnabled(false);
        cl.setOnClickListener(v -> {
            handler.removeCallbacks(setINVISIBLE);
            handler.removeCallbacks(setINVISIBLEExceptLock);
            if (lockMode != 2) {
                if (speedACan[1]) clickCount[0]++;
                handler.postDelayed(() -> {
                    if (clickCount[0] == 1 && speedACan[1] && !secondLockChanged) {
                        if (binding.pvVg.getVisibility() == View.VISIBLE) {
                            handler.postDelayed(setINVISIBLE, 0);
                        } else {
                            handler.postDelayed(setINVISIBLE, 2000);
                            if (playDanmaku) binding.pvImDan.setVisibility(View.VISIBLE);
                            binding.pvVg.setVisibility(View.VISIBLE);
                            binding.pvImLc.setVisibility(View.VISIBLE);
                            binding.pvPb.setVisibility(View.INVISIBLE);
                        }
                    } else if (speedACan[1] && clickCount[0] == 2 && !tapScale) {
                        float x = point[0];
                        // 根据点击位置判断区域
                        if (x < leftThird) {
                            // 屏幕左侧
                            toBackSeconds();
                        } else if (x >= leftThird && x < rightThird) {
                            // 屏幕中间
                            if (ijkMediaPlayer.isPlaying()) {
                                ijkMediaPlayer.pause();
                                canPlay = false;
                                if (playDanmaku) {
                                    danmakuView.pause();
                                    binding.pvImDan.setVisibility(View.VISIBLE);
                                }
                                binding.pvImPause.setImageResource(R.drawable.ic_play);
                                binding.pvVg.setVisibility(View.VISIBLE);
                                binding.pvImLc.setVisibility(View.VISIBLE);
                                binding.pvPb.setVisibility(View.INVISIBLE);
                                binding.pvTv0.setText("暂停播放");
                            } else {
                                ijkMediaPlayer.start();
                                canPlay = true;
                                if (playDanmaku && danmakuReset) {
                                    danmakuView.seekTo((long)binding.pvSb.getProgress());
                                    danmakuReset = false;
                                }else if (playDanmaku) danmakuView.resume();
                                binding.pvImPause.setImageResource(R.drawable.ic_pause);
                                handler.postDelayed(setINVISIBLE, 0);
                                binding.pvTv0.setText("继续播放");
                            }
                            binding.pvTv0.setVisibility(View.VISIBLE);
                            handler.postDelayed(() -> runOnUiThread(() -> binding.pvTv0.setVisibility(View.INVISIBLE)), 1000);
                        } else {
                            // 屏幕右侧
                            toNextSeconds();
                        }

                    }
                    clickCount[0] = 0;
                }, 330);
            } else {
                handler.removeCallbacks(lockSetInvisible);
                if (binding.pvImLc.getVisibility() != View.VISIBLE) {
                    binding.pvImLc.setVisibility(View.VISIBLE);
                    handler.postDelayed(lockSetInvisible, 2000);
                } else binding.pvImLc.setVisibility(View.INVISIBLE);
            }
        });

        cl.setOnLongClickListener(v -> {
            cl.setHapticFeedbackEnabled(false);
            if (canRestart && progress > 0 && speedACan[1]) {
                ijkMediaPlayer.seekTo(0);
                if (playDanmaku) danmakuView.seekTo(0L);
                cl.setHapticFeedbackEnabled(true);
            } else if (speedACan[1] && !secondLockChanged) {
                binding.pvTv0.setText("倍速播放中");
                binding.pvTv0.setVisibility(View.VISIBLE);
                ijkMediaPlayer.setSpeed(2);
                DrawHandler.setSpeed(2f);
                speedACan[0] = true;
                cl.setHapticFeedbackEnabled(true);
            }
            return true;
        });
        long maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        final boolean backGesture = playSet.getBoolean("back_gesture", true);
        cl.setOnTouchListener((v, event) -> {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    if (lockMode == 1) currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    if (tapScale && System.currentTimeMillis() - lastClickTime <= 320) {
                        canSecondLockChange = false;
                    } else if (tapScale) lastClickTime = System.currentTimeMillis();
                    speedACan[1] = true;
                    point[0] = event.getX();
                    point[1] = event.getY();
                    if (lockMode == 1) currentPos = (int)ijkMediaPlayer.getCurrentPosition();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float offsetX = event.getX() - point[0]; // X 轴上的移动距离
                    float offsetY = event.getY() - point[1]; // Y 轴上的移动距离
                    if (backGesture && point[0] < 25 && event.getX() - point[0] > 80) {
                        setResult(Activity.RESULT_OK, new Intent().putExtra("progress",(int) ijkMediaPlayer.getCurrentPosition()));
                        finish();
                    }
                    else if (offsetX > 3.5 || offsetX < -3.5 || offsetY > 3.5 || offsetY < -3.5) {
                        handler.postDelayed(setINVISIBLE, 0);
                        if (lockMode == 1 && speedACan[1] && point[0] >= 25 && canSecondLockChange && !speedACan[0]) {
                            if (offsetX > 50 && secondLockTouchMode != verticalMode) {
                                secondLockTouchMode = horizonMode;
                                int gap = (int) (((offsetX - 50) / 400) * 23000);
                                int percent = (int) ((currentPos + gap) * 100 / duration);
                                if (percent > 100) {
                                    percent = 100;
                                    gap = (int) (duration - currentPos);
                                }
                                secondLockChanged = true;
                                toast("快进" + gap / 1000 + "S（" + percent + "%）");
                                change = currentPos + gap;
                            } else if (offsetX < -50 && secondLockTouchMode != verticalMode) {
                                secondLockTouchMode = horizonMode;
                                int gap = (int) (((offsetX + 50) / 400) * 23000);
                                int percent = (int) ((currentPos + gap) * 100 / duration);
                                if (percent < 0) {
                                    percent = 0;
                                    gap = -currentPos;
                                }
                                secondLockChanged = true;
                                toast("快退" + Math.abs(gap) / 1000 + "S（" + percent + "%）");
                                change = currentPos + gap;
                            } else if (offsetY < -50 && secondLockTouchMode != horizonMode) {
                                secondLockTouchMode = verticalMode;
                                secondLockChanged = true;
                                int gap = (int) (((-offsetY -50) / 400) * maxVolume + currentVolume);
                                int percent = (int) (gap * 100 / maxVolume);
                                if (percent > 100) {
                                    percent = 100;
                                }
                                toast("音量加（" + percent + "%）");
                                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,gap,0);
                            } else if (offsetY > 50 && secondLockTouchMode != horizonMode) {
                                secondLockTouchMode = verticalMode;
                                secondLockChanged = true;
                                int gap = (int) (currentVolume - ((offsetY - 50) / 400) * maxVolume);
                                int percent = (int) (gap * 100 / maxVolume);
                                if (percent < 0) {
                                    percent = 0;
                                }
                                toast("音量减（" + percent + "%）");
                                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,gap,0);
                            } else if (secondLockChanged) {
                                secondLockTouchMode = 3;
                                toast("取消操作");
                                change = currentPos;
                            }
                            break;
                        }
                        speedACan[1] = false;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (tapScale) canSecondLockChange = true;
                    if (lockMode == 1 && secondLockChanged) {
                        secondLockChanged = false;
                        speedACan[1] = false;
                        if (secondLockTouchMode == horizonMode) {
                            ijkMediaPlayer.seekTo(change);
                            if (playDanmaku && canPlay) {
                                danmakuView.seekTo((change));
                            } else if (playDanmaku) {
                                danmakuReset = true;
                            }
                        }
                        binding.pvTv0.setVisibility(View.INVISIBLE);
                        secondLockTouchMode = 3;
                    }
                    if (speedACan[0]) {
                        binding.pvTv0.setVisibility(View.INVISIBLE);
                        ijkMediaPlayer.setSpeed(currentSpeed);
                        speedACan[0] = false;
                        DrawHandler.setSpeed(currentSpeed);
                    }
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    speedACan[1] = false;
                    break;
            }
            return super.onTouchEvent(event);
        });
    }

    private void toNextSeconds() {
        long pos = ijkMediaPlayer.getCurrentPosition() + 10000;
        if (playDanmaku && canPlay) {
            danmakuView.seekTo(pos);
        } else if (playDanmaku) {
            danmakuView.clear();
            danmakuReset = true;
        }
        ijkMediaPlayer.seekTo(pos);
        toast("快进10S");
    }

    private void toBackSeconds() {
        long pos = ijkMediaPlayer.getCurrentPosition() - 10000;
        if (playDanmaku && canPlay) {
            danmakuView.seekTo(pos);
        } else if (playDanmaku) {
            danmakuView.clear();
            danmakuReset = true;
        }
        ijkMediaPlayer.seekTo(pos);
        toast("快退10S");
    }

    private void toast(String info) {
        binding.pvTv0.setText(info);
        binding.pvTv0.setVisibility(View.VISIBLE);
        handler.removeCallbacks(setToastInvisible);
        handler.postDelayed(setToastInvisible, 1000);
    }

    Runnable setToastInvisible = new Runnable() {
        @Override
        public void run() {
            runOnUiThread(() -> binding.pvTv0.setVisibility(View.INVISIBLE));
        }
    };
    private void setVideoPath(Uri uri) throws IOException {
        videoName = getIntent().getStringExtra("name");
        videoPath = getIntent().getStringExtra("path");
        if (uri != null) {
            if (videoName != null) {
                videoPath = videoName;
            } else if (videoPath != null) {
                videoName = videoPath;
            } else videoName = videoPath = String.valueOf(uri);
            Map<String, String> headers = (Map<String, String>) getIntent().getSerializableExtra("cookie");
            if (headers != null) {
                ijkMediaPlayer.setDataSource(this, uri, headers);
            } else ijkMediaPlayer.setDataSource(this, uri);
            ijkMediaPlayer.prepareAsync();
        } else if (videoPath != null) {
            ijkMediaPlayer.setDataSource(videoPath);
            ijkMediaPlayer.prepareAsync();
        } else {
            Toast.makeText(this, "未找到该视频", Toast.LENGTH_SHORT).show();
            setResult(Activity.RESULT_CANCELED, new Intent().putExtra("progress",(int) ijkMediaPlayer.getCurrentPosition()));
            finish();
        }
    }

    private void initIjk() {
        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "user_agent", getIntent().getStringExtra("agent"));
        if (playSet.getBoolean("jump_play", true))
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 3);
        if (playSet.getBoolean("hard_play", true))
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
        else ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 0);
        if (!playSet.getBoolean("audio", true))
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 1);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 1);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_clear", 1); //dns 清理
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "fflags", "flush_packets");
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "reconnect", 1);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0);
        if (playSet.getBoolean("sharp", false))
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "enable-accurate-seek", 1);
        ijkMediaPlayer.setKeepInBackground(true);
        binding.pvTvTitle.setText(videoName);
        ijkMediaPlayer.setOnPreparedListener(iMediaPlayer -> {
            ijkMediaPlayer.setSpeed(currentSpeed);
            if(currentSpeed != 1.00) {
                String speed = currentSpeed+"X";
                toast(speed);
                binding.pvTvSpeed.setText(speed);
            }
            duration = ijkMediaPlayer.getDuration();
            if (playSet.getBoolean("none_start", false)) adjustAudio(0);
            float volume = playSet.getFloat("volume", 100) / 100;
            ijkMediaPlayer.setVolume(volume, volume);
            initPlayView(ijkMediaPlayer.getVideoWidth(), ijkMediaPlayer.getVideoHeight());
            initSbPbTimeTv();
            if (getIntent().getIntExtra("progress", 0) == 0) {
                progress = (long) videoInfoInSQL[0];
            } else progress = getIntent().getIntExtra("progress", 0);
            if (progress > 0) {
                ijkMediaPlayer.seekTo(progress);
                Toast.makeText(this, "3S内长按重播", Toast.LENGTH_SHORT).show();
            }
            handler.postDelayed(() -> canRestart = false, 3000);
            if (playSet.getInt("lock_mode", 0) == 1) {
                binding.pvImLc.setImageResource(R.drawable.ic_second_lock);
                lockMode = 1;
                binding.pvCl.canTrans(false);
            }
            if (!danmakuPlayed && danmakuPrepared) {
                danmakuView.start(progress);
                danmakuPlayed = true;
                if (!playSet.getBoolean("hd_dan", false)) {
                    binding.pvImDan.setImageResource(R.drawable.ic_danmaku_green);
                } else
                    danmakuView.hide();
            }
            binding.pvImRotate.setOnClickListener(this);
        });
        ijkMediaPlayer.setOnErrorListener((iMediaPlayer, i, i1) -> {
            Toast.makeText(PlayVideoActivity.this, "播放错误", Toast.LENGTH_SHORT).show();
            setResult(Activity.RESULT_CANCELED, new Intent().putExtra("progress",(int) ijkMediaPlayer.getCurrentPosition()));
            finish();
            return true;
        });
        ijkMediaPlayer.setOnBufferingUpdateListener((iMediaPlayer, i) -> {
            int buff = (int) (i * duration / 100);
            binding.pvPb.setSecondaryProgress(buff);
            binding.pvSb.setSecondaryProgress(buff);
        });
        String dataSource = ijkMediaPlayer.getDataSource();
        if (dataSource == null) dataSource = "";
        if (dataSource.startsWith("http")) {
            ijkMediaPlayer.setOnInfoListener((mp, what, extra) -> {
                if (what == IMediaPlayer.MEDIA_INFO_BUFFERING_START) {
                    if (playDanmaku) danmakuView.pause();
                    handler.postDelayed(updateSpeedRunnable, 0); // 启动定时器
                    binding.pvPbBuff.setVisibility(View.VISIBLE);
                    binding.pvTvBuff.setVisibility(View.VISIBLE);
                } else if (what == IMediaPlayer.MEDIA_INFO_BUFFERING_END || what == IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                    binding.pvTvBuff.setVisibility(View.GONE);
                    binding.pvPbBuff.setVisibility(View.GONE);
                    handler.removeCallbacks(updateSpeedRunnable);
                    if (playDanmaku && canPlay) danmakuView.resume();
                }
                return false;
            });
        } else {
            binding.pvTvBuff.setVisibility(View.GONE);
            binding.pvPbBuff.setVisibility(View.GONE);
        }
        ijkMediaPlayer.setOnCompletionListener(iMediaPlayer -> {
            if (playSet.getBoolean("restart", false)) {
                Toast.makeText(PlayVideoActivity.this, "循环播放", Toast.LENGTH_SHORT).show();
                ijkMediaPlayer.start();
                if (playDanmaku) danmakuView.start(0);
            } else if (playSet.getBoolean("back_finish", true)) {
                setResult(Activity.RESULT_OK, new Intent().putExtra("progress",(int) ijkMediaPlayer.getCurrentPosition()));
                Toast.makeText(PlayVideoActivity.this, "播放结束", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    ViewGroup.LayoutParams params;
    private void adjustPlayView(int videoWith, int videoHeight) {
        if (choose_suf) params = surfaceView.getLayoutParams();
        else params = textureView.getLayoutParams();
        if (screenWidth / screenHeight <= videoWith / videoHeight) {
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.height = (int) ((float) videoHeight / videoWith * screenWidth);
        } else{
            params.width = (int) ((float) screenHeight * videoWith / videoHeight);
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        }
        if (choose_suf) surfaceView.setLayoutParams(params);
        else textureView.setLayoutParams(params);
    }

    private void initVideoInfo() throws IOException {
        setVideoPath(getIntent().getData());
        sqLiteOpenHelper = new SQLiteOpenHelper(this);//初始化数据库
        videoInfoInSQL = sqLiteOpenHelper.getVideoInfo(videoPath);
    }
    private void initPlayView(int videoWith, int videoHeight) {
        ViewGroup.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER);
        if (choose_suf) {
            surfaceView = new SurfaceView(this);
            binding.pvFl.addView(surfaceView,params);
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            surfaceHolder.addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    if (playSet.getBoolean("horizon", false)) {
                        if (playSet.getBoolean("un_horizon",false)) {
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                        }
                        else setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        horizon = true;
                    }
                    if (!playSet.getBoolean("stuff",false)) adjustPlayView(videoWith, videoHeight);
                    ijkMediaPlayer.setDisplay(holder);
                    if (firstPlay) {
                        firstPlay = false;
                        ijkMediaPlayer.start();
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    ijkMediaPlayer.setDisplay(null);
                }
            });
        } else {
            textureView = new TextureView(this);
            binding.pvFl.addView(textureView,params);
            textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
                    if (playSet.getBoolean("horizon", false)) {
                        if (playSet.getBoolean("un_horizon",false)) {
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                        }
                        else setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        horizon = true;
                    }
                    if (!playSet.getBoolean("stuff",false)) adjustPlayView(videoWith, videoHeight);
                    ijkMediaPlayer.setSurface(new Surface(surface));
                    if (firstPlay) {
                        firstPlay = false;
                        ijkMediaPlayer.start();
                    }
                }

                @Override
                public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
                }

                @Override
                public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                    ijkMediaPlayer.setSurface(null);
                    return false;
                }

                @Override
                public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
                }
            });
        }
    }

    private Runnable updateSpeedRunnable = new Runnable() {
        @Override
        public void run() {
            binding.pvTvBuff.setText(String.format("%.2f KB/s", ijkMediaPlayer.getTcpSpeed() / 1024.0));
            handler.postDelayed(this, 500);
        }
    };

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.pv_back) {
            setResult(Activity.RESULT_OK, new Intent().putExtra("progress",(int) ijkMediaPlayer.getCurrentPosition()));
            finish();
        } else if (id == R.id.pv_tv_len0) {
            handler.removeCallbacks(setINVISIBLE);
            handler.removeCallbacks(setINVISIBLEExceptLock);
            handler.postDelayed(setINVISIBLE, 2000);
            adjustAudio(2);
            toast("音量减");
        } else if (id == R.id.pv_tv_len1) {
            handler.removeCallbacks(setINVISIBLE);
            handler.removeCallbacks(setINVISIBLEExceptLock);
            handler.postDelayed(setINVISIBLE, 2000);
            adjustAudio(1);
            toast("音量加");
        } else if (id == R.id.pv_im_pause) {
            if (ijkMediaPlayer.isPlaying()) {
                ijkMediaPlayer.pause();
                canPlay = false;
                if (playDanmaku) danmakuView.pause();
                binding.pvImPause.setImageResource(R.drawable.ic_play);
            } else {
                ijkMediaPlayer.start();
                canPlay = true;
                if (playDanmaku && danmakuReset) {
                    danmakuView.seekTo((long)binding.pvSb.getProgress());
                    danmakuReset = false;
                }else if (playDanmaku) danmakuView.resume();
                binding.pvImPause.setImageResource(R.drawable.ic_pause);
            }
        } else if (id == R.id.pv_tv_speed) {
            popupWindow.show();
        } else if (id == R.id.pv_im_rotate) {
            if (horizon) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                horizon = false;
            } else {
                if (playSet.getBoolean("un_horizon",false)) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                } else setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                horizon = true;
            }
        } else if (id == R.id.pv_im_dan) {
            if (danmakuView.isShown()) {
                binding.pvImDan.setImageResource(R.drawable.ic_danmaku);
                danmakuView.hide();
                playSet.edit().putBoolean("hd_dan", true).apply();
            } else {
                binding.pvImDan.setImageResource(R.drawable.ic_danmaku_green);
                danmakuView.show();
                playSet.edit().putBoolean("hd_dan", false).apply();
            }
        } else if (id == R.id.pv_im_lc) {
            handler.removeCallbacks(lockSetInvisible);
            if (lockMode == 2) {
                lockMode = 0;
                secondLockChanged = false;
                binding.pvCl.canScale(true);
                binding.pvCl.canTrans(true);
                if (playDanmaku) binding.pvImDan.setVisibility(View.VISIBLE);
                binding.pvVg.setVisibility(View.VISIBLE);
                binding.pvImLc.setVisibility(View.VISIBLE);
                binding.pvPb.setVisibility(View.INVISIBLE);
                binding.pvImLc.setImageResource(R.drawable.ic_unlock);
                handler.postDelayed(setINVISIBLEExceptLock, 2000);
                handler.postDelayed(lockSetInvisible, 2000);
                playSet.edit().putInt("lock_mode", 0).apply();
            } else if (lockMode == 1){
                lockMode = 2;
                secondLockChanged = false;
                binding.pvVg.setVisibility(View.INVISIBLE);
                binding.pvPb.setVisibility(View.VISIBLE);
                binding.pvImDan.setVisibility(View.INVISIBLE);
                binding.pvCl.canScale(false);
                playSet.edit().putInt("lock_mode", 2).apply();
                binding.pvImLc.setImageResource(R.drawable.ic_lock);
                handler.postDelayed(lockSetInvisible, 2000);
            } else {
                lockMode = 1;
                binding.pvCl.canScale(true);
                binding.pvCl.canTrans(false);
                playSet.edit().putInt("lock_mode", 1).apply();
                binding.pvImLc.setImageResource(R.drawable.ic_second_lock);
                handler.postDelayed(lockSetInvisible, 2000);
            }
        }
    }
    private void initScreenInfo() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;
        leftThird = screenWidth / 4;
        rightThird = screenWidth * 3 / 4;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                @SuppressLint("PrivateApi") Class clsPkgParser = Class.forName("android.content.pm.PackageParser$Package");
                Constructor constructor = clsPkgParser.getDeclaredConstructor(String.class);
                constructor.setAccessible(true);

                @SuppressLint("PrivateApi") Class clsActivityThread = Class.forName("android.app.ActivityThread");
                Method method = clsActivityThread.getDeclaredMethod("currentActivityThread");
                method.setAccessible(true);
                Object activityThread = method.invoke(null);
                Field hiddenApiWarning = clsActivityThread.getDeclaredField("mHiddenApiWarningShown");
                hiddenApiWarning.setAccessible(true);
                hiddenApiWarning.setBoolean(activityThread, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        SharedPreferences SharedPreferencesUtil = newBase.getSharedPreferences("display", MODE_PRIVATE);
        float dpiTimes = SharedPreferencesUtil.getFloat("dpi", 1.0F);
        if (dpiTimes == 1.0F) super.attachBaseContext(newBase);
        else try {
            DisplayMetrics displayMetrics = newBase.getResources().getDisplayMetrics();
            Configuration configuration = newBase.getResources().getConfiguration();
            configuration.densityDpi = (int) (displayMetrics.densityDpi * dpiTimes);
            super.attachBaseContext(newBase.createConfigurationContext(configuration));
        } catch (Exception e) {
            super.attachBaseContext(newBase);
        }
    }
}