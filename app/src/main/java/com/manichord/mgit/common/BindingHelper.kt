package com.manichord.mgit.common

import com.google.android.material.textfield.TextInputLayout
import androidx.databinding.BindingAdapter


@BindingAdapter("app:errorText")
fun setErrorMessage(view: TextInputLayout, errorMessage: String?) {
    errorMessage.let { view.error = errorMessage }
}

