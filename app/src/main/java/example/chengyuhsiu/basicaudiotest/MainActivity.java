package example.chengyuhsiu.basicaudiotest;

import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.MediaController.MediaPlayerControl;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import android.net.Uri;
import android.content.ContentResolver;
import android.database.Cursor;
import android.widget.ListView;
import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;


public class MainActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    private ImageButton mBtnMediaPlayPause, mBtnMediaPrev, mBtnMediaNext;
    private ToggleButton mBtnMediaRepeat;


    private MediaPlayer mMediaPlayer = null;

    private Boolean mbIsInitialised = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBtnMediaPlayPause = (ImageButton)findViewById(R.id.btnMediaPlayPause);
        mBtnMediaPrev = (ImageButton)findViewById(R.id.btnPrev);
        mBtnMediaNext = (ImageButton)findViewById(R.id.btnNext);
        mBtnMediaRepeat = (ToggleButton) findViewById(R.id.btnRepeat);

        mBtnMediaPlayPause.setOnClickListener(btnMediaPlayPauseOnClick);
        mBtnMediaPrev.setOnClickListener(btnMediaPrevOnClick);
        mBtnMediaNext.setOnClickListener(btnMediaNextOnClick);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mMediaPlayer = new MediaPlayer();

        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.song);

        try {
            mMediaPlayer.setDataSource(this, uri);
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "指定的音樂檔錯誤！", Toast.LENGTH_LONG)
                    .show();
        }

        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnCompletionListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        mMediaPlayer.release();
        mMediaPlayer = null;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        mediaPlayer.stop();
        mbIsInitialised = true;
        mBtnMediaPlayPause.setImageResource(android.R.drawable.ic_media_play);
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
        mediaPlayer.release();
        mediaPlayer = null;

        Toast.makeText(MainActivity.this, "發生錯誤，停止播放", Toast.LENGTH_LONG)
                .show();

        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.seekTo(0);
        mediaPlayer.start();

        Toast.makeText(MainActivity.this, "開始播放", Toast.LENGTH_LONG)
                .show();
    }

    private View.OnClickListener btnMediaPlayPauseOnClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mMediaPlayer.isPlaying()) {
                mBtnMediaPlayPause.setImageResource(android.R.drawable.ic_media_play);
                mMediaPlayer.pause();
            } else {
                mBtnMediaPlayPause.setImageResource(android.R.drawable.ic_media_pause);

                if (mbIsInitialised) {
                    mMediaPlayer.prepareAsync();
                    mbIsInitialised = false;
                } else
                    mMediaPlayer.start();
            }
        }
    };

    private View.OnClickListener btnMediaPrevOnClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

        }
    };

    private View.OnClickListener btnMediaNextOnClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

        }
    };

    private View.OnClickListener btnMediaRepeatOnClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (((ToggleButton)v).isChecked())
                mMediaPlayer.setLooping(true);
            else
                mMediaPlayer.setLooping(false);
        }
    };
}
