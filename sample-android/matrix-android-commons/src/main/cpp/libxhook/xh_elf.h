


#ifndef XH_ELF_H
#define XH_ELF_H 1

#include <stdint.h>
#include <elf.h>
#include <link.h>

#ifdef __cplusplus
extern "C" {
#endif

typedef struct
{
    const char *pathname;
    
    ElfW(Addr)  base_addr;
    ElfW(Addr)  bias_addr;
    
    ElfW(Ehdr) *ehdr;
    ElfW(Phdr) *phdr;

    ElfW(Dyn)  *dyn; //.dynamic
    ElfW(Word)  dyn_sz;

    const char *strtab; //.dynstr (string-table)
    ElfW(Sym)  *symtab; //.dynsym (symbol-index to string-table's offset)

    ElfW(Addr)  relplt; //.rel.plt or .rela.plt
    ElfW(Word)  relplt_sz;
    
    ElfW(Addr)  reldyn; //.rel.dyn or .rela.dyn
    ElfW(Word)  reldyn_sz;
    
    ElfW(Addr)  relandroid; //android compressed rel or rela
    ElfW(Word)  relandroid_sz;

    //for ELF hash
    uint32_t   *bucket;
    uint32_t    bucket_cnt;
    uint32_t   *chain;
    uint32_t    chain_cnt; //invalid for GNU hash

    //append for GNU hash
    uint32_t    symoffset;
    ElfW(Addr) *bloom;
    uint32_t    bloom_sz;
    uint32_t    bloom_shift;
    
    int         is_use_rela;
    int         is_use_gnu_hash;
} xh_elf_t;

int xh_elf_init(xh_elf_t *self, uintptr_t base_addr, const char *pathname);
int xh_elf_hook(xh_elf_t *self, const char *symbol, void *new_func, void **old_func);

int xh_elf_check_elfheader(uintptr_t base_addr);

int xh_elf_find_symidx_by_name(xh_elf_t *self, const char *symbol, uint32_t *symidx);

#ifdef __cplusplus
}
#endif

#endif
