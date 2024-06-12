package com.dicoding.storyapp.ui.customeview

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText

class ValidatedEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {

    private var validationFunction: ((String) -> Boolean)? = null
    private var errorMessage: String? = null

    fun setValidationFunction(validationFunction: (String) -> Boolean, errorMessage: String) {
        this.validationFunction = validationFunction
        this.errorMessage = errorMessage

        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val text = s.toString()
                if (validationFunction.invoke(text) == false) {
                    error = errorMessage
                } else {
                    error = null
                }
            }
        })
    }
}