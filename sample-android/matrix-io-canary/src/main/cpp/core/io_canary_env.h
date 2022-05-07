

//
//

#ifndef MATRIX_IO_CANARY_IO_CANARY_ENV_H
#define MATRIX_IO_CANARY_IO_CANARY_ENV_H

namespace iocanary {

    enum IOCanaryConfigKey {
        kMainThreadThreshold = 0,
        kSmallBufferThreshold,
        kRepeatReadThreshold,

        //!!kConfigKeysLen always the last one!!
        kConfigKeysLen
    };

    class IOCanaryEnv {
    public:
        IOCanaryEnv();
        void SetConfig(IOCanaryConfigKey key, long val);

        long GetJavaMainThreadID() const;
        long GetMainThreadThreshold() const;
        long GetSmallBufferThreshold() const;
        long GetRepeatReadThreshold() const;

        //in μs.
        //it may be negative if the io-cost more than POSSIBLE_NEGATIVE_THRESHOLD
        //else it can be negligible
        //80% of the well-known 16ms
        constexpr static const int kPossibleNegativeThreshold = 13*1000;
        constexpr static const int kSmallBufferOpTimesThreshold = 20;
    private:
        long GetConfig(IOCanaryConfigKey key) const;

        //in μs
        constexpr static const int kDefaultMainThreadTriggerThreshold = 500*1000;
        //We take 4096B(4KB) as a small size of the buffer
        constexpr static const int kDefaultBufferSmallThreshold = 4096;
        constexpr static const int kDefaultRepeatReadThreshold = 5;

        long configs_[IOCanaryConfigKey::kConfigKeysLen];
    };
}

#endif //MATRIX_IO_CANARY_IO_CANARY_ENV_H
