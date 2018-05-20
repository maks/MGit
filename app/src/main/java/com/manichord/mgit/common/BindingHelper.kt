package com.manichord.mgit.common

import android.support.design.widget.TextInputLayout
import android.databinding.BindingAdapter


@BindingAdapter("app:errorText")
fun setErrorMessage(view: TextInputLayout, errorMessage: String?) {
    errorMessage.let { view.error = errorMessage }
}

