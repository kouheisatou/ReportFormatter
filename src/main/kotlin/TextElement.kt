import androidx.compose.material.Text
import androidx.compose.runtime.Composable

class TextElement(
    elementName: String,
    parentElement: Element?,
) : Element(
    elementName,
    parentElement,
) {

    @Composable
    override fun extractView(): Element {
        Text(elementName)
        return this
    }

    override fun exportToText(): String {
        return elementName
    }
}
