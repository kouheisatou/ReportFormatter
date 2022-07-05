import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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

    init {
        genChildren()
    }

    final override fun genChildren(){

        val lines = resourceManager.getResource(resourcePath)

        for(l in lines){

            val textsInLine = l.replace("\n", "").split("%%")
            val elementsInLine = mutableListOf<Element>()

            for(t in textsInLine.withIndex()){
                if(t.index % 2 == 0){
                    elementsInLine.add(TextElement(t.value, this@VariableElement))
                }else{
                    when{
                        // 予約語
                        // 引数は-の後に渡される
                        t.value.matches(Regex("^input-.*")) -> elementsInLine.add(InputCommandElement(t.value, this@VariableElement))
                        t.value.matches(Regex("^add-.*")) -> elementsInLine.add(AddCommandElement(t.value, this@VariableElement))
                        // 予約語以外は通常の変数名として扱う
                        else -> elementsInLine.add(VariableElement(t.value, this@VariableElement))
                    }
                }
            }

            elements.add(elementsInLine)
        }
    }

    @OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
    @Composable
    override fun extractView(): Element {
        println("extract : $elementName")

        var active by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier
                .fillMaxWidth()
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

            for(row in elements){

                Spacer(modifier = Modifier.height(5.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for(e in row){
                        e.extractView()

                        if(e is InputCommandElement){
                            // InputCommandElementで同じ引数のInputCommandElementを子として持つElementが出てくるまで親をたどる
                            var inputSyncDestinationParent = parentElement
                            var inputSyncDestination: InputCommandElement? = null
                            while (true){
                                for(parentRow in inputSyncDestinationParent?.elements ?: mutableListOf()){
                                    for(pe in parentRow){
                                        if(pe is InputCommandElement){
                                            if(pe.defaultText == e.defaultText){
                                                inputSyncDestination = pe
                                                break
                                            }
                                        }
                                    }
                                }
                                inputSyncDestinationParent = inputSyncDestinationParent?.parentElement ?: break
                            }
                            e.inputText.value = inputSyncDestination?.inputText?.value ?: e.inputText.value
                        }
                    }
                }
            }

            resultText.value = rootElement.exportToText()

        }
        return this
    }
}
