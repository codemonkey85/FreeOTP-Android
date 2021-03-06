/*
 * FreeOTP
 *
 * Authors: Nathaniel McCallum <npmccallum@redhat.com>
 *
 * Copyright (C) 2013  Nathaniel McCallum, Red Hat
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fedorahosted.freeotp.dialogs;

import java.util.Locale;

import org.fedorahosted.freeotp.R;

import android.net.Uri;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

public class ManualDialogActivity extends BaseAddTokenDialogActivity implements OnItemSelectedListener {
    private static final String DEFAULT_INTERVAL = "30";
    private static final String DEFAULT_COUNTER  = "0";

    private final int           SHA1_OFFSET      = 1;
    private final int           TOTP_OFFSET      = 0;
    private EditText            mIssuer;
    private EditText            mLabel;
    private EditText            mSecret;
    private EditText            mInterval;
    private Spinner             mAlgorithm;
    private Spinner             mType;

    public ManualDialogActivity() {
        super(R.layout.manual, android.R.string.cancel, 0, R.string.add);
    }

    @Override
    public void onStart() {
        super.onStart();

        mIssuer = (EditText) findViewById(R.id.issuer);
        mLabel = (EditText) findViewById(R.id.label);
        mSecret = (EditText) findViewById(R.id.secret);
        mInterval = (EditText) findViewById(R.id.interval);
        mAlgorithm = (Spinner) findViewById(R.id.algorithm);
        mType = (Spinner) findViewById(R.id.type);

        // Select the default algorithm
        mAlgorithm.setSelection(SHA1_OFFSET);

        // Setup the Interval / Counter toggle
        mType.setOnItemSelectedListener(this);

        // Disable the Add button
        getButton(BUTTON_POSITIVE).setEnabled(false);

        // Set constraints on when the Add button is enabled
        TextWatcher tw = new ManualTextWatcher(this);
        mIssuer.addTextChangedListener(tw);
        mLabel.addTextChangedListener(tw);
        mSecret.addTextChangedListener(new ManualSecretTextWatcher(this));
        mInterval.addTextChangedListener(tw);
    }

    @Override
    public void onClick(View view, int which) {
        if (which != BUTTON_POSITIVE)
            return;

        // Get the fields
        String issuer = Uri.encode(mIssuer.getText().toString());
        String label = Uri.encode(mLabel.getText().toString());
        String secret = Uri.encode(mSecret.getText().toString());
        String type = mType.getSelectedItemId() == TOTP_OFFSET ? "totp" : "hotp";
        String algorithm = mAlgorithm.getSelectedItem().toString().toLowerCase(Locale.US);
        int interval = Integer.parseInt(mInterval.getText().toString());
        int digits = ((RadioButton) findViewById(R.id.digits6)).isChecked() ? 6 : 8;

        // Create the URI
        String uri = String.format(Locale.US, "otpauth://%s/%s:%s?secret=%s&algorithm=%s&digits=%d",
                                   type, issuer, label, secret, algorithm, digits);
        if (type.equals("totp"))
            uri = uri.concat(String.format("&period=%d", interval));
        else
            uri = uri.concat(String.format("&counter=%d", interval));

        // Add the token
        onTokenURI(uri);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        TextView tv = (TextView) findViewById(R.id.interval_label);
        if (position == 0) {
            tv.setText(R.string.interval);
            mInterval.setText(DEFAULT_INTERVAL);
        } else {
            tv.setText(R.string.counter);
            mInterval.setText(DEFAULT_COUNTER);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}
