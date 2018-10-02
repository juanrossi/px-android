package com.mercadopago.android.px.internal.util;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.mercadopago.android.px.model.ExternalFragment;

public final class FragmentUtil {

    private FragmentUtil() {
    }

    public static void addFragmentInside(@NonNull final ViewGroup viewGroup,
        @IdRes final int resId,
        @NonNull final ExternalFragment model) {
        if (viewGroup.getContext() instanceof AppCompatActivity) {
            addFragmentInside(viewGroup, (AppCompatActivity) viewGroup.getContext(), resId, model.zClassName,
                model.args);
        } else {
            throw new IllegalArgumentException("Container context is not a activity");
        }
    }

    public static void addFragmentInside(@NonNull final ViewGroup viewGroup,
        @IdRes final int resId, @NonNull final Fragment fragment) {
        if (viewGroup.getContext() instanceof AppCompatActivity) {
            addFragmentInside(viewGroup, (AppCompatActivity) viewGroup.getContext(), resId, fragment);
        } else {
            throw new IllegalArgumentException("Container context is not a activity");
        }
    }

    private static void addFragmentInside(@NonNull final ViewGroup viewGroup,
        @NonNull final AppCompatActivity context,
        @IdRes final int resId,
        @NonNull final String zClassFragmentName,
        @Nullable final Bundle topArgs) {

        final Fragment fragment = FragmentUtil.createInstance(zClassFragmentName);
        fragment.setArguments(topArgs);

        addFragmentInside(viewGroup, context, resId, fragment);
    }

    private static void addFragmentInside(@NonNull final ViewGroup viewGroup,
        @NonNull final AppCompatActivity context,
        @IdRes final int resId,
        @NonNull final Fragment fragment) {

        final FrameLayout frameLayout = new FrameLayout(context);
        frameLayout.setId(resId);
        viewGroup.addView(frameLayout);
        context.getSupportFragmentManager()
            .beginTransaction()
            .replace(resId, fragment)
            .commit();
    }

    @NonNull
    private static Fragment createInstance(@NonNull final String className) {
        try {
            final Class<Fragment> clazz = (Class<Fragment>) Class.forName(className);
            return clazz.newInstance();
        } catch (final ClassNotFoundException e) {
            e.printStackTrace();
        } catch (final IllegalAccessException e) {
            e.printStackTrace();
        } catch (final InstantiationException e) {
            e.printStackTrace();
        }

        return new Fragment();
    }
}
