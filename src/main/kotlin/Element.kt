import androidx.compose.runtime.Composable

abstract class Element(val elementName: String, val parentElement: Element?) {

    val elements = mutableListOf<MutableList<Element>>()
    val id: Long = componentCount

    init {
        componentCount ++
    }

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

    override fun equals(other: Any?): Boolean {
        if(other is Element){
            if(other.id == this.id){
                return true
            }
        }
        return false
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