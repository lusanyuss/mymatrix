

#ifndef buffer_source_h
#define buffer_source_h

#include <sys/mman.h>
#include <sys/stat.h>
#include <unistd.h>
#include <stdlib.h>

class buffer_source {
public:
    buffer_source() {
        _buffer = NULL;
        _buffer_size = 0;
    }

    virtual ~buffer_source() {}

    inline void *buffer() { return _buffer; }

    inline size_t buffer_size() { return _buffer_size; }

    virtual void *realloc(size_t new_size) = 0;
    virtual void free() = 0;
    virtual bool init_fail() = 0;

protected:
    void *_buffer;
    size_t _buffer_size;
};

class buffer_source_memory : public buffer_source {
public:
    ~buffer_source_memory() { free(); }

    virtual bool init_fail() { return false; }

    static std::atomic<size_t> g_realloc_counter;
    static std::atomic<size_t> g_realloc_memory_counter;

    virtual void *realloc(size_t new_size) {

        if (_buffer) g_realloc_counter.fetch_add(1, std::memory_order_relaxed);

        void *ptr = ::realloc(_buffer, new_size);
        if (ptr != NULL) {

            g_realloc_memory_counter.fetch_sub(_buffer_size, std::memory_order_relaxed);
            g_realloc_memory_counter.fetch_add(new_size, std::memory_order_relaxed);

            _buffer = ptr;
            _buffer_size = new_size;
        }

        return ptr;
    }

    virtual void free() {
        if (_buffer) {

            g_realloc_memory_counter.fetch_sub(_buffer_size, std::memory_order_relaxed);

            ::free(_buffer);
            _buffer = NULL;
            _buffer_size = 0;
        }
    }
};

#endif /* buffer_source_h */
