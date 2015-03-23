package com.byteshaft.smspopup;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SmsReceiver extends BroadcastReceiver implements View.OnClickListener {
    EditText mMessageBox = null;
    String photo = null;
    String number;
    String messageText;
    SmsMessage message;
    String contactName;
    Button cancel, reply;
    AlertDialog dialog;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        Object[] pdus = (Object[]) bundle.get("pdus");
        message = SmsMessage.createFromPdu((byte[]) pdus[0]);
        number = message.getOriginatingAddress();
        messageText = message.getMessageBody();
        contactName = getContactName(context, number);
        if (contactName == null ) {
            contactName = number;
        }
        showDialog();
    }

    private void showDialog() {
        LayoutInflater inflater = MainActivity.self.getLayoutInflater();
        LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.popup_layout, null);
        mMessageBox = (EditText) linearLayout.findViewById(R.id.editTextMsg);
        setInputFieldTextChangeListener();
        TextView incomingNumber = (TextView) linearLayout.findViewById(R.id.incomingNumber);
        TextView incomingMessage = (TextView) linearLayout.findViewById(R.id.tv);
        reply = (Button) linearLayout.findViewById(R.id.bReply);
        reply.setOnClickListener(this);
        cancel = (Button) linearLayout.findViewById(R.id.bCancel);
        cancel.setOnClickListener(this);
        incomingMessage.setText(messageText);
        incomingNumber.setText(contactName);
        ImageView imageView = (ImageView) linearLayout.findViewById(R.id.img);
        if (photo != null) {
            Uri uri = Uri.parse(photo);
            imageView.setImageURI(uri);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.self);
        builder.setView(linearLayout);
        builder.create();
        dialog = builder.show();
    }
    private void sendSms(String number, String message) {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(number, null, message, null, null);
    }

    public String getContactName(Context context, String phoneNumber) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME,
                ContactsContract.PhoneLookup.PHOTO_URI}, null, null, null);
        if (cursor == null) {
            return null;
        }
        String contactName = null;
        if(cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            photo = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.PHOTO_URI));
        }

        if(cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        return contactName;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bReply:
                sendSms(number, messageText);
                dialog.dismiss();
                break;
            case R.id.bCancel:
                dialog.dismiss();
        }
    }

    private void setInputFieldTextChangeListener() {
        mMessageBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mMessageBox.getText().toString().isEmpty()) {
                    reply.setEnabled(false);
                } else {
                    reply.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
}

