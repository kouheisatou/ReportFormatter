import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import java.awt.FileDialog
import java.awt.Frame

lateinit var rootElement: RootElement
var resultText: MutableState<String> = mutableStateOf("")
lateinit var resourceManager: ResourceManager

@OptIn(ExperimentalMaterialApi::class)
fun main() = application {

    // カレントディレクトリ設定
    resourceManager = ResourceManager("sample/", true)

    Window(
        onCloseRequest = ::exitApplication,
        title = "ReportFormatter",
    ) {
        // RootElement名設定
        var openedFileName by remember { mutableStateOf("tutorial") }
        var errMsg by remember { mutableStateOf<String?>(null) }
        var filePickerState by remember { mutableStateOf(false) }

        // err msg dialog
        if(errMsg != null){
            AlertDialog(
                modifier = Modifier.width((window.width*4/7).dp),
                onDismissRequest = {   },
                title = {
                    Text(text = "error")
                },
                text = {
                    Text(errMsg ?: "")
                },
                confirmButton = {
                    TextButton(
                        onClick = { // confirmをタップしたとき
                            errMsg = null
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = null
            )
        }

        if(filePickerState){
            AwtWindow(
                create = {
                    object : FileDialog(Frame(), "Choose a root file", LOAD) {
                        override fun setVisible(value: Boolean) {
                            super.setVisible(value)

                            val prevResourceManager = resourceManager
                            val prevRootElement = rootElement

                            if (value) {
                                openedFileName = if(file.matches(Regex(".+\\.txt"))){
                                    file.subSequence(0 .. file.length -5).toString()
                                }else{
                                    errMsg = "permitted .txt file only"
                                    resourceManager = prevResourceManager
                                    rootElement = prevRootElement
                                    filePickerState = false
                                    return
                                }
                                println(openedFileName)
                                try {
                                    resourceManager = ResourceManager(directory ?: return)
                                    rootElement = RootElement(openedFileName)
                                }catch (e: Exception){
                                    errMsg = e.message
                                    resourceManager = prevResourceManager
                                    rootElement = prevRootElement
                                    filePickerState = false
                                }
                            }
                            filePickerState = false
                        }
                    }
                },
                dispose = FileDialog::dispose,
            )
        }

        MenuBar {
            Menu("ファイル", mnemonic = 'F') {
                Item("テンプレートを開く", onClick = {
                    filePickerState = true
                })
            }
        }


        try{
            rootElement = RootElement(openedFileName)
        }catch (e: Exception){
            println("template file error")
        }

        Row{

            Box(
                modifier = Modifier
                    .weight(2f)
                    .horizontalScroll(rememberScrollState()),
            ){
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(1) {
                        rootElement.extractView()
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
                modifier = Modifier.weight(1f)
            )
        }

    }

}
