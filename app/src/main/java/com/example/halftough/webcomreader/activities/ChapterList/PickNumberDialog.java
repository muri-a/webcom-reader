package com.example.halftough.webcomreader.activities.ChapterList;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.halftough.webcomreader.R;

public class PickNumberDialog extends DialogFragment {
    final private int DEFAULT_NUMBER = 5;
    NoticeNumberPickerListener listener;

    public interface NoticeNumberPickerListener{
        public void onNumberPickerPositiveClick(int value);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try{
            listener = (NoticeNumberPickerListener)activity;
        }
        catch (ClassCastException e){
            throw new ClassCastException(activity.toString() + " must implement NoticeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final EditText editText;
        ImageButton lessButton, moreButton;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.pick_number_dialog, null);
        editText = (EditText)view.findViewById(R.id.numberDialogEditText);
        lessButton = (ImageButton)view.findViewById(R.id.numberDialogLessButton);
        moreButton = (ImageButton)view.findViewById(R.id.numberDialogMoreButton);

        editText.setText(Integer.toString(DEFAULT_NUMBER));
        lessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int val = Integer.parseInt(editText.getText().toString());
                if(val>1)
                    val -= 1;
                editText.setText(Integer.toString(val));
            }
        });
        moreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int val = Integer.parseInt(editText.getText().toString());
                val += 1;
                editText.setText(Integer.toString(val));
            }
        });

        builder.setTitle(R.string.chapter_list_download_few_title);
        builder.setView(view);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onNumberPickerPositiveClick(Integer.parseInt(editText.getText().toString()));
            }
        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PickNumberDialog.this.getDialog().cancel();
            }
        });
        return builder.create();
    }
}
