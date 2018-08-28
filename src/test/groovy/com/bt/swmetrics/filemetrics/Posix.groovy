package com.bt.swmetrics.filemetrics

import com.sun.jna.NativeLibrary
import com.sun.jna.Platform
import com.sun.jna.Function

// For the original inspiration for this code see
// https://issues.apache.org/jira/browse/GROOVY-7493

class Posix {
    private static final int MAX_PATH = 1024
    private NativeLibrary posixLibraryInstance = null

    void chdir(String dirName) {
        def chdirFunction = getNativeFunction("chdir")
        int error = chdirFunction.invokeInt([dirName] as Object[])
        if (error != 0) {
            throw new Error("Could not change working directory to $dirName: error code = $error)")
        }
    }

     String getcwd() {
        def getcwdFunction = getNativeFunction("getcwd")
         getcwdFunction.invokeString([new String(new byte[MAX_PATH]), MAX_PATH] as Object[], false);
    }

    private NativeLibrary getPosixLibrary() {
        if (posixLibraryInstance == null) {
            posixLibraryInstance = NativeLibrary.getInstance(null)
        }
        posixLibraryInstance
    }

    private Function getNativeFunction(String name) {
        posixLibrary.getFunction(getOsDependentFunctionName(name))
    }

    private static String getOsDependentFunctionName(String name) {
        Platform.isWindows() ? "_" + name : name
    }
}