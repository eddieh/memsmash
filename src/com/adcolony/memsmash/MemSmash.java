/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * - smash Java heap on UI thread
 * - smash Java heap on background thread
 * - consume X% of Java heap
 * - leak over time
 * - leak at a specific time
 * - leak at a random time
 *
 * - smash native heap on UI thread
 * - smash native heap on background thread
 * - consume X% of system memory
 * - consume X% of virtual memory
 * - malloc failure
 */
package com.adcolony.memsmash;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.app.ActivityManager;

import android.content.ComponentCallbacks;

/*
 * I've got to praise Google for naming this interface. It is just the
 * best. What could be more clear? That name truly lets you know that
 * this is an interface containing a single callback method for memory
 * management. We include it here so we can implement the callback:
 * onTrimMemory(int). Genius!
 */
import android.content.ComponentCallbacks2;

import java.util.List;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MemSmash extends Activity implements ComponentCallbacks, ComponentCallbacks2
{
    private List<byte[]> leakedMem = new ArrayList();
    private int leakedSize = 0;

    void log(String str)
    {
        Log.i("MemSmash", str);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout);

        //printMemoryStats();
        //smashMemoryOnUIThread();
        //smashMemoryOnBackgroundThread();
    }

    /*
     * Message handlers
     */

    public void leakMemoryMessage(View view)
    {
        log("!!! Leak mem pressed");
        //smashMemoryOnUIThread();
        smashMemoryOnBackgroundThread();
    }

    /*
     * Java memory
     */

    @Override
    public void onTrimMemory(int level)
    {
        log(">>> TRIM!!! " + level);

        super.onTrimMemory(level);
    }

    @Override
    public void onLowMemory ()
    {
        log(">>> LOW!!!");
    }

    private void printMemoryStats()
    {
        ActivityManager am = (ActivityManager)getSystemService(ACTIVITY_SERVICE);

        log(">>> mem class " + am.getMemoryClass());
        log(">>> large mem class " + am.getLargeMemoryClass());
        log(">>> max mem " + Runtime.getRuntime().maxMemory());
    }

    private void smashMemoryOnUIThread()
    {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                leakedMem.add(new byte[1024 * 1024]);
                leakedSize++;
                log(">>> leaked " + leakedSize + " MB");

                smashMemoryOnUIThread();
            }
        }, 1000);
    }

    private void smashMemoryOnBackgroundThread()
    {
        new Thread(new Runnable() {
            @Override
            public void run()
            {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                while (true) {
                    synchronized (leakedMem) {
                        leakedMem.add(new byte[1024 * 1024]);
                        leakedSize++;
                        log(">>> leaked " + leakedSize + " MB");

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            // DGAF
                        }

                        // avoid unreachable compiler error (haha static analyzer)
                        if (leakedMem == null) break;
                    }
                }
            }
        }).start();
    }

    /*
     * native memory
     */

    public native String  stringFromJNI();

    static {
        System.loadLibrary("memsmash");
    }
}
