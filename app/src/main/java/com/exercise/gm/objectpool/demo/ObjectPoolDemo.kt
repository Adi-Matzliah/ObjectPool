package com.exercise.gm.objectpool.demo

import com.exercise.gm.objectpool.ObjectPool
import com.exercise.gm.objectpool.SomeObject
 
object ObjectPoolDemo {


    private const val TAG = "ObjectPoolDemo"

    fun runDemo() {
        println("Here we start the demo...")
        val pool = ObjectPool(5)
        lateinit var objectPooled: SomeObject
        for (index in 1..10) {
            objectPooled = pool.pull()
            println("pulled object $objectPooled")
            attemptGC()
        }
    }

    private fun attemptGC() {
        // allocate  chunks of memory in order to do some works, then run garbage collector clean
        val foo = ArrayList<ByteArray>()
        for (ii in 0..999) foo.add(ByteArray(1024))
        println("Garbage collector is running...")
        System.gc()
    }


}
