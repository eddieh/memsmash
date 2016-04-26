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
 */
package com.adcolony.memsmash;

import android.app.Activity;
import android.widget.TextView;
import android.os.Bundle;

import android.app.ActivityManager;

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

public class MemSmash extends Activity implements ComponentCallbacks2
{
    private List<byte[]> leakedMem;
    private int leakedSize = 0;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

        System.out.println(">>> mem class " + am.getMemoryClass());
        System.out.println(">>> large mem class " + am.getLargeMemoryClass());

        System.out.println(">>> max mem " + Runtime.getRuntime().maxMemory());


        leakedMem = new ArrayList();

        new Thread(new Runnable() {
            @Override
            public void run()
            {
                smashMemory();
            }
        }).start();

        TextView  tv = new TextView(this);
        //tv.setText(stringFromJNI());
        tv.setText("what");
        setContentView(tv);
    }

    @Override
    public void onTrimMemory(int level)
    {
        System.out.println(">>> TRIM!!! " + level);

        super.onTrimMemory(level);
    }

    @Override
    public void onLowMemory ()
    {
        System.out.println(">>> LOW!!!");
    }

    public native String  stringFromJNI();

    private synchronized void smashMemory ()
    {
        while (true) {
            leakedMem.add(new byte[1024 * 1024]);
            leakedSize++;
            System.out.println(">>> leaked " + leakedSize + " MB");

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // DGAF
            }

            // avoid unreachable compiler error (haha static analyzer)
            if (leakedMem == null) break;
        }
    }

    static {
        System.loadLibrary("memsmash");
    }
}
