package com.exercise.gm.objectpool

import java.lang.ref.PhantomReference
import java.lang.ref.Reference
import java.lang.ref.ReferenceQueue
import java.util.*

class ObjectPool(maxObjects: Int = 1) {
    private val _pool: Queue<SomeObject> = LinkedList<SomeObject>()

    private val _refQueue: ReferenceQueue<Any> = ReferenceQueue()
    private val _refToObj: IdentityHashMap<Any, SomeObject> = IdentityHashMap()
    private val _objToRef: IdentityHashMap<SomeObject, Any> = IdentityHashMap()

    init {
        for (i in 1 until maxObjects) {
            _pool.add(ObjectFactory.createObject(i, i.toString()))
        }
    }

    /**
     * The internal method to retrieve a connection from the pool,
     * associating it with a weak reference. This is called from
     * [.getConnection], which is responsible for ensuring
     * that there's a connection in the pool
     */
    @Synchronized
    private fun wrapObject(someObject: SomeObject): SomeObject {
        val ref: PhantomReference<SomeObject> = PhantomReference(someObject, _refQueue)
        _objToRef[someObject] = ref
        _refToObj[ref] = someObject
        return someObject
    }


    /**
     * Retrieves a object from the pool, blocking until one becomes
     * available.
     */
    fun pull(): SomeObject {
        while (true) {
            synchronized(this) {
                if (_pool.size > 0) {
                    return wrapObject(_pool.remove())
                }
            }
            tryWaitingForGarbageCollector()
        }
    }

    /**
     * Called by [.pullObject] when there are no objects in the
     * pool, to see if one has been recovered by the garbage collector. This
     * function waits a short time, but then returns so that the caller can
     * again look in the pool.
     */
    private fun tryWaitingForGarbageCollector() {
        try {
            val ref: Reference<*>? = _refQueue.remove(100)
            ref?.let {
                releaseObject(ref)
            }
        } catch (e: InterruptedException) {
        }
    }

    /**
     * Returns a connection to the pool when the associated reference is
     * enqueued.
     */
    @Synchronized
    private fun releaseObject(ref: Reference<*>) {
        val obj: SomeObject? = _refToObj.remove(ref)
        obj?.let{
            releaseObject(it)
        }
    }

    /**
     * Returns an object to the pool.
     */
    @Synchronized
    private fun releaseObject(obj: SomeObject) {
        val ref: Any? = _objToRef.remove(obj)
        _refToObj.remove(ref)
        _pool.offer(obj)
        System.err.println("Released object $obj")
    }

}