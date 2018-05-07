package com.mercadopago;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mercadopago.example.R;

public class SampleTopFragment extends Fragment {

    public static final String SOME_PARCELABLE = "ARG_SOME_PARCELABLE";

    public static class ParcelableArgument implements Parcelable {

        private final String label;

        public ParcelableArgument(final String label) {
            this.label = label;
        }

        protected ParcelableArgument(Parcel in) {
            label = in.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(label);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<ParcelableArgument> CREATOR = new Creator<ParcelableArgument>() {
            @Override
            public ParcelableArgument createFromParcel(Parcel in) {
                return new ParcelableArgument(in);
            }

            @Override
            public ParcelableArgument[] newArray(int size) {
                return new ParcelableArgument[size];
            }
        };
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sample, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView textView = view.findViewById(R.id.textView);
        ParcelableArgument arg = getArguments().getParcelable(SOME_PARCELABLE);
        textView.setText(arg.label);
    }
}
