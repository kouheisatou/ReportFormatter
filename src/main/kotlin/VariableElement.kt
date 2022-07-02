import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.mouseClickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import java.io.BufferedReader
import java.io.FileReader

open class VariableElement(
    elementName: String,
    parentElement: Element?,
) : Element(
    elementName,
    parentElement,
) {
    protected open val resourcePath = "$elementName.txt"

    @OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
    @Composable
    override fun extractView(): Element {
        println("extract : $elementName")

        val lines = resourceManager.getResource(resourcePath)

        var active by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier
                .border(width = if(active) 2.dp else 1.dp, if(active) Color.Black else Color.Gray, shape = RoundedCornerShape(4.dp))
                .padding(5.dp)
                .onPointerEvent(PointerEventType.Enter){ active = true }
                .onPointerEvent(PointerEventType.Move){ active = true }
                .onPointerEvent(PointerEventType.Exit){ active = false }
        ) {
            Text(
                text = resourcePath,
                modifier = Modifier
                    .background(Color.DarkGray, shape = RoundedCornerShape(4.dp))
                    .padding(2.dp),
                color = Color.LightGray
            )

            for(l in lines){

                val textsInLine = l.replace("\n", "").split("%%")
                val elementsInLine = mutableListOf<Element>()

                Row(verticalAlignment = Alignment.CenterVertically) {
                    for(t in textsInLine.withIndex()){
                        if(t.value == "") continue
                        if(t.index % 2 == 0){
                            elementsInLine.add(TextElement(t.value, this@VariableElement).extractView())
                        }else{
                            when{
                                // 予約語
                                // 引数は-の後に渡される
                                t.value.matches(Regex("^input-.*")) -> {
                                    val newElement = InputCommandElement(t.value, this@VariableElement).extractView() as InputCommandElement

                                    // InputCommandElementで同じ引数のInputCommandElementを子として持つElementが出てくるまで親をたどる
                                    var inputSyncDestinationParent = parentElement
                                    var inputSyncDestination: InputCommandElement? = null
                                    while (true){
                                        for(row in inputSyncDestinationParent?.elements ?: mutableListOf()){
                                            for(e in row){
                                                if(e is InputCommandElement){
                                                    if(e.defaultText == newElement.defaultText){
                                                        inputSyncDestination = e
                                                        break
                                                    }
                                                }
                                            }
                                        }
                                        inputSyncDestinationParent = inputSyncDestinationParent?.parentElement ?: break
                                    }
                                    newElement.inputText.value = inputSyncDestination?.inputText?.value ?: newElement.inputText.value

                                    elementsInLine.add(newElement)
                                }
                                t.value.matches(Regex("^add-.*")) -> elementsInLine.add(AddCommandElement(t.value, this@VariableElement).extractView())
                                // 予約語以外は通常の変数名として扱う
                                else -> elementsInLine.add(VariableElement(t.value, this@VariableElement).extractView())
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(5.dp))
                elements.add(elementsInLine)
                resultText.value = rootElement.exportToText()
            }
        }
        return this
    }
}
