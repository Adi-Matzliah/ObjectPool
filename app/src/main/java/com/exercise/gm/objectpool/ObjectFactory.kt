package com.exercise.gm.objectpool

object ObjectFactory {

    fun createObject(id: Int, str: String): SomeObject = SomeObject(id, str)
}