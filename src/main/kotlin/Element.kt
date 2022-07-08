import androidx.compose.runtime.Composable

abstract class Element(val elementName: String, val parentElement: Element?) {

    val elements = mutableListOf<MutableList<Element>>()

    @Composable
    abstract fun extractView(): Element

    open fun exportToText(): String {
        var result = ""
        for(row in elements.withIndex()){
            for(e in row.value){
                result += e.exportToText()
            }
            if(row.index != elements.size-1){
                result += "\n"
            }
        }
        return result
    }

    open fun applyAllChildren(applyToChild: (child: Element) -> Unit){
        for(row in elements){
            for(e in row){
                applyToChild(e)
                e.applyAllChildren(applyToChild)
            }
        }
    }

    override fun toString(): String {
        return "$elementName{$elements}"
    }
}