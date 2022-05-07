



#ifndef LIBMATRIX_JNI_ENHANCEDLSYM_H
#define LIBMATRIX_JNI_ENHANCEDLSYM_H

#include <map>
#include <string>
#include <link.h>
#include <mutex>

namespace enhance {
    void* dlopen(const char* __file_name, int __flag);
    int dlclose(void* __handle);
    void* dlsym(void* __handle, const char* __symbol);
    size_t dlsizeof(void *__addr);

    struct DlInfo {

        DlInfo() {}
        ~DlInfo() {
            if (strtab) {
                free(strtab);
            }
            if (symtab) {
                free(symtab);
            }
        }

        std::string pathname;

        ElfW(Addr)  base_addr;
        ElfW(Addr)  bias_addr;

        ElfW(Ehdr) *ehdr; // pointing to loaded mem
        ElfW(Phdr) *phdr; // pointing to loaded mem

        char *strtab = nullptr; // strtab
        ElfW(Word) strtab_size; // size in bytes

        ElfW(Sym)  *symtab = nullptr;
        ElfW(Word) symtab_num;

    };
}

#endif //LIBMATRIX_JNI_ENHANCEDLSYM_H
