

#ifndef _LIBWECHATBACKTRACE_DWARF_EH_FRAME_H
#define _LIBWECHATBACKTRACE_DWARF_EH_FRAME_H

#include <stdint.h>

#include <unwindstack/DwarfSection.h>
#include <unwindstack/Memory.h>
#include "DwarfSectionDecoder.h"

namespace wechat_backtrace {

    template<typename AddressType>
    class DwarfEhFrameDecoder : public DwarfSectionDecoder<AddressType> {
    public:
        DwarfEhFrameDecoder(unwindstack::Memory *memory) : DwarfSectionDecoder<AddressType>(
                memory) {}

        virtual ~DwarfEhFrameDecoder() = default;

        uint64_t GetCieOffsetFromFde32(uint32_t pointer) override {
            return this->memory_.cur_offset() - pointer - 4;
        }

        uint64_t GetCieOffsetFromFde64(uint64_t pointer) override {
            return this->memory_.cur_offset() - pointer - 8;
        }

        uint64_t AdjustPcFromFde(uint64_t pc) override {
            // The eh_frame uses relative pcs.
            return pc + this->memory_.cur_offset() - 4;
        }
    };

}  // namespace wechat_backtrace

#endif  // _LIBWECHATBACKTRACE_DWARF_EH_FRAME_H
