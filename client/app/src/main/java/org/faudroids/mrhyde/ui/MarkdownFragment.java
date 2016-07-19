package org.faudroids.mrhyde.ui;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.faudroids.mrhyde.R;

import java.util.ArrayList;

import timber.log.Timber;

public class MarkdownFragment extends DialogFragment {

    public interface MarkdownActionListener {
        void onInsertBoldText(DialogFragment dialog);
        void onInsertItalicText(DialogFragment dialog);
        void onInsertLink(DialogFragment dialog);
        void onInsertImage(DialogFragment dialog);
        void onInsertQuote(DialogFragment dialog);
        void onInsertCode(DialogFragment dialog);
        void onInsertUnorderedList(DialogFragment dialog);
        void onInsertOrderedList(DialogFragment dialog);
        void onInsertH1(DialogFragment dialog);
        void onInsertH2(DialogFragment dialog);
        void onInsertH3(DialogFragment dialog);
        void onInsertH4(DialogFragment dialog);
        void onInsertH5(DialogFragment dialog);
        void onInsertH6(DialogFragment dialog);
    }

    MarkdownActionListener mListener;

    View.OnClickListener markdownAction = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if(v instanceof Button) {
                Button btn = (Button) v;

                switch(btn.getId()) {
                    case R.id.btn_markdown_bold:
                        mListener.onInsertBoldText(MarkdownFragment.this);
                        break;
                    case R.id.btn_markdown_italic:
                        mListener.onInsertItalicText(MarkdownFragment.this);
                        break;
                    case R.id.btn_markdown_code:
                        mListener.onInsertCode(MarkdownFragment.this);
                        break;
                    case R.id.btn_markdown_quote:
                        mListener.onInsertQuote(MarkdownFragment.this);
                        break;
                    case R.id.btn_markdown_link:
                        mListener.onInsertLink(MarkdownFragment.this);
                        break;
                    case R.id.btn_markdown_image:
                        mListener.onInsertImage(MarkdownFragment.this);
                        break;
                    case R.id.btn_markdown_ul:
                        mListener.onInsertUnorderedList(MarkdownFragment.this);
                        break;
                    case R.id.btn_markdown_ol:
                        mListener.onInsertOrderedList(MarkdownFragment.this);
                        break;
                    case R.id.btn_markdown_h1:
                        mListener.onInsertH1(MarkdownFragment.this);
                        break;
                    case R.id.btn_markdown_h2:
                        mListener.onInsertH2(MarkdownFragment.this);
                        break;
                    case R.id.btn_markdown_h3:
                        mListener.onInsertH3(MarkdownFragment.this);
                        break;
                    case R.id.btn_markdown_h4:
                        mListener.onInsertH4(MarkdownFragment.this);
                        break;
                    case R.id.btn_markdown_h5:
                        mListener.onInsertH5(MarkdownFragment.this);
                        break;
                    case R.id.btn_markdown_h6:
                        mListener.onInsertH6(MarkdownFragment.this);
                        break;
                    default:
                        dismiss();
                }
                dismiss();
            }
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if(mListener == null) {
            try {
                mListener = (MarkdownActionListener) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException("Activity " + activity.toString() + " must implement" +
                        " MarkdownActionListener");
            }
        } else {
            throw new RuntimeException("MarkdownActionListener already registered.");
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_markdown, container, false);

        ArrayList<Button> buttonArrayList = new ArrayList<>();

        buttonArrayList.add((Button) rootView.findViewById(R.id.btn_markdown_italic));
        buttonArrayList.add((Button) rootView.findViewById(R.id.btn_markdown_bold));
        buttonArrayList.add((Button) rootView.findViewById(R.id.btn_markdown_link));
        buttonArrayList.add((Button) rootView.findViewById(R.id.btn_markdown_image));
        buttonArrayList.add((Button) rootView.findViewById(R.id.btn_markdown_quote));
        buttonArrayList.add((Button) rootView.findViewById(R.id.btn_markdown_code));
        buttonArrayList.add((Button) rootView.findViewById(R.id.btn_markdown_ul));
        buttonArrayList.add((Button) rootView.findViewById(R.id.btn_markdown_ol));
        buttonArrayList.add((Button) rootView.findViewById(R.id.btn_markdown_h1));
        buttonArrayList.add((Button) rootView.findViewById(R.id.btn_markdown_h2));
        buttonArrayList.add((Button) rootView.findViewById(R.id.btn_markdown_h3));
        buttonArrayList.add((Button) rootView.findViewById(R.id.btn_markdown_h4));
        buttonArrayList.add((Button) rootView.findViewById(R.id.btn_markdown_h5));
        buttonArrayList.add((Button) rootView.findViewById(R.id.btn_markdown_h6));

        for(Button b : buttonArrayList) {
            b.setOnClickListener(markdownAction);
        }

        return rootView;
    }
}
