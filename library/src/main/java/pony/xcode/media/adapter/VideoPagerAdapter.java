package pony.xcode.media.adapter;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;

import com.gallopmark.imagepicker.R;
import pony.xcode.media.bean.MediaBean;
import pony.xcode.media.utils.DateUtils;
import pony.xcode.media.utils.DialogUtils;
import pony.xcode.media.utils.MediaUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

//视频预览适配器
public class VideoPagerAdapter extends MediaBasePagerAdapter<MediaBean> {
    private List<View> mViewList;
    private OnPagerItemClickListener mListener;
    private VideoView mPlayingView;
    private TimerTask mTimerTask;
    private int mCurrentPosition = -1;

    public VideoPagerAdapter(Context context, List<MediaBean> dataList, ViewPager viewPager, int initPosition) {
        super(context, dataList);
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        if (mDataList != null && !mDataList.isEmpty()) {
            mViewList = new ArrayList<>();
            for (int i = 0, count = mDataList.size(); i < count; i++) {
                View view = layoutInflater.inflate(R.layout.mediaselector_adapter_video_view, new FrameLayout(context), false);
                mViewList.add(view);
            }
        }
        init(viewPager, mDataList, initPosition);
    }

    /**
     * viewPager 选中监听
     *
     * @param images       传入预览的数据源
     * @param initPosition 从哪个位置开始预览
     */
    private void init(ViewPager viewPager, final List<MediaBean> images, int initPosition) {
        final ViewPager.SimpleOnPageChangeListener listener = new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (isWithinRange(position)) {
                    stopPlaybackIfNoNull();
                    View view = mViewList.get(position);
                    playVideo(view, images.get(position).getPath());
                }
            }
        };
        viewPager.addOnPageChangeListener(listener);
        if (initPosition == 0) {
            listener.onPageSelected(initPosition);
        }
    }

    private boolean isWithinRange(int position) {
        return mViewList != null && !mViewList.isEmpty() && position >= 0 && position < mViewList.size();
    }

    private void stopPlaybackIfNoNull() {
        if (mPlayingView != null) {
            removeTimerTask();
            mPlayingView.stopPlayback();
            mPlayingView = null;
        }
    }

    private void removeTimerTask() {
        if (mTimerTask != null) {
            mPlayingView.removeCallbacks(mTimerTask);
            mTimerTask = null;
        }
    }

    private void playVideo(final View view, final String path) {
        mPlayingView = view.findViewById(R.id.videoView);
        final ImageView playerButton = view.findViewById(R.id.iv_player);
        final LinearLayout llController = view.findViewById(R.id.ll_controller);
        llController.setVisibility(View.GONE);
        final SeekBar seekBar = llController.findViewById(R.id.seekBar);
        final TextView currentTextView = llController.findViewById(R.id.tv_current);
        final TextView durationTextView = llController.findViewById(R.id.tv_duration);
        mPlayingView.setVideoURI(MediaUtil.getProviderUri(mContext, new File(path)));
        mPlayingView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(final MediaPlayer mp) {
                seekBar.setMax(mp.getDuration());
                llController.setVisibility(View.VISIBLE);
                currentTextView.setText(DateUtils.generateTime(mp.getCurrentPosition()));
                durationTextView.setText(DateUtils.generateTime(mp.getDuration()));
                playerButton.setVisibility(View.GONE);
                mp.start();
                if (mCurrentPosition > 0) {
                    mPlayingView.seekTo(mCurrentPosition);
                    mCurrentPosition = -1;
                }
                runTimer(currentTextView, seekBar);
            }
        });
        mPlayingView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                llController.setVisibility(View.GONE);
                playerButton.setVisibility(View.VISIBLE);
                removeTimerTask();
            }
        });
        mPlayingView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                removeTimerTask();
                DialogUtils.unablePlayVideo(mContext);
                return false;
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mPlayingView.isPlaying()) {
                    mPlayingView.seekTo(seekBar.getProgress());
                }
            }
        });
        playerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playVideo(view, path);
            }
        });
    }

    private void runTimer(TextView tvCurrent, SeekBar seekBar) {
        mTimerTask = new TimerTask(tvCurrent, seekBar);
        if (mPlayingView != null) {
            mPlayingView.post(mTimerTask);
        }
    }

    private class TimerTask implements Runnable {
        private TextView tvCurrent;
        private SeekBar seekBar;

        TimerTask(TextView tvCurrent, SeekBar seekBar) {
            this.tvCurrent = tvCurrent;
            this.seekBar = seekBar;
        }

        @Override
        public void run() {
            if (mPlayingView != null && mPlayingView.isPlaying()) {
                int currentPosition = mPlayingView.getCurrentPosition();
                tvCurrent.setText(DateUtils.generateTime(currentPosition));
                seekBar.setProgress(currentPosition);
                //关键点 1000- currentPosition % 1000 由于 getCurrentPosition（）会少于 1000毫秒
                mPlayingView.postDelayed(this, 1000 - (currentPosition % 1000));
            }
        }
    }

    public void resume() {
        if (mPlayingView != null && !mPlayingView.isPlaying()) {
            mPlayingView.start();
        }
    }

    public void pause() {
        if (mPlayingView != null && mPlayingView.isPlaying()) {
            mPlayingView.pause();
            mCurrentPosition = mPlayingView.getCurrentPosition();
            removeTimerTask();
        }
    }

    public void destroy() {
        if (mPlayingView != null) {
            removeTimerTask();
        }
        stopPlaybackIfNoNull();
    }

    @Override
    public int getCount() {
        return mViewList == null ? 0 : mViewList.size();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        View view = mViewList.get(position);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClick(position);
                }
            }
        });
        container.addView(view);
        return view;
    }

    public void setOnPagerItemClickListener(OnPagerItemClickListener listener) {
        this.mListener = listener;
    }
}
