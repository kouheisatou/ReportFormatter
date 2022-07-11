import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * @param text nullを渡すと自動的に$ElementName.txtを読み込む。文字列を渡すとその文字列をElementに展開する
 * @param outLine 枠線をつけるかどうか
 * @param nameTag Element名を表示するか否か
 */
open class VariableElement(
    elementName: String,
    parentElement: Element?,
    text: String? = null,
    private val outLine: Boolean = true,
    private val nameTag: Boolean = true
) : Element(
    elementName,
    parentElement,
) {
    private val resourcePath: String = "$elementName.txt"

    init {
        val lines = text?.split("\n")
            ?: try{
                resourceManager.getResource(resourcePath)
            }catch (e: Exception){
                e.printStackTrace()
                throw Exception("this variable name does not exist : %%$elementName%%\ncalled from ${parentElement?.elementName ?: "root"}.txt")
            }

        genChildren(lines)
    }

    private fun genChildren(lines: List<String>){

        for(l in lines){

            val textsInLine = l.replace("\n", "").split("%%")
            val elementsInLine = mutableListOf<Element>()

            for(t in textsInLine.withIndex()){
                if(t.index % 2 == 0){
                    elementsInLine.add(TextElement(t.value, this))
                }else{
                    when{
                        // 予約語
                        // 引数は-の後に渡される
                        t.value.matches(Regex("^input-.*-.*")) -> elementsInLine.add(InputCommandElement(t.value, this, t.value.split("-")[2]))
                        t.value.matches(Regex("^input-.*")) -> elementsInLine.add(InputCommandElement(t.value, this))
                        t.value.matches(Regex("^add-.*")) -> elementsInLine.add(AddCommandElement(t.value, this))
                        // 予約語以外は通常の変数名として扱う
                        else -> elementsInLine.add(VariableElement(t.value, this))
                    }
                }
            }

            elements.add(elementsInLine)
        }
    }

    @Composable
    override fun extractView(): Element {

        val modifier = if(outLine){
            Modifier
                .border(width = 1.dp, Color.Gray, shape = RoundedCornerShape(4.dp))
                .padding(5.dp)
                .fillMaxWidth()
        }else{
            Modifier.fillMaxWidth()
        }

        Column(
            modifier = modifier
        ) {
            if(nameTag){
                Text(
                    text = resourcePath,
                    modifier = Modifier
                        .background(Color.DarkGray, shape = RoundedCornerShape(4.dp))
                        .padding(2.dp),
                    color = Color.LightGray
                )
            }

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
                                            if(pe.hint == e.hint){
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
        }
        return this
    }
}
