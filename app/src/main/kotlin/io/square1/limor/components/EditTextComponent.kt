package io.square1.limor.components

import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import io.square1.limor.R
import kotlinx.android.synthetic.main.component_edit_text.view.*
import org.jetbrains.anko.textColor


/**
 * This class is a custom edit text for Square1 projects. This class show a TextInputEditText with a TextInputLayout with white background, round corners with 4dp and when it has the focus a blue stroke is added.
 * In a future the background color and the stroke color will be added as parameters.
 * This class use a background xml drawable, you can find it in drawables folder with the name edittext.xml
 * You can find the xml view of this component in the folder layout with the filename component_edit_text.xml
 * The attributes defined for this component are in attrs.xml with the tag <declare-styleable name="edit_text_component_attributes">
 *
 * Example of usage:
 *
 *     <io.square1.limor.components.EditTextComponent
 *           android:id="@+id/customComponent1"
 *           android:layout_width="match_parent"
 *           android:layout_height="wrap_content"
 *           app:isPassword="false"
 *           app:hasShadow="false"
 *           app:fontColor="@color/darkBlueGrey"
 *           app:topTitleLeft="Password"
 *           app:topTextRight="Forgot your password?"
 *           app:textSize="17"
 *           android:layout_marginTop="100dp"/>
 *
 * @param hasShadow boolean indicates that the edit text must have shadow or not (plain).
 * @param isPassword boolean indicates that the edit text will contain a password and hide the chracters and show an eye icon at the right.
 * @param fontColor will be the font color of the text inside the edit text.
 * @param topTitleLeft If it's not null or empty this is the title text that the edittext will have, it's aligned at left. If it's null or empty the visibility will be GONE.
 * @param topTextRight If it's not null or empty this is the top right text that the edittext will have, it's aligned at right. If it's null or empty the visibility will be GONE.
 * @param textSize Set the size of the text of the edit text
 * @param lines Set the number of lines that will show the edittext
 * @param hintText Set the size of the text of the edit text
 * @param inputType Set the type of keyboard that will be displayed depending on if the edittext is an email, a number, a normal text or a password.
 *
 */
class EditTextComponent(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    init {
        inflate(context, R.layout.component_edit_text, this)

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.edit_text_component_attributes,
            0, 0
        ).apply {

            try {

                val hasShadow = getBoolean(R.styleable.edit_text_component_attributes_hasShadow, false)
                val isPassword = getBoolean(R.styleable.edit_text_component_attributes_isPassword, false)
                val fontColor = getString(R.styleable.edit_text_component_attributes_fontColor)
                val topTitleLeft = getString(R.styleable.edit_text_component_attributes_topTitleLeft)
                val topTextRight = getString(R.styleable.edit_text_component_attributes_topTextRight)
                val eTextSize = getString(R.styleable.edit_text_component_attributes_etTextSize)
                val isEditable = getBoolean(R.styleable.edit_text_component_attributes_isEditable, true)
                val lines = getInteger(R.styleable.edit_text_component_attributes_lines, 1)
                val hintText = getString(R.styleable.edit_text_component_attributes_hintText)
                val inputType = getInteger(R.styleable.edit_text_component_attributes_inputType, 0)

                //Top Text align at left (title of edittext)
                if (topTitleLeft.isNullOrEmpty()) {
                    my_left_title.visibility = View.GONE
                } else {
                    my_left_title.visibility = View.VISIBLE
                    my_left_title.text = topTitleLeft
                }

                //Top Text align at right
                if (topTextRight.isNullOrEmpty()) {
                    my_right_text.visibility = View.GONE
                } else {
                    my_right_text.visibility = View.VISIBLE
                    my_right_text.text = topTextRight
                }

                //Text color of the edittext
                myEdit.textColor = Color.parseColor(fontColor)

                //Check if edittext will contain a password or it will be plain text
                if (isPassword) {
                    //Password eye icon
                    myEditLyt.isPasswordVisibilityToggleEnabled = true
                    //Password characters
                    myEdit.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
                    myEdit.transformationMethod = PasswordTransformationMethod()
                } else {
                    myEditLyt.isPasswordVisibilityToggleEnabled = false
                    myEdit.inputType = InputType.TYPE_CLASS_TEXT
                }

                //inputType
                when (inputType){
                    1 -> myEdit.inputType = InputType.TYPE_CLASS_NUMBER
                    2 -> myEdit.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                }

                //Number of lines (eight)
                myEdit.setLines(lines)

                //Hint text
                myEdit.hint = hintText

                //Add shadow to the edittext
                if (hasShadow) {
                    myCardview.elevation = 4f
                } else {
                    myCardview.elevation = 0f
                }

                //Set text size the edittext
                if (!eTextSize.isNullOrEmpty()) {
                    myEdit.textSize = eTextSize.toFloat()
                }

                //Manage editable
                myEdit.isEnabled = isEditable


            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    fun setText(string: String) {
        myEdit.text = string.toEditable()
    }

    private fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)
}