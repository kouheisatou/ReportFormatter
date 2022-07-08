import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.TextFieldValue

class InputCommandElement(
    elementName: String,
    parentElement: Element?,
    private val defaultText: String = ""
) : Element(
    elementName,
    parentElement,
){
    val hint = elementName.split("-")[1]
    var inputText: MutableState<TextFieldValue> = mutableStateOf(TextFieldValue(defaultText))

    @Composable
    override fun extractView(): Element {
        inputText = remember { mutableStateOf(TextFieldValue(inputText.value.text)) }
        TextField(
            value = inputText.value,
            onValueChange = {
                inputText.value = it
                // 同じ引数を持つ子要素のtextを全て同期するように更新
                parentElement?.applyAllChildren { e: Element ->
                    if(e is InputCommandElement){
                        if(e.hint == this.hint){
                            e.inputText.value = it
                        }
                    }
                }
            },
            label = { Text(hint) }
        )
        return this
    }

    override fun exportToText(): String {
        return inputText.value.text
    }

    override fun toString(): String {
        return "${this.javaClass}{${inputText.value.text}}"
    }
}