
//



#ifndef XH_CORE_H
#define XH_CORE_H 1

#ifdef __cplusplus
extern "C" {
#endif

void xh_core_block_refresh();

void xh_core_unblock_refresh();

int xh_core_register(const char *pathname_regex_str, const char *symbol,
                     void *new_func, void **old_func);

int xh_core_ignore(const char *pathname_regex_str, const char *symbol);

int xh_core_grouped_register(int group_id, const char *pathname_regex_str, const char *symbol,
                           void *new_func, void **old_func);

int xh_core_grouped_ignore(int group_id, const char *pathname_regex_str, const char *symbol);

int xh_core_refresh(int async);

void xh_core_clear();

void xh_core_enable_debug(int flag);

void xh_core_enable_sigsegv_protection(int flag);

void* xh_core_elf_open(const char *path);

int xh_core_got_hook_symbol(void* h_lib, const char* symbol, void* new_func, void** old_func);

void xh_core_elf_close(void *h_lib);

#ifdef __cplusplus
}
#endif

#endif
