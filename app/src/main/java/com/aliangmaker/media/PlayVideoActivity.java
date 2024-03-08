package com.aliangmaker.media;

import android.annotation.SuppressLint;
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
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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

import java.io.IOException;
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
import org.jetbrains.annotations.NotNull;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class PlayVideoActivity extends AppCompatActivity implements View.OnClickListener {
    private ActivityPlayVideoBinding binding;
    private SharedPreferences playSet;
    private final IjkMediaPlayer ijkMediaPlayer = new IjkMediaPlayer();
    private SQLiteOpenHelper sqLiteOpenHelper;
    private final Handler handler = new Handler();
    private BroadcastReceiver bluetoothStateReceiver;
    private float currentSpeed = 1;
    private ListPopupWindow popupWindow;
    private String videoName, videoPath;
    private DanmakuContext danmakuContext;
    private AudioManager audioManager;
    private SurfaceView surfaceView;
    private TextureView textureView;
    
    private DanmakuView danmakuView;
    private int CVolume;
    private boolean canPlayDanmaku = false, danmakuPlayed = false, horizon = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlayVideoBinding.inflate(getLayoutInflater());
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//沉浸播放
        setContentView(binding.getRoot());
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        CVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        playSet = getSharedPreferences("play_set", MODE_PRIVATE);
        sqLiteOpenHelper = new SQLiteOpenHelper(this);//初始化数据库
        binding.pvCl.initScale(binding.pvFl);//初始化缩放View
        try {
            initIjk();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        initBluetooth();
        if (getIntent().getStringExtra("danmaku") != null) initDanmaku();
        else binding.pvImDanmaku.setVisibility(View.GONE);
        startService(new Intent(this, PostService.class));//上传数据
        initOthersView();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && playSet.getBoolean("watch_back",false)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onConfigurationChanged(@NonNull @NotNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE || newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            adjustPlayView(ijkMediaPlayer.getVideoWidth(), ijkMediaPlayer.getVideoHeight());
        }
    }

    private void adjustAudio(int way) {
        if (way == 1) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, 0);
        } else if (way == 2) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_LOWER, 0);
        } else audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, way, 0);
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
                    if (danmakuView != null) danmakuView.pause();
                    binding.pvImPause.setImageResource(R.drawable.ic_play);
                } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                    Toast.makeText(context, "蓝牙已连接", Toast.LENGTH_SHORT).show();
                    ijkMediaPlayer.start();
                    if (danmakuView != null)danmakuView.resume();
                    binding.pvImPause.setImageResource(R.drawable.ic_pause);
                }
            }
        };

        // 注册 BroadcastReceiver
        registerReceiver(bluetoothStateReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        boolean ct = playSet.getBoolean("background", false);
        if (danmakuView != null && !ct) danmakuView.pause();
        if (!ct) ijkMediaPlayer.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (playSet.getBoolean("horizon", false)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            horizon = true;
        }
        handler.postDelayed(setINVISIBLE, 2000);
        if (danmakuView != null) danmakuView.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (danmakuView != null) danmakuView.release();
        int progress = (int) ijkMediaPlayer.getCurrentPosition();
        if (progress >= ijkMediaPlayer.getDuration() - 3000) {
            progress = 0;
        }
        unregisterReceiver(bluetoothStateReceiver);
        sqLiteOpenHelper.saveVideoProgress(videoPath, progress);
        handler.removeCallbacksAndMessages(null);
        ijkMediaPlayer.release();
        adjustAudio(CVolume);
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
                canPlayDanmaku = true;
                if (!danmakuPlayed && ijkMediaPlayer.isPlaying()) {
                    danmakuView.start(ijkMediaPlayer.getCurrentPosition());
                    danmakuPlayed = true;
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
        binding.pvImDanmaku.setOnClickListener(this);
        if (!playSet.getBoolean("hd_dan", false)) {
            binding.pvImDanmaku.setImageResource(R.drawable.ic_danmaku_green);
        } else danmakuView.hide();
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
            return (T) (Float) ((float) (sharedPreferences.getInt(SPId, 50) + 50) / 100.0f);
        } else if (SPId.equals("danmakuSpeed")) {
            int value = sharedPreferences.getInt(SPId, 1);
            if (value == 0) {
                return (T) (Float) 1.3f;
            } else if (value == 1) {
                return (T) (Float) 1f;
            } else if (value == 2) {
                return (T) (Float) 0.7f;
            }
        } else if (SPId.equals("danmakuLines")) {
            return (T) (Integer) sharedPreferences.getInt(SPId, 2);
        }
        throw new IllegalArgumentException("Invalid item: " + SPId);
    }

    private void initOthersView() {
        initCl();
        initPoWin(binding.pvTvSpeed);
        binding.pvTvSpeed.setOnClickListener(this);
        binding.pvBack.setOnClickListener(this);
        binding.pvTvLen1.setOnClickListener(this);
        binding.pvImPause.setOnClickListener(this);
        binding.pvTvSpeed.setOnClickListener(this);
        binding.pvImRotate.setOnClickListener(this);
        binding.pvTvLen0.setOnClickListener(this);
        binding.pvTvLen0.setOnLongClickListener(view -> {
            handler.removeCallbacks(setINVISIBLE);
            handler.postDelayed(setINVISIBLE, 2000);
            adjustAudio(2);
            return true;
        });
        binding.pvTvLen1.setOnLongClickListener(view -> {
            handler.removeCallbacks(setINVISIBLE);
            handler.postDelayed(setINVISIBLE, 2000);
            adjustAudio(1);
            return true;
        });
        if (playSet.getBoolean("dark", false))
            binding.pvFlDark.setBackgroundColor(Color.argb(playSet.getInt("dark_pg", 0), 0, 0, 0));
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
            tv.setText(speed + "X");
            currentSpeed = Float.parseFloat(speed);
            ijkMediaPlayer.setSpeed(currentSpeed);
            DrawHandler.setSpeed(currentSpeed);
        });
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.pv_back) {
            finish();
        } else if (id == R.id.pv_tv_len0) {
            long pos = ijkMediaPlayer.getCurrentPosition() - 10000;
            ijkMediaPlayer.seekTo(pos);
            if (danmakuView != null)danmakuView.seekTo(pos);
        } else if (id == R.id.pv_tv_len1) {
            long pos = ijkMediaPlayer.getCurrentPosition() + 10000;
            ijkMediaPlayer.seekTo(pos);
            if (danmakuView != null)danmakuView.seekTo(pos);
        } else if (id == R.id.pv_im_pause) {
            if (ijkMediaPlayer.isPlaying()) {
                ijkMediaPlayer.pause();
                if (danmakuView != null)danmakuView.pause();
                binding.pvImPause.setImageResource(R.drawable.ic_play);
            } else {
                ijkMediaPlayer.start();
                if (danmakuView != null)danmakuView.resume();
                binding.pvImPause.setImageResource(R.drawable.ic_pause);
            }
        } else if (id == R.id.pv_tv_speed) {
            popupWindow.show();
        } else if (id == R.id.pv_im_rotate) {
            if (horizon) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                horizon = false;
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                horizon = true;
            }
        } else if (id == R.id.pv_im_danmaku) {
            if (danmakuView.isShown()) {
                binding.pvImDanmaku.setImageResource(R.drawable.ic_danmaku);
                danmakuView.hide();
                playSet.edit().putBoolean("hd_dan",true).apply();
            } else {
                binding.pvImDanmaku.setImageResource(R.drawable.ic_danmaku_green);
                danmakuView.show();
                playSet.edit().putBoolean("hd_dan",false).apply();
            }
        }
    }

    private void initSbPbTimeTv() {
        binding.pvSb.setMax((int) ijkMediaPlayer.getDuration());
        binding.pvPb.setMax((int) ijkMediaPlayer.getDuration());
        int totalSeconds = (int) (ijkMediaPlayer.getDuration() / 1000);
        int totalMinutes = totalSeconds / 60;
        int totalSecondsRemainder = totalSeconds % 60;
        @SuppressLint("DefaultLocale") String totalTime = String.format("%02d:%02d", totalMinutes, totalSecondsRemainder);
        binding.pvTvLen1.setText(totalTime);
        binding.pvSb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            boolean canPlay = true;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (danmakuView != null) danmakuView.seekTo((long) progress);
                    ijkMediaPlayer.seekTo(progress);
                }
                binding.pvPb.setProgress(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (ijkMediaPlayer.isPlaying()) canPlay = true;
                else canPlay = false;
                ijkMediaPlayer.pause();
                if (danmakuView != null)danmakuView.pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (canPlay) {
                    if (danmakuView != null)danmakuView.resume();
                    ijkMediaPlayer.start();
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
            clickCount[0]++;
            handler.postDelayed(() -> {
                if (clickCount[0] == 1 && speedACan[1]) {
                    if (binding.pvVg.getVisibility() == View.VISIBLE) {
                        binding.pvVg.setVisibility(View.INVISIBLE);
                        binding.pvPb.setVisibility(View.VISIBLE);
                    } else {
                        handler.removeCallbacks(setINVISIBLE);
                        handler.postDelayed(setINVISIBLE, 2000);
                        binding.pvVg.setVisibility(View.VISIBLE);
                        binding.pvPb.setVisibility(View.INVISIBLE);
                    }
                } else if (speedACan[1] && clickCount[0] == 2) {
                    if (ijkMediaPlayer.isPlaying()) {
                        ijkMediaPlayer.pause();
                        if (danmakuView != null)danmakuView.pause();
                        binding.pvImPause.setImageResource(R.drawable.ic_play);
                        handler.removeCallbacks(setINVISIBLE);
                        binding.pvVg.setVisibility(View.VISIBLE);
                        binding.pvPb.setVisibility(View.INVISIBLE);
                    } else {
                        ijkMediaPlayer.start();
                        if (danmakuView != null)danmakuView.resume();
                        binding.pvImPause.setImageResource(R.drawable.ic_pause);
                        handler.removeCallbacks(setINVISIBLE);
                        handler.postDelayed(setINVISIBLE, 2000);
                    }
                }
                clickCount[0] = 0;
            }, 320);
        });

        cl.setOnLongClickListener(v -> {
            cl.setHapticFeedbackEnabled(false);
            if (speedACan[1]) {
                binding.pvTv0.setVisibility(View.VISIBLE);
                cl.setHapticFeedbackEnabled(true);
                ijkMediaPlayer.setSpeed(2);
                DrawHandler.setSpeed(2f);
                speedACan[0] = true;
            }
            return true;
        });
        cl.setOnTouchListener((v, event) -> {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    speedACan[1] = true;
                    point[0] = event.getX();
                    point[1] = event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float offsetX = event.getX() - point[0]; // X 轴上的移动距离
                    float offsetY = event.getY() - point[1]; // Y 轴上的移动距离
                    float value = (float) Math.sqrt(offsetX * offsetX + offsetY * offsetY);
                    if (playSet.getBoolean("back_gesture", true) && point[0] < 25 && event.getX() - point[0] > 100)
                        finish();
                    else if (value > 5) speedACan[1] = false;
                    break;
                case MotionEvent.ACTION_UP:
                    if (speedACan[0]) {
                        binding.pvTv0.setVisibility(View.INVISIBLE);
                        ijkMediaPlayer.setSpeed(currentSpeed);
                        speedACan[0] = false;
                        DrawHandler.setSpeed(currentSpeed);
                    }
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    speedACan[1] = false;
            }
            return super.onTouchEvent(event);
        });
    }

    private void setVideoPath(Uri uri) throws IOException {
        videoName = getIntent().getStringExtra("name");
        videoPath = getIntent().getStringExtra("path");
        if (uri != null) {
            if (videoName != null) {
                videoPath = videoName;
            } else if (videoPath != null) {
                videoName = videoPath;
            } else videoName = videoPath = String.valueOf(uri);
            Map<String,String> headers = (Map<String, String>) getIntent().getSerializableExtra("cookie");
            if (headers != null) {
                ijkMediaPlayer.setDataSource(this,uri,headers);
            }else ijkMediaPlayer.setDataSource(this,uri);
            ijkMediaPlayer.prepareAsync();
        } else if (videoPath != null) {
            ijkMediaPlayer.setDataSource(videoPath);
            ijkMediaPlayer.prepareAsync();
        } else {
            Toast.makeText(this, "未找到该视频", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initIjk() throws IOException {
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
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_clear", 1); //dns 清理
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 1);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "fflags", "flush_packets");
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "reconnect", 1);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0);
        if (playSet.getBoolean("sharp", false))
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "enable-accurate-seek", 1);
        ijkMediaPlayer.setKeepInBackground(true);
        setVideoPath(getIntent().getData());
        binding.pvTvTitle.setText(videoName);
        ijkMediaPlayer.setOnPreparedListener(iMediaPlayer -> {
            if (playSet.getBoolean("none_start", false)) adjustAudio(0);
            float volume = playSet.getFloat("volume", 100) / 100;
            ijkMediaPlayer.setVolume(volume, volume);
            initPlayView(ijkMediaPlayer.getVideoWidth(), ijkMediaPlayer.getVideoHeight());
            initSbPbTimeTv();
            long progress;
            if (getIntent().getIntExtra("progress", 0) == 0) {
                progress = sqLiteOpenHelper.getVideoProgress(videoPath);
            } else progress = getIntent().getIntExtra("progress", 0);
            if (progress > 0) {
                ijkMediaPlayer.seekTo(progress);
                Toast.makeText(this, "继续上一次播放", Toast.LENGTH_SHORT).show();
            }
            if (!danmakuPlayed && canPlayDanmaku) {
                danmakuView.start(progress);
                danmakuPlayed = true;
            }
        });
        ijkMediaPlayer.setOnErrorListener((iMediaPlayer, i, i1) -> {
            Toast.makeText(PlayVideoActivity.this, "播放错误", Toast.LENGTH_SHORT).show();
            finish();
            return false;
        });
        ijkMediaPlayer.setOnCompletionListener(iMediaPlayer -> {
            if (playSet.getBoolean("restart", false)) {
                Toast.makeText(PlayVideoActivity.this, "循环播放", Toast.LENGTH_SHORT).show();
                ijkMediaPlayer.start();
                if (danmakuView != null)danmakuView.start(0);
            } else if (playSet.getBoolean("back_finish", true)) {
                Toast.makeText(PlayVideoActivity.this, "播放结束", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void adjustPlayView(int videoWith, int videoHeight) {
        ViewGroup.LayoutParams params;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        if (videoWith >= videoHeight)
            params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) ((float) videoHeight / videoWith * displayMetrics.widthPixels), Gravity.CENTER);
        else
            params = new FrameLayout.LayoutParams((displayMetrics.heightPixels * videoWith / videoHeight), ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER);
        if (playSet.getBoolean("view", true)) {
            surfaceView.setLayoutParams(params);
        } else textureView.setLayoutParams(params);
    }
    private void initPlayView(int videoWith, int videoHeight) {
        if (playSet.getBoolean("view", true)) {
            surfaceView = new SurfaceView(this);
            binding.pvFl.addView(surfaceView);
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            surfaceHolder.addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    adjustPlayView(videoWith,videoHeight);
                    ijkMediaPlayer.setDisplay(holder);
                    ijkMediaPlayer.start();
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
            binding.pvFl.addView(textureView);
            textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
                    adjustPlayView(videoWith,videoHeight);
                    ijkMediaPlayer.setSurface(new Surface(surface));
                    ijkMediaPlayer.start();
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
}