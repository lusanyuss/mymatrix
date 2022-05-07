

#ifndef _LIBWECHATBACKTRACE_GLOBAL_H
#define _LIBWECHATBACKTRACE_GLOBAL_H

#include <stdint.h>

#include <memory>
#include <mutex>
#include <string>
#include <unordered_map>
#include <vector>

#include <unwindstack/Elf.h>
#include <unwindstack/Memory.h>

// Forward declarations.
namespace wechat_backtrace {
    class Maps;

    class QuickenMapInfo;

    class DebugGlobal {
    public:
        explicit DebugGlobal(std::shared_ptr<unwindstack::Memory> &memory);

        DebugGlobal(std::shared_ptr<unwindstack::Memory> &memory, std::vector<std::string> &search_libs);

        virtual ~DebugGlobal() = default;

        void SetArch(unwindstack::ArchEnum arch);

        unwindstack::ArchEnum arch() { return arch_; }

    protected:
        bool Searchable(const std::string &name);

        void FindAndReadVariable(wechat_backtrace::Maps *maps, const char *variable);

        virtual bool ReadVariableData(uint64_t offset) = 0;

        virtual void ProcessArch() = 0;

        unwindstack::ArchEnum arch_ = unwindstack::ARCH_UNKNOWN;

        std::shared_ptr<unwindstack::Memory> memory_;
        std::vector<std::string> search_libs_;
    };

}  // namespace unwindstack

#endif  // _LIBWECHATBACKTRACE_GLOBAL_H
