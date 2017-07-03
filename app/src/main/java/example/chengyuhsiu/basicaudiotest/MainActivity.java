package example.chengyuhsiu.basicaudiotest;

import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.MediaController.MediaPlayerControl;

import java.io.IOException;
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

    private final int REQUEST_PERMISSION_FOR_WRITE_EXTERNAL_STORAGE = 100;

    private String TAG="BasicAudioTest";

    private ArrayList<Song> songList;
    private ListView songView;
    private int song_id = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.e(TAG, "onCreate()");
        mBtnMediaPlayPause = (ImageButton)findViewById(R.id.btnMediaPlayPause);
        mBtnMediaPrev = (ImageButton)findViewById(R.id.btnPrev);
        mBtnMediaNext = (ImageButton)findViewById(R.id.btnNext);
        mBtnMediaRepeat = (ToggleButton) findViewById(R.id.btnRepeat);

        mBtnMediaPlayPause.setOnClickListener(btnMediaPlayPauseOnClick);
        mBtnMediaPrev.setOnClickListener(btnMediaPrevOnClick);
        mBtnMediaNext.setOnClickListener(btnMediaNextOnClick);
        mBtnMediaRepeat.setOnClickListener(btnMediaRepeatOnClick);

        askForWriteExternalStoragePermission();

        songView = (ListView)findViewById(R.id.song_list);
        songList = new ArrayList<Song>();
        getSongList();

        Collections.sort(songList, new Comparator<Song>() {
            @Override
            public int compare(Song song, Song song2) {
                return song.getTitle().compareTo(song2.getTitle());
            }
        });

        SongAdapter songAdt = new SongAdapter(this, songList);
        songView.setAdapter(songAdt);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        // 檢查收到的權限要求編號是否和我們送出的相同
        if (requestCode == REQUEST_PERMISSION_FOR_WRITE_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "取得 WRITE_EXTERNAL_STORAGE 權限", Toast.LENGTH_SHORT)
                        .show();
                return;
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume()");
        mMediaPlayer = new MediaPlayer();

        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnCompletionListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "onStop()");
        mMediaPlayer.release();
        mMediaPlayer = null;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        mediaPlayer.stop();
        Log.e(TAG, "onCompletion()");
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
            Log.e(TAG, "btnMediaPlayPaueOnClick start");
            if (mMediaPlayer.isPlaying()) {
                mBtnMediaPlayPause.setImageResource(android.R.drawable.ic_media_play);
                mMediaPlayer.pause();
                Log.e(TAG, "btnMediaPlayPauseOnClick pause");
            } else {
                mBtnMediaPlayPause.setImageResource(android.R.drawable.ic_media_pause);
                Log.e(TAG, "btnMediaPlayPauseOnClick : go to playSong()");
                playSong();
            }
        }
    };

    private View.OnClickListener btnMediaPrevOnClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            Log.e(TAG, "btnMediaPrev song_id=" + song_id);
            if (song_id > 0) {
                song_id--;
                Log.e(TAG, "btnMediaPrev 2 song_id=" + song_id);
            } else {
                song_id = 0;
            }
            playSong();
        }
    };

    private View.OnClickListener btnMediaNextOnClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            Log.e(TAG, "btnMediaPrev song list size=" + songList.size());

            if (song_id < songList.size()-1) {
                song_id++;
            } else {
                song_id = songList.size() - 1;
            }
            playSong();
        }
    };

    private View.OnClickListener btnMediaRepeatOnClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            Log.e(TAG, "Try to set repeat");
            if (((ToggleButton)v).isChecked()) {
                mMediaPlayer.setLooping(true);
            } else {
                mMediaPlayer.setLooping(false);
            }
        }
    };

    private void askForWriteExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    MainActivity.this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                AlertDialog.Builder altDlgBuilder =
                        new AlertDialog.Builder(MainActivity.this);
                altDlgBuilder.setTitle("提示");
                altDlgBuilder.setMessage("App需要讀寫SD卡中的資料。");
                altDlgBuilder.setIcon(android.R.drawable.ic_dialog_info);
                altDlgBuilder.setCancelable(false);
                altDlgBuilder.setPositiveButton("確定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{
                                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        REQUEST_PERMISSION_FOR_WRITE_EXTERNAL_STORAGE);
                            }
                        });
                altDlgBuilder.show();
                return;
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_PERMISSION_FOR_WRITE_EXTERNAL_STORAGE);
                return;
            }
        }
    }

    private void playSong(){
        Song playSong = songList.get(song_id);
        long currSong = playSong.getID();
        String songTitle = playSong.getTitle();

        Log.e(TAG, "playSong() : " + mMediaPlayer.isPlaying());
        // joe : how to avoid first time play error
        // if add this back, a bug play -> pause -> play failed
        //if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
        //}

        Log.e(TAG, "playSong(): " + currSong + " title: " + songTitle);
        Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currSong);
        try {
            mMediaPlayer.setDataSource(this, trackUri);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void getSongList() {
        ContentResolver musicResolver = getContentResolver();
        Uri mUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(mUri,null, null, null, null);

        if(musicCursor!=null && musicCursor.moveToFirst()){
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                songList.add(new Song(thisId, thisTitle, thisArtist));
            } while (musicCursor.moveToNext());
        }
    }

    public void songPicked(View view) {
        song_id = Integer.parseInt(view.getTag().toString());
        Log.e(TAG, "song picked test " + song_id + " playing " + mMediaPlayer.isPlaying());

        if (mMediaPlayer.isPlaying() == false) {
            Log.e(TAG, "songPicked set to pause");
            mBtnMediaPlayPause.setImageResource(android.R.drawable.ic_media_pause);
        }
        playSong();
    }
}
