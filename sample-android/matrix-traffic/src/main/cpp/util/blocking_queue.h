

// Author: leafjia@tencent.com
//
// blocking_queue.h

#ifndef MATRIX_ANDROID_BLOCKING_QUEUE_H
#define MATRIX_ANDROID_BLOCKING_QUEUE_H
#include <deque>
#include <mutex>

using namespace std;

template <typename T>
class blocking_queue {
private:
    deque<T> coreQueue;
    mutex queueGuard;

public:
    void push(T msg) {
        queueGuard.lock();
        coreQueue.push_back(msg);
        queueGuard.unlock();
    }
    void pop() {
        queueGuard.lock();
        this->coreQueue.pop_front();
        queueGuard.unlock();
    }
    T front() {
        queueGuard.lock();
        T msg = coreQueue.front();
        queueGuard.unlock();
        return msg;
    }
    bool empty() {
        return coreQueue.empty();
    }
    int size() {
        return coreQueue.size();
    }
    void shrink_to_fit() {
        queueGuard.lock();
        coreQueue.shrink_to_fit();
        queueGuard.unlock();
    }
    void clear() {
        queueGuard.lock();
        coreQueue.clear();
        queueGuard.unlock();
    }
};


#endif //MATRIX_ANDROID_BLOCKING_QUEUE_H
