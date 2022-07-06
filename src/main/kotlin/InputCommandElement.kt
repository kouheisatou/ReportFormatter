import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.TextFieldValue

class InputCommandElement(
    elementName: String,
    parentElement: Element?,
) : Element(
    elementName,
    parentElement,
){
    val defaultText = elementName.split("-")[1]
    var inputText: MutableState<TextFieldValue> = mutableStateOf(TextFieldValue(""))

    @Composable
    override fun extractView(): Element {
        inputText = remember { mutableStateOf(TextFieldValue()) }
        TextField(
            value = inputText.value,
            onValueChange = {
                inputText.value = it
                // 同じ引数を持つ子要素のtextを全て同期するように更新
                parentElement?.applyAllChildren { e: Element ->
                    if(e is InputCommandElement){
                        if(e.defaultText == this.defaultText){
                            e.inputText.value = it
                        }
                    }
                }
            },
            label = { Text(defaultText) }
        )
        return this
    }

    override fun exportToText(): String {
        return inputText.value.text
    }
}