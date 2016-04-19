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

import java.util.List;
import java.util.ArrayList;

public class MemSmash extends Activity
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
        while (true) {
            leakedMem.add(new byte[1024 * 1024]);
            leakedSize++;
            System.out.println(">>> leaked " + leakedSize + " MB");

            // avoid unreachable compiler error (haha static analyzer)
            if (leakedMem == null) break;
        }

        TextView  tv = new TextView(this);
        tv.setText(stringFromJNI());
        setContentView(tv);
    }

    public native String  stringFromJNI();

    static {
        System.loadLibrary("memsmash");
    }
}
