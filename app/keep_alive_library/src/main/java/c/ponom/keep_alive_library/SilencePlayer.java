package c.ponom.keep_alive_library;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;



@SuppressWarnings("unused")
public class SilencePlayer {

    static private MediaPlayer player;
    boolean playerPlaying = false;
    boolean playerContinuouslyPlaying = false;
    int audioResourceId;


    public void stopRepeat() {
        playerContinuouslyPlaying = false;
    }


    public SilencePlayer(){

    }



    public void setVolume(float volume){
        if (player != null)
            player.setVolume(volume,0);
        // тишину можно и в моно поиграть для экономии
    }


    public void startPlayer(Context context) {
        // то он ранее был released
        if (player==null) player =MediaPlayer.create(context, audioResourceId);
        player.start();
        playerPlaying = true;
    }


    public void pausePlayer() {
        if (player==null||!player.isPlaying()) return;
        player.pause();
        playerPlaying = false;
    }

    public void stopPlayer() {
        if (player==null||!player.isPlaying()) return;
        playerPlaying = false;
        player.stop();
    }



    public void releasePlayer() {
        if (player != null) {
            if (playerPlaying) pausePlayer();
            player.reset();
            player.release();
            player =null;
        }
    }

    void playOnce(Context context,int audioResource){
        audioResourceId = audioResource;
        releasePlayer();
        startPlayer(context);
        player.setOnCompletionListener(mp -> {
            releasePlayer();
        });

    }
    // todo - не доделано проигрывние после рестарта несколько раз
    public void launchNewPeriodicPlay(Context context,int audioResource, int pause){
        audioResourceId = audioResource;
        setVolume(LifeKeeper.AUDIO_VOLUME);
        releasePlayer();
        player =MediaPlayer.create(context, audioResourceId);
        startPlayer(context);
        player.setOnCompletionListener(mp -> {
            Runnable playAgain = () -> {
                if (playerPlaying) startPlayer(context);
                LifeKeeper.getInstance().emitEvents();
            };
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(playAgain,pause);
        });
    }

    public boolean isPlayerPlayingState() {
        return player.isPlaying();
    }

}
