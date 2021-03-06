

#ifndef _LIWECHATBACKTRACE_JIT_DEBUG_H
#define _LIWECHATBACKTRACE_JIT_DEBUG_H

#include <stdint.h>

#include <memory>
#include <mutex>
#include <string>
#include <vector>

#include <unwindstack/Global.h>
#include <unwindstack/Memory.h>

#include "BacktraceDefine.h"
#include "QuickenTable.h"
#include "DebugGlobal.h"
#include "QuickenInMemory.h"

namespace unwindstack {
    enum ArchEnum : uint8_t;

    class Elf;
}

namespace wechat_backtrace {

    // Forward declarations.
    class Maps;

    class DebugJit : public DebugGlobal {
    public:
        explicit DebugJit(std::shared_ptr<unwindstack::Memory> &memory);

        virtual ~DebugJit();

        unwindstack::Elf *GetElf(Maps *maps, uint64_t pc);

        bool GetFutSectionsInMemory(
                Maps *maps,
                uint64_t pc,
                /* out */ std::shared_ptr<wechat_backtrace::QutSectionsInMemory> &fut_sections);

        static std::shared_ptr<DebugJit> &Instance();

    private:
        void Init(Maps *maps);

        uint64_t (DebugJit::*read_descriptor_func_)(uint64_t) = nullptr;

        uint64_t (DebugJit::*read_entry_func_)(uint64_t *, uint64_t *) = nullptr;

        uint64_t ReadDescriptor32(uint64_t);

        uint64_t ReadDescriptor64(uint64_t);

        uint64_t ReadEntry32Pack(uint64_t *start, uint64_t *size);

        uint64_t ReadEntry32Pad(uint64_t *start, uint64_t *size);

        uint64_t ReadEntry64(uint64_t *start, uint64_t *size);

        bool ReadVariableData(uint64_t ptr_offset) override;

        void ProcessArch() override;

        uint64_t entry_addr_ = 0;
        bool initialized_ = false;
        std::vector<unwindstack::Elf *> elf_list_;      // TODO

        std::mutex lock_;

        std::unique_ptr<QuickenInMemory<addr_t>> quicken_in_memory_;

//        const bool log = false;
    };

}  // namespace unwindstack

#endif  // _LIWECHATBACKTRACE_JIT_DEBUG_H
