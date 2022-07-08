import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class AddCommandElement(
    elementName: String,
    parentElement: Element?,
) : Element(
    elementName,
    parentElement,
){
    private val targetElementName = elementName.split("-")[1]

    init {
        VariableElement(targetElementName, this@AddCommandElement)
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    override fun extractView(): Element {

        var active by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier
//                .onPointerEvent(PointerEventType.Enter){ active = true }
//                .onPointerEvent(PointerEventType.Move){ active = true }
//                .onPointerEvent(PointerEventType.Exit){ active = false }
//                .border(width = if(active) 2.dp else 1.dp, if(active) Color.Black else Color.Gray, shape = RoundedCornerShape(4.dp))
                .border(width = 1.dp, Color.Gray, shape = RoundedCornerShape(4.dp))
                .padding(5.dp)
        ) {
            var addCount by remember { mutableStateOf (0) }
            repeat(addCount){
                elements.add(mutableListOf(VariableElement(targetElementName, this@AddCommandElement).extractView()))
                Spacer(modifier = Modifier.height(5.dp))
            }
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    colors = ButtonDefaults.textButtonColors(
                        backgroundColor = Color.LightGray,
                        contentColor = Color.DarkGray,
                        disabledContentColor = Color.White,
                    ),
                    onClick = {
                        addCount++
                    }){
                    Text(elementName)
                }
                if(elements.isNotEmpty()){
                    Button(
                        colors = ButtonDefaults.textButtonColors(
                            backgroundColor = Color.LightGray,
                            contentColor = Color.DarkGray,
                            disabledContentColor = Color.White
                        ),
                        onClick = {
                            elements.removeAt(elements.lastIndex)
                            addCount--
                        }
                    ){
                        Text("delete-$targetElementName")
                    }
                }
            }
        }
        return this
    }
}