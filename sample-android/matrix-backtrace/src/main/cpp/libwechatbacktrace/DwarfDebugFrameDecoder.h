

#ifndef _LIBWECHATBACKTRACE_DWARF_DEBUG_FRAME_H
#define _LIBWECHATBACKTRACE_DWARF_DEBUG_FRAME_H

#include <stdint.h>

#include <vector>

#include <unwindstack/DwarfSection.h>

#include "DwarfSectionDecoder.h"

namespace wechat_backtrace {

    template<typename AddressType>
    class DwarfDebugFrameDecoder : public DwarfSectionDecoder<AddressType> {
    public:
        DwarfDebugFrameDecoder(unwindstack::Memory *memory) : DwarfSectionDecoder<AddressType>(
                memory) {
            this->cie32_value_ = static_cast<uint32_t>(-1);
            this->cie64_value_ = static_cast<uint64_t>(-1);
        }

        virtual ~DwarfDebugFrameDecoder() = default;

        uint64_t GetCieOffsetFromFde32(uint32_t pointer) override {
            return this->entries_offset_ + pointer;
        }

        uint64_t GetCieOffsetFromFde64(uint64_t pointer) override {
            return this->entries_offset_ + pointer;
        }

        uint64_t AdjustPcFromFde(uint64_t pc) override { return pc; }
    };

}  // namespace wechat_backtrace

#endif  // _LIBWECHATBACKTRACE_DWARF_DEBUG_FRAME_H
