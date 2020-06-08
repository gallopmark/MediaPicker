package pony.xcode.media;

public class VideoConfig {

    private int mDurationLimit; //录像限制的时长
    private long mSizeLimit; //录像限制的大小
    private int mQuality; //拍照的质量  只能是0或1

    private VideoConfig(Builder builder) {
        this.mDurationLimit = builder.mDurationLimit;
        this.mSizeLimit = builder.mSizeLimit;
        this.mQuality = builder.mQuality;
    }

    public int getDurationLimit() {
        return mDurationLimit;
    }

    public long getSizeLimit() {
        return mSizeLimit;
    }

    public int getQuality() {
        return mQuality;
    }

    public static class Builder {
        private int mDurationLimit = 0; //单位秒
        private long mSizeLimit = 0; //以字节为单位
        private int mQuality = 1; //拍照的质量  只能是0或1

        public Builder setDurationLimit(int durationLimit) {
            this.mDurationLimit = durationLimit;
            return this;
        }

        public Builder setSizeLimit(long sizeLimit) {
            this.mSizeLimit = sizeLimit;
            return this;
        }

        public Builder setQuality(int quality) {
            this.mQuality = quality;
            return this;
        }

        public VideoConfig build() {
            return new VideoConfig(this);
        }
    }
}
