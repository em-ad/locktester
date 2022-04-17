package com.lock.locklib

import com.lock.locklib.blelibrary.Data.BleCommon
import java.io.ByteArrayOutputStream

class TestRunner {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            println("TEST FUN BLUETOOTH")
            // letsTest()
        }

        fun letsTest() {
            val byteArrayOutputStream = ByteArrayOutputStream()
            byteArrayOutputStream.write(BleCommon.mUnlock, 0, BleCommon.mUnlock.size)
            val byte = BleCommon.addCrcAndEnd(byteArrayOutputStream)
            println(byte)
        }
    }

}