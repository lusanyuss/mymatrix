

//
//

#ifndef LIBMATRIX_HOOK_MEMORYHOOK_H
#define LIBMATRIX_HOOK_MEMORYHOOK_H

#define TAG "Matrix.MemoryHook"

extern "C" void fake_malloc(void * ptr, size_t byte_count);

extern "C" void fake_free(void * ptr);

void on_alloc_memory(void *caller, void *ptr, size_t byte_count);

void on_realloc_memory(void *caller, void *ptr, size_t byte_count);

void on_free_memory(void *ptr);

void on_mmap_memory(void *caller, void *ptr, size_t byte_count);

void on_munmap_memory(void *ptr);

void dump(bool enable_mmap = false,
          const char *log_path = nullptr,
          const char *json_path = nullptr);

void enable_stacktrace(bool);

void set_stacktrace_log_threshold(size_t threshold);

void set_tracing_alloc_size_range(size_t min, size_t max);

void memory_hook_init();

#endif //LIBMATRIX_HOOK_MEMORYHOOK_H
