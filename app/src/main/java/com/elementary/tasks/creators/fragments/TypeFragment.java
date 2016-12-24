package com.elementary.tasks.creators.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;

import com.elementary.tasks.R;

/**
 * Copyright 2016 Nazar Suhovich
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public abstract class TypeFragment extends Fragment {

    protected Context mContext;
    protected ReminderInterface mInterface;

    public abstract boolean save();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (mContext == null) {
            mContext = context;
        }
        if (mInterface == null) {
            mInterface = (ReminderInterface) context;
            mInterface.setExclusionAction(null);
            mInterface.setRepeatAction(null);
            mInterface.setEventHint(getString(R.string.remind_me));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (mContext == null) {
            mContext = activity;
        }
        if (mInterface == null) {
            mInterface = (ReminderInterface) activity;
            mInterface.setExclusionAction(null);
            mInterface.setRepeatAction(null);
            mInterface.setEventHint(getString(R.string.remind_me));
        }
    }

    public boolean onBackPressed() {
        return true;
    }
}
