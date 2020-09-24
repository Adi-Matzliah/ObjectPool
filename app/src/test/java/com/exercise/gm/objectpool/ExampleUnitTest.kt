package com.exercise.gm.objectpool

import com.exercise.gm.objectpool.connection.ConnectionPoolDemo
import com.exercise.gm.objectpool.demo.ObjectPoolDemo
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun objectPool_isRunning() {
        ObjectPoolDemo.runDemo()
    }
}