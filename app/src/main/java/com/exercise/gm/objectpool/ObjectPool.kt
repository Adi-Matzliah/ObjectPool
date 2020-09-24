package com.exercise.gm.objectpool

import java.lang.ref.PhantomReference
import java.lang.ref.Reference
import java.lang.ref.ReferenceQueue
import java.util.*

open class ObjectPool(maxObjects: Int = 1) {
    private val _pool: Queue<SomeObject> = LinkedList()

    private val _refQueue: ReferenceQueue<Any> = ReferenceQueue()
    private val _refToObj: IdentityHashMap<Any, SomeObject> = IdentityHashMap()
    private val _objToRef: IdentityHashMap<SomeObject, Any> = IdentityHashMap()

    init {
        println("initializing the pool:")
        for (i in 0 until maxObjects) {
            _pool.add(ObjectFactory.createObject(i, i.toString()))
            println("object number $i was created ${_pool.size}")
        }
    }

    /**
     * The internal method to retrieve a object from the pool,
     * associating it with a weak reference. This is called from
     * [.pull], which is responsible for ensuring
     * that there's a object in the pool
     */
    @Synchronized
    private fun wrapObject(someObject: SomeObject): SomeObject {
        val wrapped = SomeObject(someObject.someInt, someObject.someString)
        val ref: PhantomReference<SomeObject> = PhantomReference(wrapped, _refQueue)
        _objToRef[someObject] = ref
        _refToObj[ref] = someObject
        System.err.println("Acquired object $someObject")
        return wrapped
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
     * Returns a object to the pool when the associated reference is
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

    /**
     * Retrieves a object from the pool, blocking until one becomes
     * available.
     */
    fun pull(): SomeObject {
        //println("waiting for retrieving object from the pull...")
        while (true) {
            synchronized(this) {
                if (_pool.size > 0) {
                    return wrapObject(_pool.remove())
                }
            }
            tryWaitingForGarbageCollector()
        }
    }
}