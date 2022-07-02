import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import java.awt.FileDialog
import java.awt.Frame

lateinit var rootElement: RootElement
lateinit var resultText: MutableState<String>
lateinit var resourceManager: ResourceManager

fun main() = application {

    // カレントディレクトリ設定
    resourceManager = ResourceManager("sample/", true)

    Window(
        onCloseRequest = ::exitApplication,
        title = "ReportFormatter",
    ) {
        // RootElement名設定
        var openedFileName by remember { mutableStateOf("tutorial") }

        var openTemplate by remember { mutableStateOf(false) }
        MenuBar {
            Menu("ファイル", mnemonic = 'F') {
                Item("テンプレートを開く", onClick = {
                    openTemplate = true
                })
                if(openTemplate){
                    openFileDialog(
                        onCloseRequest = { directory, filename ->
                            openedFileName = if(filename.matches(Regex(".+\\.txt"))){
                                filename.subSequence(0 .. filename.length -5).toString()
                            }else{
                                return@openFileDialog
                            }
                            println(openedFileName)

                            rootElement = RootElement(openedFileName)
                            resourceManager = ResourceManager(directory ?: return@openFileDialog)
                        }
                    )
                }
            }
        }

        Row{
            Box() {
                LazyColumn() {
                    items(1) {
                        rootElement = RootElement(openedFileName)
                        rootElement.extractView()
                        openTemplate = false
                    }
                }
            }

            Divider(
                color = Color.Black,
                modifier = Modifier.fillMaxHeight().width(1.dp)
            )

            resultText = remember { mutableStateOf("") }
            TextField(
                value = resultText.value,
                onValueChange = { resultText.value = it },
                readOnly = true,
                modifier = Modifier.fillMaxHeight().fillMaxWidth()
            )
        }

    }

}



@Composable
private fun openFileDialog(
    parent: Frame? = null,
    onCloseRequest: (directory: String?, filename: String) -> Unit
) = AwtWindow(
    create = {
        object : FileDialog(parent, "Choose a root file", LOAD) {
            override fun setVisible(value: Boolean) {
                super.setVisible(value)
                if (value) {
                    onCloseRequest(directory, file)
                }
            }
        }
    },
    dispose = FileDialog::dispose
)