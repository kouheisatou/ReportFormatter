import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.AwtWindow
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.awt.FileDialog
import java.awt.Frame
import java.io.File


var rootElement: Element? = null
var resultText: MutableState<String> = mutableStateOf("")
lateinit var resourceManager: ResourceManager
private var autoSaveFilenameElement: Element? = null

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
fun main() = application {

    // set init root element and dir
    resourceManager = ResourceManager("sample/", true)
    var openedFileName by remember { mutableStateOf("tutorial") }

    Window(
        onCloseRequest = ::exitApplication,
        title = "ReportFormatter",
    ) {

        // dialogs display state
        var errMsg by remember { mutableStateOf<String?>(null) }
        var filePickerState by remember { mutableStateOf<Int?>(null) }
        var overwriteConfirmationDialogState by remember { mutableStateOf(false) }

        // auto save
        var autoSaveSettingDialog by remember { mutableStateOf(false) }
        var enabledAutoSave by remember { mutableStateOf(false) }
        var autoSaveFilePath by remember { mutableStateOf<String?>(null) }

        // init result area
        resultText = remember { mutableStateOf("") }

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

        // file picker dialog
        when(filePickerState){
            FileDialog.LOAD -> {

                FilePicker("choose a root template file", filePickerState!!) { dir, file ->

                    val prevResourceManager = resourceManager
                    val prevRootElement = rootElement

                    openedFileName = if (file.matches(Regex(".+\\.txt"))) {
                        file.subSequence(0..file.length - 5).toString()
                    } else {
                        errMsg = "permitted .txt file only"
                        resourceManager = prevResourceManager
                        rootElement = prevRootElement
                        filePickerState = null
                        return@FilePicker
                    }
                    println(openedFileName)
                    try {
                        resourceManager = ResourceManager(dir)
                        rootElement = null
                    } catch (e: Exception) {
                        errMsg = e.message
                        resourceManager = prevResourceManager
                        rootElement = prevRootElement
                        filePickerState = null
                    }

                    filePickerState = null
                }
            }

            FileDialog.SAVE -> {
                FilePicker("choose save destination", filePickerState!!){ dir, file ->
                    println("save : $dir$file")
                    File("$dir$file").writeText(resultText.value)
                    filePickerState = null
                }
            }
        }

        // overwrite confirmation dialog
        if(overwriteConfirmationDialogState){
            AlertDialog(
                modifier = Modifier.width(((window.width)*4/7).dp),
                onDismissRequest = {   },
                title = {
                    Text(text = "上書き確認")
                },
                text = {
                    Text("$autoSaveFilePath はすでに存在します。上書きしますか？")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            enabledAutoSave = true
                            overwriteConfirmationDialogState = false
                        }
                    ) {
                        Text("上書き")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        overwriteConfirmationDialogState = false
                        autoSaveFilePath = null
                    }){
                        Text("キャンセル")
                    }
                }
            )
        }

        // set root element and format err check
        try{
            if(rootElement == null){
                rootElement = VariableElement(openedFileName, null)
                autoSaveFilenameElement = VariableElement("filename", null, rootElement!!.elementName + ".txt", outLine = false, nameTag = false)
                enabledAutoSave = false
            }
        }catch (e: Exception){
            println("template file error")
        }

        // extract view
        Row{

            Box(
                modifier = Modifier
                    .weight(2f)
                    .horizontalScroll(rememberScrollState()),
            ){
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    rootElement?.extractView()
                    resultText.value = rootElement?.exportToText() ?: ""
                }
            }

            Divider(
                color = Color.Black,
                modifier = Modifier.fillMaxHeight().width(1.dp)
            )

            TextField(
                value = resultText.value,
                onValueChange = { resultText.value = it },
                readOnly = true,
                modifier = Modifier.weight(1f)
            )
        }

        // auto save
        if(enabledAutoSave){
            val file = File(autoSaveFilePath!!)
            file.writeText(resultText.value)
            println("save : ${file.path}")
        }

        // auto save setting dialog
        if(autoSaveSettingDialog && rootElement != null){

            AlertDialog(

                modifier = Modifier.width((window.width*6/7).dp),
                onDismissRequest = {   },
                title = {
                    Text(text = "自動保存設定")
                },
                text = {

                    Column(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .fillMaxWidth()
                    ) {

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                        ) {

                            Text("自動保存")

                            Switch(
                                modifier = Modifier.fillMaxWidth(),
                                checked = enabledAutoSave,
                                onCheckedChange = {
                                    if(it){
                                        autoSaveFilePath = "${System.getProperty("user.home")}/Desktop/${autoSaveFilenameElement!!.exportToText()}"
                                        val file = File(autoSaveFilePath!!)
                                        if(file.exists()){
                                            overwriteConfirmationDialogState = true
                                        }else{
                                            enabledAutoSave = true
                                        }
                                    }else{
                                        enabledAutoSave = false
                                        autoSaveFilePath = null
                                    }
                                }
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                        ){
                            Text("保存先 : ")
                            autoSaveFilenameElement!!.extractView()
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            autoSaveSettingDialog = false
                        }
                    ) {
                        Text("閉じる")
                    }
                },
                dismissButton = null
            )
        }

        MenuBar {
            Menu("ファイル", mnemonic = 'F') {
                Item(
                    "テンプレートを開く",
                    shortcut = KeyShortcut(Key.O, ctrl = true),
                    onClick = {
                        filePickerState = FileDialog.LOAD
                    }
                )
                Item(
                    "保存",
                    shortcut = KeyShortcut(Key.S, ctrl = true),
                    onClick = {
                        filePickerState = FileDialog.SAVE
                    }
                )
                Item("自動保存設定", onClick = {
                    autoSaveSettingDialog = true
                })
            }
        }
    }
}

@Composable
fun FilePicker(title: String, mode: Int, afterPicked: (dir: String, file: String) -> Unit){

    AwtWindow(
        create = {
            object : FileDialog(Frame(), title, mode) {
                override fun setVisible(value: Boolean) {
                    super.setVisible(value)
                    if(value){
                        afterPicked(directory ?: return, file ?: return)
                    }
                }
            }
        },
        dispose = FileDialog::dispose,
    )
}